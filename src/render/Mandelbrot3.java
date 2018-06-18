package render;

import parameter.DoubleParameter;
import parameter.NumericParameter;
import parameter.Parameter;
import util.Vector3;

public class Mandelbrot3 extends Renderable
{
	private int iterations;
	private double threshold;

	private DoubleParameter escapeThreshold = new DoubleParameter("Escape Threshold", "The maximum allowed distance for a point from the origin", false, time, NumericParameter.Validate.CLAMP, 0, Double.POSITIVE_INFINITY, 3.0);

	public Mandelbrot3()
	{
		cameraLongitude.setValue(-110.0);
		cameraLatitude.setValue(70.0);
		cameraDistance.setValue(3.0);
		viewportWidth.setValue(6.0);
		rayLength.setValue(6.0);
		maxIterations.setValue(12);
	}

	@Override
	Parameter[] customParameters()
	{
		return new Parameter[]{escapeThreshold};
	}

	@Override
	void cacheCustomParameters(double time)
	{
		iterations = maxIterations.getValue(time);
		threshold = escapeThreshold.getValue(time);
		threshold *= threshold;
	}

	@Override
	int compute(Vector3 position)
	{
		if (position.getMagnitudeSquared() > threshold)
		{
			return 0;
		}

		return compute(new Complex3(position.X, position.Y, position.Z));
	}

	private int compute(Complex3 z)
	{
		Complex3 c = z;

		for (int i = 0; i < iterations; i++)
		{
			if (z.squareMagnitude() > threshold)
			{
				return i;
			}

			z = z.square().add(c);
		}

		return iterations;
	}

	private static class Complex3
	{
		private final double R;
		private final double J;
		private final double K;

		private Complex3(double r, double j, double k)
		{
			R = r;
			J = j;
			K = k;
		}

		private Complex3 add(Complex3 other)
		{
			return new Complex3(R + other.R, J + other.J, K + other.K);
		}

		private Complex3 square()
		{
			return new Complex3(R * R - J * J, 2 * R * J - K * K, 2 * R * K);
		}

		private double squareMagnitude()
		{
			return R * R + J * J + K * K;
		}
	}
}
