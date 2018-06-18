package render;

import util.Vector3;

public class Mandelbrot4 extends Renderable
{
	private int iterations;

	public Mandelbrot4()
	{
		cameraDistance.setValue(2.0);
		maxIterations.setValue(10);
	}

	@Override
	void cacheCustomParameters(double time)
	{
		iterations = maxIterations.getValue(time);
	}

	@Override
	protected int compute(Vector3 position)
	{
		if (position.getMagnitudeSquared() > 4)
		{
			return 0;
		}

		double q = Math.sqrt(position.X * position.X + position.Y * position.Y);
		double b = position.X * position.Z / q;
		double d = position.Y * position.Z / q;

		return compute(new Complex4(position.X, b, position.Y, d));
	}

	private int compute(Complex4 z)
	{
		Complex4 c = z;

		for (int i = 0; i < iterations; i++)
		{
			if (z.squareMagnitude() > 4)
			{
				return i;
			}

			z = z.square().add(c);
		}

		return iterations;
	}

	private static class Complex4
	{
		final double A;
		final double B;
		final double C;
		final double D;

		Complex4(double a, double b, double c, double d)
		{
			A = a;
			B = b;
			C = c;
			D = d;
		}

		Complex4 square()
		{
			return new Complex4(A * A - 2 * B * D - C * C, 2 * (A * B - C * D), B * B + 2 * A * C - D * D, 2 * (A * D + B * C));
		}

		Complex4 add(Complex4 other)
		{
			return new Complex4(A + other.A, B + other.B, C + other.C, D + other.D);
		}

		double squareMagnitude()
		{
			return A * A + B * B + C * C + D * D;
		}

		@Override
		public String toString()
		{
			return String.format("(%.4f, %.4f, %.4f, %.4f)", A, B, C, D);
		}
	}
}
