package render;

import parameter.DoubleParameter;
import parameter.Parameter;
import util.Vector3;

public abstract class Escaper extends Renderable
{
	private int iterations;

	private DoubleParameter escapeThreshold = new DoubleParameter("Escape Threshold", "The maximum distance allowed distance for a point from the origin", false, time, 2.0);
	private double escape;

	@Override
	void cacheCustomParameters(double time)
	{
		iterations = maxIterations.getValue(time);
		escape = escapeThreshold.getValue(time);
		escape = escape * escape;
	}

	@Override
	Parameter[] customParameters()
	{
		return new Parameter[]{escapeThreshold};
	}

	@Override
	int compute(Vector3 position)
	{
		return compute(position, 0);
	}

	private int compute(Vector3 position, int iteration)
	{
		if (iteration == iterations || position.getMagnitudeSquared() > escape)
		{
			return iteration;
		}

		return compute(next(position), iteration + 1);
	}

	abstract Vector3 next(Vector3 current);
}
