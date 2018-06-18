package render;

import parameter.DoubleParameter;
import parameter.Parameter;
import util.Vector3;

public class Julia extends Renderable
{
	private DoubleParameter verticalDistance = new DoubleParameter("Vertical Position", "The vertical position expressed as a value from 0 to 1, which is later scaled to range from -2 to 2", false, null, 0.0);

	private DoubleParameter real = new DoubleParameter("Real Offset", "The real part of the offset c used to compute the Julia set at a certain vertical position", true, verticalDistance, 0.0);
	private DoubleParameter imaginary = new DoubleParameter("Imaginary Offset", "The imaginary part of the offset c used to compute the Julia set at a certain vertical position", true, verticalDistance, 0.0);

	{
		Object realDummy = real.getKeyframe(0);
		real.addKeyframe(-2, -4.0);
		real.addKeyframe(2, 4.0);
		real.removeKeyframe(realDummy);

		Object imaginaryDummy = imaginary.getKeyframe(0);
		imaginary.addKeyframe(-2, -1.0);
		imaginary.addKeyframe(2, 3.0);
		imaginary.removeKeyframe(imaginaryDummy);
	}

	private int iterations;

	public Julia()
	{
		cameraDistance.setValue(3.0);
		viewportWidth.setValue(6.0);
		rayLength.setValue(6.0);
		maxIterations.setValue(10);
	}

	@Override
	void cacheCustomParameters(double time)
	{
		iterations = maxIterations.getValue(time);
	}

	@Override
	Parameter[] customParameters()
	{
		return new Parameter[]{real, imaginary};
	}

	@Override
	int compute(Vector3 position)
	{
		//		if (Math.abs(position.Z) > 2 || position.X * position.X + position.Y * position.Y > 4)
		//		{
		//			return 0;
		//		}

		Complex z = new Complex(position.X, position.Y);
		Complex c = new Complex(real.getValue(position.Z/* * 0.25 + 0.5*/), imaginary.getValue(position.Z/* * 0.25 + 0.5*/));
		return compute(z, c);
	}

	private int compute(Complex z, Complex c)
	{
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

	private static class Complex
	{
		private final double R;
		private final double I;

		private Complex(double r, double i)
		{
			R = r;
			I = i;
		}

		private Complex add(Complex other)
		{
			return new Complex(R + other.R, I + other.I);
		}

		private Complex square()
		{
			return new Complex(R * R - I * I, 2 * R * I);
		}

		private double squareMagnitude()
		{
			return R * R + I * I;
		}
	}
}