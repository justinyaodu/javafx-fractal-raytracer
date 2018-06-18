package util;

public class Vector3
{
	public final double X;
	public final double Y;
	public final double Z;

	public Vector3()
	{
		this(0, 0, 0);
	}

	public Vector3(double x, double y, double z)
	{
		X = x;
		Y = y;
		Z = z;
	}

	public Vector3 add(Vector3 other)
	{
		return new Vector3(X + other.X, Y + other.Y, Z + other.Z);
	}

	public Vector3 subtract(Vector3 other)
	{
		return add(other.scale(-1));
	}

	public Vector3 scale(double d)
	{
		return new Vector3(X * d, Y * d, Z * d);
	}

	public Vector3 normalized()
	{
		return scale(1 / getMagnitude());
	}

	public double dot(Vector3 other)
	{
		return X * other.X + Y * other.Y + Z * other.Z;
	}

	public Vector3 cross(Vector3 other)
	{
		return new Vector3(Y * other.Z - Z * other.Y, Z * other.X - X * other.Z, X * other.Y - Y * other.X);
	}

	public double getMagnitude()
	{
		return Math.sqrt(getMagnitudeSquared());
	}

	public double getMagnitudeSquared()
	{
		return X * X + Y * Y + Z * Z;
	}

	@Override
	public String toString()
	{
		return String.format("<%.8f, %.8f, %.8f>", X, Y, Z);
	}
}