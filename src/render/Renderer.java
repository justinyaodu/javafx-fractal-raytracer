package render;

import gui.GUI;
import gui.Labelable;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import parameter.ChoiceParameter;
import parameter.DoubleParameter;
import parameter.Parameter;
import parameter.ParameterGroup;
import util.Utils;
import util.Vector3;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Renderer extends Service<Image>
{
	private ChoiceParameter<Renderable> renderables = new ChoiceParameter<>("Shape", "The shape to be rendered",
			new Mandelbrot3(), new Mandelbrot4(), new Julia(), new MengerSponge(), new AntiMengerSponge(), new TangentBubbles());

	private ChoiceParameter<String> renderMode = new ChoiceParameter<>("Render Mode", "Toggles between rendering individual frames or sequences", "Single Image", "Sequence");

	private BooleanProperty renderSequence = new SimpleBooleanProperty();

	{
		renderSequence.bind(new BooleanBinding()
		{
			{
				bind(renderMode);
			}

			@Override
			protected boolean computeValue()
			{
				return renderMode.getValue().equals("Sequence");
			}
		});
	}

	private BooleanProperty busy = new SimpleBooleanProperty();

	{
		busy.bind(new BooleanBinding()
		{
			{
				bind(stateProperty());
			}

			@Override
			protected boolean computeValue()
			{
				return stateProperty().getValue().equals(State.RUNNING);
			}
		});
	}

	private Node topBar = buildTopBar();
	private Node parameters = buildParameters();

	private String imagePath;

	public Node getTopBar()
	{
		return topBar;
	}

	public Node getParameters()
	{
		return parameters;
	}

	private BorderPane buildTopBar()
	{
		Button render = new Button("Render");
		render.setOnAction(actionEvent ->
		{
			if (renderSequence.getValue())
			{
				DirectoryChooser directoryChooser = new DirectoryChooser();
				directoryChooser.setTitle("Select Image Save Directory");

				File imageDirectory = directoryChooser.showDialog(null);

				if (imageDirectory == null)
				{
					return;
				}

				imagePath = imageDirectory.getAbsolutePath() + File.separator + renderables.getValue().getFilename() + "_";
			}

			reset();
			start();
		});
		render.disableProperty().bind(busy);

		Button cancel = new Button("Cancel");
		cancel.setOnAction(actionEvent -> cancel());
		cancel.disableProperty().bind(busy.not());

		Node renderModeControl = renderMode.getControl();
		renderModeControl.disableProperty().bind(busy);

		HBox renderControls = new HBox(GUI.SPACING, renderModeControl, render, cancel);

		ProgressBar progressBar = new ProgressBar();
		progressBar.setPrefWidth(Double.MAX_VALUE);
		progressBar.setPadding(new Insets(0, GUI.SPACING, 0, GUI.SPACING));
		progressBar.progressProperty().bind(progressProperty());

		Node renderablesControl = renderables.getControl();
		renderablesControl.disableProperty().bind(busy);

		BorderPane borderPane = new BorderPane(progressBar, null, renderControls, null, renderablesControl);
		borderPane.setPadding(new Insets(GUI.SPACING));
		return borderPane;
	}

	private ScrollPane buildParameters()
	{
		ScrollPane scrollPane = new ScrollPane();
		scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

		scrollPane.contentProperty().bind(new ObjectBinding<Node>()
		{
			{
				bind(renderables);
			}

			@Override
			protected Node computeValue()
			{
				List<ParameterGroup> parameterGroups = renderables.getValue().getParameterGroups();

				GridPane gridPane = new GridPane();
				gridPane.setHgap(GUI.SPACING);
				gridPane.setVgap(GUI.SPACING);
				gridPane.setPadding(new Insets(GUI.SPACING));

				int rowIndex = 0;
				int labelCol = 0;
				int controlCol = 1;
				int decorationCol = 2;

				for (ParameterGroup group : parameterGroups)
				{
					gridPane.add(Labelable.getLabel(group, true), labelCol, rowIndex++);

					for (Parameter parameter : group.getParameters())
					{
						gridPane.add(Labelable.getLabel(parameter, false), labelCol, rowIndex);
						gridPane.add(parameter.getControl(), controlCol, rowIndex);
						gridPane.add(parameter.getDecoration(), decorationCol, rowIndex++);
					}
				}

				gridPane.disableProperty().bind(busy);

				return gridPane;
			}
		});

		return scrollPane;
	}

	public String getFilename()
	{
		return renderables.getValue().getFilename();
	}

	@Override
	protected Task<Image> createTask()
	{
		return new RenderTask();
	}

	private class RenderTask extends Task<Image>
	{
		private Renderable renderable = renderables.getValue();

		private double time;

		private double cycleSize;
		private double cycleOffset;

		@Override
		protected Image call()
		{
			int frameCount = renderSequence.getValue() ? renderable.frameCount.getValue() : 1;

			for (int f = 0; f < frameCount; f++)
			{
				long initTime = System.currentTimeMillis();

				time = frameCount == 1 ? renderable.time.getValue() :
						new DoubleParameter(null, null, true, new DoubleParameter(null, null, false, null, 0.0), renderable.startTime.getValue(), renderable.endTime.getValue()).getValue(f / (frameCount - 1.0));

				renderable.cacheCustomParameters(time);

				cycleSize = renderable.colorCycleSize.getValue(time);
				cycleOffset = renderable.colorCycleOffset.getValue(time);

				Vector3 directionNormalized = getDirectionNormalized();
				Vector3 cameraPosition = directionNormalized.scale(-renderable.cameraDistance.getValue(time));

				Vector3 right = new Vector3(directionNormalized.Y, -directionNormalized.X, 0).normalized();
				Vector3 down = directionNormalized.cross(right).normalized();

				int width = renderable.imageWidth.getValue();
				int height = renderable.imageHeight.getValue();

				double viewportHeight = renderable.viewportWidth.getValue(time) * height / width;
				double pixelScale = renderable.viewportWidth.getValue(time) / width;

				Vector3 topLeftCorner = cameraPosition.add(right.scale(-0.5 * renderable.viewportWidth.getValue(time) + renderable.viewportOffsetX.getValue(time)))
						.add(down.scale(-0.5 * viewportHeight - renderable.viewportOffsetY.getValue(time)));

				right = right.scale(pixelScale);
				down = down.scale(pixelScale);

				Vector3 topLeftCenter = topLeftCorner.add(right.scale(-0.5)).add(down.scale(-0.5));

				double stepSize = renderable.rayStepSize.getValue(time);

				Vector3 direction = directionNormalized.scale(stepSize);

				int stepCount = (int) Math.ceil(renderable.rayLength.getValue(time) / stepSize) + 1;

				int arrayWidth = width + 2;
				int arrayHeight = height + 2;

				Vector3[] rights = new Vector3[arrayWidth];
				for (int i = 0; i < arrayWidth; i++)
				{
					rights[i] = right.scale(i);
				}
				Vector3[] downs = new Vector3[arrayHeight];
				for (int i = 0; i < arrayHeight; i++)
				{
					downs[i] = down.scale(i);
				}
				Vector3[] directions = new Vector3[stepCount];
				for (int i = 0; i < stepCount; i++)
				{
					directions[i] = direction.scale(i);
				}

				System.err.printf("init completed in %d ms\n", System.currentTimeMillis() - initTime);
				long raytraceTime = System.currentTimeMillis();

				Raytrace[][] raytraces = new Raytrace[arrayWidth][arrayHeight];

				boolean ghostAll = renderable.ghostAllLayers.getValue();
				int iterations = renderable.maxIterations.getValue(time);

				double totalWork = arrayWidth * arrayHeight;

				int threadSleepMs = renderable.threadSleepMs.getValue();

				int threadCount = renderable.threadCount.getValue();
				Thread[] threads = new Thread[threadCount];
				int[] progresses = new int[threadCount];
				for (int t = 0; t < threadCount; t++)
				{
					int id = t;

					Thread thread = new Thread(() ->
					{
						for (int i = id; i < arrayWidth; i += threadCount)
						{
							Vector3 positionWidth = topLeftCenter.add(rights[i]);
							for (int j = 0; j < arrayHeight; j++)
							{
								Vector3 positionWidthHeight = positionWidth.add(downs[j]);
								Raytrace raytrace = new Raytrace();
								for (int k = 0; k < stepCount; k++)
								{
									Vector3 position = positionWidthHeight.add(directions[k]);
									int result = renderable.compute(position);
									if (raytrace.addIntersection(result, position, k, iterations) && !ghostAll)
									{
										break;
									}
								}
								raytraces[i][j] = raytrace;
								progresses[id]++;
							}
						}
					});

					threads[t] = thread;
					thread.start();
				}

				boolean alive = true;
				while (alive)
				{
					int sum = 0;
					alive = false;

					for (int t = 0; t < threads.length; t++)
					{
						alive |= threads[t].isAlive();
						sum += progresses[t];
					}
					updateProgress((double) sum / (arrayWidth * arrayHeight), 1);
					try
					{
						Thread.sleep(threadSleepMs);
					}
					catch (InterruptedException e)
					{
						return getValue();
					}
				}

				System.err.printf("raytrace completed in %d ms\n", System.currentTimeMillis() - raytraceTime);
				long imageTime = System.currentTimeMillis();

				WritableImage image = new WritableImage(width, height);

				double[] alphas = new double[iterations + 1];
				for (int i = 1; i <= iterations; i++)
				{
					alphas[i] = Utils.clamp(i * renderable.ghostingIntensity.getValue(time) / (iterations * iterations));
				}

				Color background = renderable.backgroundColor.getValue(time);
				boolean shading = renderable.shadingEnabled.getValue();
				double brightScalar = renderable.brightnessScalar.getValue(time);
				double brightOffset = renderable.brightnessOffset.getValue(time);
				boolean ghost = renderable.ghostingEnabled.getValue();

				int[] offsetX = new int[]{-1, 0, 1, 1, 1, 0, -1, -1, -1};
				int[] offsetY = new int[]{1, 1, 1, 0, -1, -1, -1, 0, 1};

				threads = new Thread[threadCount];
				for (int t = 0; t < threadCount; t++)
				{
					int id = t;

					Thread thread = new Thread(() ->
					{
						for (int i = id; i < width; i += threadCount)
						{
							int x = i + 1;

							for (int j = 0; j < height; j++)
							{
								int y = j + 1;

								double r = background.getRed();
								double g = background.getGreen();
								double b = background.getBlue();

								Raytrace raytrace = raytraces[x][y];

								if (raytrace.targetIndex > -1)
								{
									if (!ghostAll)
									{
										double brightness = 1;

										if (shading)
										{
											Vector3 middle = raytrace.getSurface();

											Vector3 normal = new Vector3();

											for (int k = 0; k < 8; k++)
											{
												normal = normal.add(normal(middle,
														raytraces[x + offsetX[k]][y + offsetY[k]].getSurface(),
														raytraces[x + offsetX[k + 1]][y + offsetY[k + 1]].getSurface()));
											}

											normal = normal.scale(0.125);
											brightness = Utils.clamp(normal.dot(directionNormalized) * brightScalar + brightOffset);
										}

										Color color = getColor(raytrace.intersections.get(raytrace.targetIndex).steps * stepSize);
										r = color.getRed() * brightness;
										g = color.getGreen() * brightness;
										b = color.getBlue() * brightness;
									}
								}

								if (ghost)
								{
									for (int k = (raytrace.targetIndex == -1 || ghostAll) ? (raytrace.intersections.size() - 1) : (raytrace.targetIndex - 1); k >= 0; k--)
									{
										Intersection intersection = raytrace.intersections.get(k);
										Color ghostColor = getColor(intersection.steps * stepSize);
										double alpha = alphas[intersection.iteration];
										r = alpha * ghostColor.getRed() + r * (1 - alpha);
										g = alpha * ghostColor.getGreen() + g * (1 - alpha);
										b = alpha * ghostColor.getBlue() + b * (1 - alpha);
									}
								}

								image.getPixelWriter().setColor(i, j, Color.color(r, g, b));
							}
						}
					});

					threads[t] = thread;
					thread.start();
				}

				alive = true;
				while (alive)
				{
					int sum = 0;
					alive = false;

					for (int t = 0; t < threads.length; t++)
					{
						alive |= threads[t].isAlive();
						sum += progresses[t];
					}
					updateProgress((double) sum / (arrayWidth * arrayHeight), 1);
					try
					{
						Thread.sleep(threadSleepMs);
					}
					catch (InterruptedException e)
					{
						return getValue();
					}
				}

				updateValue(image);

				System.err.printf("image writing completed in %d ms\n\n", System.currentTimeMillis() - imageTime);

				if (frameCount > 1)
				{
					Utils.saveImage(image, new File(imagePath + String.format("%0" + String.valueOf(frameCount + 1).length() + "d", f + 1)));
				}
			}

			return getValue();
		}

		private Color getColor(double distance)
		{
			double colorValue = (distance % cycleSize + cycleSize) % cycleSize / cycleSize;
			colorValue = ((colorValue + cycleOffset) % 1 + 1) % 1;
			return renderable.colorCycle.getValue(colorValue);
		}

		class Raytrace
		{
			private ArrayList<Intersection> intersections = new ArrayList<>();
			private int targetIndex = -1;
			private int previous = 0;

			private boolean addIntersection(int iteration, Vector3 point, int steps, int target)
			{
				if (iteration != previous)
				{
					intersections.add(new Intersection(Math.max(iteration, previous), point, steps));
					previous = iteration;

					if (iteration >= target && targetIndex == -1)
					{
						targetIndex = intersections.size() - 1;
						return true;
					}
				}

				return false;
			}

			public Vector3 getSurface()
			{
				return targetIndex == -1 ? null : intersections.get(targetIndex).surface;
			}
		}

		class Intersection
		{
			private int iteration;
			private Vector3 surface;
			private int steps;

			Intersection(int iteration, Vector3 surface, int steps)
			{
				this.iteration = iteration;
				this.surface = surface;
				this.steps = steps;
			}
		}

		private Vector3 getDirectionNormalized()
		{
			double longitude = renderable.cameraLongitude.getValue(time) * Math.PI / 180;
			double latitude = renderable.cameraLatitude.getValue(time) * Math.PI / 180;

			double z = Math.sin(latitude);
			double base = Math.cos(latitude);
			double y = base * Math.sin(longitude);
			double x = base * Math.cos(longitude);

			return new Vector3(-x, -y, -z);
		}

		private Vector3 normal(Vector3 a, Vector3 b, Vector3 c)
		{
			if (a == null || b == null || c == null)
			{
				return new Vector3();
			}

			return a.subtract(b).cross(c.subtract(b)).normalized();

		}
	}
}
