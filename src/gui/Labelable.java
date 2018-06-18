package gui;

import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;

public interface Labelable
{
	String getName();

	String getHelp();

	static Label getLabel(Labelable labelable, boolean bold)
	{
		Label label = new Label(labelable.getName());
		if (bold)
		{
			label.setStyle("-fx-font-weight: bold");
		}
		Tooltip.install(label, new Tooltip(labelable.getHelp()));
		return label;
	}
}
