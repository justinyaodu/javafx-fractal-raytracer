package parameter;

import javafx.scene.Node;
import javafx.scene.control.TextField;


public abstract class NumericParameter<T extends Number> extends InterpolatedParameter<T>
{
	public enum Validate
	{
		NONE,
		CLAMP
	}

	public NumericParameter(String name, String help, boolean interpolate, InterpolatedParameter<Double> time, T... values)
	{
		super(name, help, interpolate, time, values);
	}

	@Override
	Node getControlInner()
	{
		TextField textField = new TextField();
		textField.setText(getValue().toString());
		textField.focusedProperty().addListener((observableValue, aBoolean, t1) ->
		{
			if (!t1)
			{
				try
				{
					setValue(parse(textField.getText()));
				}
				catch (NumberFormatException e)
				{
					textField.setText(getValue().toString());
				}
			}
		});
		addListener((observableValue, t, t1) -> textField.setText(t1.toString()));
		return textField;
	}

	abstract T parse(String string) throws NumberFormatException;
}
