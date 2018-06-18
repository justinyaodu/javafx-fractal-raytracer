package render;

import util.Vector3;

public class TangentBubbles extends Escaper
{
	public TangentBubbles()
	{
		maxIterations.setValue(5);
	}

	@Override
	Vector3 next(Vector3 current)
	{
		return new Vector3(Math.tan(current.X + current.Y), Math.tan(current.Y + current.Z), Math.tan(current.Z + current.X));
	}
}
