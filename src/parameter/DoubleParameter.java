package parameter;

public class DoubleParameter extends NumericParameter<Double>
{
	public DoubleParameter(String name, String help, boolean interpolate, InterpolatedParameter<Double> time, Double... values)
	{
		this(name, help, interpolate, time, Validate.NONE, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, values);
	}

	public DoubleParameter(String name, String help, boolean interpolate, InterpolatedParameter<Double> time, Validate validate, double min, double max, Double... values)
	{
		super(name, help, interpolate, time, values);

		switch (validate)
		{
			case CLAMP:
				addListener((observableValue, aDouble, t1) ->
				{
					double validated = Math.max(min, Math.min(max, t1));
					if (!t1.equals(validated))
					{
						setValue(validated);
					}
				});
		}
	}

	@Override
	InterpolatedParameter<Double> getSimple()
	{
		return new DoubleParameter(getName(), getHelp(), false, null, getValue());
	}

	@Override
	Double interpolate(Double start, Double end, double time)
	{
		return start + (end - start) * time;
	}

	@Override
	Double parse(String string) throws NumberFormatException
	{
		return Double.parseDouble(string);
	}
}
