package parameter;

import gui.Labelable;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.layout.HBox;

public abstract class Parameter<T> extends SimpleObjectProperty<T> implements Labelable
{
	private String help;

	public Parameter(String name, String help, T value)
	{
		super(null, name, value);
		this.help = help;
	}

	public String getHelp()
	{
		return help;
	}

	public abstract Node getControl();

	public Node getDecoration()
	{
		return new HBox();
	}
}
