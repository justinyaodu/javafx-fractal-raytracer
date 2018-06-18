package parameter;

import gui.GUI;
import gui.Labelable;
import javafx.beans.Observable;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public abstract class InterpolatedParameter<T> extends Parameter<T>
{
	private boolean allowInterpolation;
	private boolean interpolationInitialized;
	private boolean forceInterpolation = false;

	private InterpolatedParameter<Double> time;

	private BooleanParameter interpolationEnabled;

	private ObservableList<Keyframe> keyframes = FXCollections.observableArrayList();

	private ObjectBinding<T> interpolationBinding;

	public InterpolatedParameter(String name, String help, boolean interpolate, InterpolatedParameter<Double> time, T... values)
	{
		super(name, help, values[0]);

		allowInterpolation = time != null;
		this.time = time;

		interpolate &= allowInterpolation;

		interpolationEnabled = new BooleanParameter("Interpolation Enabled", "Enable interpolation for this parameter", false);
		interpolationEnabled.addListener((observableValue, aBoolean, t1) ->
		{
			if (t1)
			{
				if (!interpolationInitialized)
				{
					keyframes = FXCollections.observableArrayList(keyframe -> new Observable[]{keyframe.time});

					for (int i = 0; i < values.length; i++)
					{
						double keyframeTime = values.length > 1 ? i / (values.length - 1.0) : 0;
						addKeyframe(keyframeTime, values[i]);
					}

					interpolationBinding = new ObjectBinding<T>()
					{
						{
							super.bind(time, keyframes);
						}

						@Override
						protected T computeValue()
						{
							return InterpolatedParameter.this.getValue(time.getValue());
						}
					};
					interpolationInitialized = true;
				}

				bind(interpolationBinding);
			}
			else
			{
				unbind();
			}
		});
		interpolationEnabled.setValue(interpolate);
	}

	public T getValue(double time)
	{
		if (!interpolationEnabled.getValue())
		{
			return super.getValue();
		}

		if (time <= keyframes.get(0).time.getValue())
		{
			return keyframes.get(0).value.getValue();
		}
		for (int i = 1; i < keyframes.size(); i++)
		{
			Keyframe start = keyframes.get(i - 1);
			Keyframe end = keyframes.get(i);

			if (time == end.time.getValue())
			{
				return end.value.getValue();
			}
			else if (time < end.time.getValue())
			{
				return interpolate(start.value.getValue(), end.value.getValue(),
						(time - start.time.getValue()) / (end.time.getValue() - start.time.getValue()));
			}
		}
		return keyframes.get(keyframes.size() - 1).value.getValue();
	}

	abstract T interpolate(T start, T end, double time);

	@Override
	public Node getDecoration()
	{
		if (!allowInterpolation)
		{
			return super.getDecoration();
		}

		Button showKeyframeWindow = new Button("...");
		showKeyframeWindow.setOnAction(actionEvent -> showKeyframeWindow());
		showKeyframeWindow.disableProperty().bind(new BooleanBinding()
		{
			{
				super.bind(interpolationEnabled);
			}

			@Override
			protected boolean computeValue()
			{
				return !interpolationEnabled.getValue();
			}
		});
		Tooltip.install(showKeyframeWindow, new Tooltip("Configure interpolation for this parameter"));

		Node interpolationControl = interpolationEnabled.getControl();
		interpolationControl.setDisable(forceInterpolation);
		Tooltip.install(interpolationControl, new Tooltip("Enable interpolation for this parameter"));

		return new HBox(GUI.SPACING, interpolationControl, showKeyframeWindow);
	}

	private void showKeyframeWindow()
	{
		Stage stage = new Stage();
		stage.setTitle("Keyframes for " + getName());

		ListView<Keyframe> listView = new ListView<>(keyframes);
		listView.setCellFactory(keyframeListView -> new KeyframeCell());

		Button ok = new Button("OK");
		ok.setOnAction(actionEvent -> stage.close());

		Button addKeyframe = new Button("Add keyframe");
		addKeyframe.setOnAction(actionEvent -> addKeyframe(time.getValue(), getValue()));

		//		GridPane top = new GridPane();
		//		top.setHgap(GUI.SPACING);
		//		top.setVgap(GUI.SPACING);
		//		top.setPadding(new Insets(GUI.SPACING));
		//
		//		top.add(Labelable.getLabel(time, false), 0, 0);
		//		top.add(time.getControl(), 1, 0);
		//		top.add(Labelable.getLabel(this, false), 0, 1);
		//		top.add(getControl(), 1, 1);

		HBox top = new HBox(GUI.SPACING, Labelable.getLabel(time, false), time.getControl(), Labelable.getLabel(this, false), getControl());
		top.setPadding(new Insets(GUI.SPACING));

		VBox vBox = new VBox(GUI.SPACING, top, listView, new BorderPane(null, null, ok, null, addKeyframe));
		vBox.setPadding(new Insets(GUI.SPACING));

		stage.setScene(new Scene(vBox));
		stage.sizeToScene();
		stage.showAndWait();
	}

	public void addKeyframe(double time, T value)
	{
		InterpolatedParameter<Double> timeSimple = this.time.getSimple();
		timeSimple.setValue(time);

		InterpolatedParameter<T> valueSimple = getSimple();
		valueSimple.setValue(value);
		valueSimple.addListener((observableValue, t, t1) ->
		{
			//a workaround to trigger value recalculation without changing the keyframe list, which would refresh the listview
			this.time.fireValueChangedEvent();
		});

		Keyframe keyframe = new Keyframe(timeSimple, valueSimple);
		timeSimple.addListener((observableValue, aDouble, t1) -> resort());

		keyframes.add(keyframe);
		resort();
	}

	public void removeKeyframe(Object keyframe)
	{
		if (keyframes.size() > 1)
		{
			keyframes.remove(keyframe);
		}
	}

	public Keyframe getKeyframe(int index)
	{
		return keyframes.get(index);
	}

	private void resort()
	{
		keyframes.sort(null);
	}

	@Override
	public Node getControl()
	{
		Node control = getControlInner();
		control.disableProperty().bind(interpolationEnabled);
		return control;
	}

	abstract Node getControlInner();

	abstract InterpolatedParameter<T> getSimple();

	public void setForceInterpolation(boolean forceInterpolation)
	{
		this.forceInterpolation = forceInterpolation;
	}

	class Keyframe implements Comparable<Keyframe>
	{
		private InterpolatedParameter<Double> time;
		private InterpolatedParameter<T> value;

		private Keyframe(InterpolatedParameter<Double> time, InterpolatedParameter<T> value)
		{
			this.time = time;
			this.value = value;
		}

		private Node getNode()
		{
			Button remove = new Button("-");
			remove.setOnAction(actionEvent -> removeKeyframe(this));
			Tooltip.install(remove, new Tooltip("Remove this keyframe"));

			return new HBox(GUI.SPACING, time.getControl(), value.getControl(), remove);
		}

		@Override
		public int compareTo(Keyframe other)
		{
			return Double.compare(time.getValue(), other.time.getValue());
		}
	}

	private class KeyframeCell extends ListCell<Keyframe>
	{
		@Override
		protected void updateItem(Keyframe item, boolean empty)
		{
			super.updateItem(item, empty);
			setText("");
			if (item != null)
			{
				setGraphic(item.getNode());
			}
			else
			{
				setText(null);
				setGraphic(null);
			}
		}
	}
}