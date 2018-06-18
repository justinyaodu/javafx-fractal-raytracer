package parameter;

import gui.Labelable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ParameterGroup implements Labelable
{
	private String name;
	private String help;
	private List<Parameter> parameters;

	public ParameterGroup(String name, String help, Parameter... parameters)
	{
		this.name = name;
		this.help = help;
		this.parameters = Collections.unmodifiableList(Arrays.asList(parameters));
	}

	public String getName()
	{
		return name;
	}

	public String getHelp()
	{
		return help;
	}

	public List<Parameter> getParameters()
	{
		return parameters;
	}
}
