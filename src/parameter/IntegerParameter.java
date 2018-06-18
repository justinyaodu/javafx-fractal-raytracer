package parameter;

public class IntegerParameter extends NumericParameter<Integer>
{
	public IntegerParameter(String name, String help, boolean interpolate, InterpolatedParameter<Double> time, Integer... values)
	{
		this(name, help, interpolate, time, Validate.NONE, Integer.MIN_VALUE, Integer.MAX_VALUE, values);
	}

	public IntegerParameter(String name, String help, boolean interpolate, InterpolatedParameter<Double> time, Validate validate, int min, int max, Integer... values)
	{
		super(name, help, interpolate, time, values);

		switch (validate)
		{
			case CLAMP:
				addListener((observableValue, integer, t1) ->
				{
					int validated = Math.max(min, Math.min(max, t1));
					if (!t1.equals(validated))
					{
						setValue(validated);
					}
				});
		}
	}

	@Override
	InterpolatedParameter<Integer> getSimple()
	{
		return new IntegerParameter(getName(), getHelp(), false, null, getValue());
	}

	@Override
	Integer interpolate(Integer start, Integer end, double time)
	{
		int min = Math.min(start, end);
		int max = Math.max(start, end);
		return Math.max(min, Math.min(max, start + (int) ((end - start + 1) * time)));
	}

	@Override
	Integer parse(String string) throws NumberFormatException
	{
		return Integer.parseInt(string);
	}
}
