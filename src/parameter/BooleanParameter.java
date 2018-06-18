package parameter;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;

public class BooleanParameter extends Parameter<Boolean>
{
	public BooleanParameter(String name, String help, boolean value)
	{
		super(name, help, value);
	}

	@Override
	public Node getControl()
	{
		CheckBox checkBox = new CheckBox();
		checkBox.setSelected(getValue());
		checkBox.selectedProperty().bindBidirectional(this);
		return checkBox;
	}
}
