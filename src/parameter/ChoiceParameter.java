package parameter;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;

public class ChoiceParameter<T> extends Parameter<T>
{
	private ObservableList<T> choices;

	public ChoiceParameter(String name, String help, T... choices)
	{
		super(name, help, choices[0]);
		this.choices = FXCollections.observableArrayList(choices);
	}

	@Override
	public Node getControl()
	{
		ComboBox<T> comboBox = new ComboBox<>(choices);
		comboBox.getSelectionModel().select(getValue());
		bind(comboBox.getSelectionModel().selectedItemProperty());
		addListener((observableValue, t, t1) ->
		{
			if (!t1.equals(comboBox.getSelectionModel().getSelectedItem()))
			{
				comboBox.getSelectionModel().select(t1);
			}
		});
		return comboBox;
	}
}
