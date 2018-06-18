package parameter;

import javafx.scene.Node;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;

public class ColorParameter extends InterpolatedParameter<Color>
{
	public ColorParameter(String name, String help, boolean interpolate, InterpolatedParameter<Double> time, Color... values)
	{
		super(name, help, interpolate, time, values);
	}

	@Override
	InterpolatedParameter<Color> getSimple()
	{
		return new ColorParameter(getName(), getHelp(), false, null, getValue());
	}

	@Override
	Color interpolate(Color start, Color end, double time)
	{
		double startRed = start.getRed();
		double startGreen = start.getGreen();
		double startBlue = start.getBlue();
		double startOpacity = start.getOpacity();

		double endRed = end.getRed();
		double endGreen = end.getGreen();
		double endBlue = end.getBlue();
		double endOpacity = end.getOpacity();

		double red = startRed + (endRed - startRed) * time;
		double green = startGreen + (endGreen - startGreen) * time;
		double blue = startBlue + (endBlue - startBlue) * time;
		double opacity = startOpacity + (endOpacity - startOpacity) * time;

		return Color.color(red, green, blue, opacity);
	}

	@Override
	Node getControlInner()
	{
		ColorPicker colorPicker = new ColorPicker();

		colorPicker.setValue(getValue());
		colorPicker.valueProperty().bindBidirectional(this);
		return colorPicker;
	}
}
