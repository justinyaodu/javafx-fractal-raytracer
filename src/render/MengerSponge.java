package render;

import util.Vector3;

public class MengerSponge extends Cube
{
	private int iterations;

	public MengerSponge()
	{
		maxIterations.setValue(5);
		ghostingEnabled.setValue(false);
	}

	@Override
	void cacheCustomParameters(double time)
	{
		iterations = maxIterations.getValue(time);
	}

	@Override
	int compute(Vector3 position)
	{
		if (super.compute(position) == 0)
		{
			return 0;
		}

		position = new Vector3(Math.abs(position.X), Math.abs(position.Y), Math.abs(position.Z));

		return compute(position, 1);
	}

	private int compute(Vector3 position, int iteration)
	{
		if (iteration == iterations)
		{
			return iteration;
		}

		position = new Vector3(Math.abs(position.X), Math.abs(position.Y), Math.abs(position.Z));

		double nextX;
		double nextY;
		double nextZ;

		int small = 0;

		if (Math.abs(position.X) < 1.0 / 3)
		{
			nextX = position.X * 3;
			small++;
		}
		else
		{
			nextX = position.X * 3 - 2;
		}

		if (Math.abs(position.Y) < 1.0 / 3)
		{
			nextY = position.Y * 3;
			small++;
		}
		else
		{
			nextY = position.Y * 3 - 2;
		}

		if (Math.abs(position.Z) < 1.0 / 3)
		{
			nextZ = position.Z * 3;
			small++;
		}
		else
		{
			nextZ = position.Z * 3 - 2;
		}

		if (small >= 2)
		{
			return iteration;
		}

		return compute(new Vector3(nextX, nextY, nextZ), iteration + 1);
	}
}
