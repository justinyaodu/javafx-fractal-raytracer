package render;

import util.Vector3;

public class Cube extends Renderable
{
	public Cube()
	{
		cameraLatitude.setValue(20.0);
		cameraDistance.setValue(1.75);
		viewportWidth.setValue(3.5);
		maxIterations.setValue(1);
	}

	@Override
	int compute(Vector3 position)
	{
		return Math.max(Math.max(Math.abs(position.X), Math.abs(position.Y)), Math.abs(position.Z)) <= 1 ? 1 : 0;
	}
}
