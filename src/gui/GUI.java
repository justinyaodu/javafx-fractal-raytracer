package gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import render.Renderer;
import util.Utils;

import java.io.File;

public class GUI extends Application
{
	public static final int SPACING = 5;

	private Renderer renderer = new Renderer();

	private ImageView imageView;

	public static void main(String[] args)
	{
		launch(args);
	}

	@Override
	public void start(Stage stage)
	{
		stage.setTitle("JavaFX Fractal Raytracer");
		stage.setScene(new Scene(buildRoot(), 1200, 800));
		stage.show();
	}

	private BorderPane buildRoot()
	{
		return new BorderPane(buildView(), buildTop(), null, null, renderer.getParameters());
	}

	private VBox buildTop()
	{
		return new VBox(SPACING, buildMenu(), renderer.getTopBar());
	}

	private MenuBar buildMenu()
	{
		MenuItem saveImage = new MenuItem("_Save image...");
		saveImage.setAccelerator(KeyCombination.valueOf("shortcut+s"));
		saveImage.setOnAction(actionEvent -> saveImage());

		Menu file = new Menu("_File", null, saveImage);

		MenuBar menuBar = new MenuBar(file);
		menuBar.setUseSystemMenuBar(true);
		return menuBar;
	}

	private ScrollPane buildView()
	{
		imageView = new ImageView();
		imageView.imageProperty().bind(renderer.valueProperty());

		ScrollPane scrollPane = new ScrollPane(imageView);
		scrollPane.setPannable(true);

		MenuItem saveImage = new MenuItem("Save image...");
		saveImage.setOnAction(actionEvent -> saveImage());

		scrollPane.setContextMenu(new ContextMenu(saveImage));
		return scrollPane;
	}

	private void saveImage()
	{
		if (imageView.getImage() == null)
		{
			new Alert(Alert.AlertType.ERROR, "No image rendered!", ButtonType.OK).showAndWait();
			return;
		}

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save Image");
		fileChooser.setInitialFileName(renderer.getFilename() + ".png");

		File file = fileChooser.showSaveDialog(null);
		if (file != null)
		{
			if (!Utils.saveImage(renderer.getValue(), file))
			{
				new Alert(Alert.AlertType.ERROR, "Unable to save image!", ButtonType.OK).showAndWait();
			}
		}
	}
}
