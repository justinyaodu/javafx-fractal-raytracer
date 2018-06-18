package render;

import javafx.scene.paint.Color;
import parameter.*;
import util.Vector3;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public abstract class Renderable
{
	DoubleParameter time = new DoubleParameter("Time", "The current time value used for rendering a frame of a sequence", false, null, NumericParameter.Validate.CLAMP, 0, 1, 0.0);
	DoubleParameter startTime = new DoubleParameter("Start Time", "The starting time value for the sequence", false, null, NumericParameter.Validate.CLAMP, 0, 1, 0.0);
	DoubleParameter endTime = new DoubleParameter("End Time", "The ending time value for the sequence", false, null, NumericParameter.Validate.CLAMP, 0, 1, 1.0);
	IntegerParameter frameCount = new IntegerParameter("Frame Count", "The number of frames to be rendered in the sequence", false, null, NumericParameter.Validate.CLAMP, 2, Integer.MAX_VALUE, 10);

	IntegerParameter imageWidth = new IntegerParameter("Image Width", "The width of the image to be rendered", false, null, NumericParameter.Validate.CLAMP, 1, Integer.MAX_VALUE, 640);
	IntegerParameter imageHeight = new IntegerParameter("Image Height", "The height of the image to be rendered", false, null, NumericParameter.Validate.CLAMP, 1, Integer.MAX_VALUE, 640);
	ColorParameter backgroundColor = new ColorParameter("Background Color", "The background color of the image", false, time, Color.WHITE);

	DoubleParameter cameraLongitude = new DoubleParameter("Longitude", "The rotation of the camera around the vertical axis, in degrees", false, time, -120.0);
	DoubleParameter cameraLatitude = new DoubleParameter("Latitude", "The angle of the camera measured from the horizontal plane, in degrees", false, time, 60.0);
	DoubleParameter cameraDistance = new DoubleParameter("Distance", "The distance of the camera plane from the origin", false, time, NumericParameter.Validate.CLAMP, 0, Double.POSITIVE_INFINITY, 2.0);

	DoubleParameter viewportWidth = new DoubleParameter("Viewport Width", "The width of the camera viewport, in world units", false, time, NumericParameter.Validate.CLAMP, 0, Double.POSITIVE_INFINITY, 4.0);
	DoubleParameter viewportOffsetX = new DoubleParameter("Viewport Offset X", "The horizontal offset of the centre of the camera viewport, in world units", false, time, 0.0);
	DoubleParameter viewportOffsetY = new DoubleParameter("Viewport Offset Y", "The vertical offset of the centre of the camera viewport, in world units", false, time, 0.0);

	DoubleParameter rayStepSize = new DoubleParameter("Ray Step Size", "The depth precision used for raytracing, in world units (smaller values are more precise)", false, time, NumericParameter.Validate.CLAMP, 0, Double.POSITIVE_INFINITY, 0.005);
	DoubleParameter rayLength = new DoubleParameter("Render Distance", "The distance that the camera can see, in world units", false, time, NumericParameter.Validate.CLAMP, 0, Double.POSITIVE_INFINITY, 4.0);

	IntegerParameter maxIterations = new IntegerParameter("Iterations", "The maximum number of function iterations used to render the fractal", false, time, NumericParameter.Validate.CLAMP, 1, Integer.MAX_VALUE, 12);

	ColorParameter colorCycle = new ColorParameter("Color Cycle", "The cycle of colors used to indicate distance of points from the camera", true, new DoubleParameter("Cycle Position", "The position around the color cycle", false, null, NumericParameter.Validate.CLAMP, 0, 1, 0.0), Color.web("F00"), Color.web("FF0"), Color.web("0F0"), Color.web("0FF"), Color.web("00F"), Color.web("F0F"), Color.web("F00"));

	{
		colorCycle.setForceInterpolation(true);
	}

	DoubleParameter colorCycleSize = new DoubleParameter("Color Cycle Size", "The change in distance, in world units, required to complete one color cycle", false, time, NumericParameter.Validate.CLAMP, 0.0, Double.MAX_VALUE, 2.0);
	DoubleParameter colorCycleOffset = new DoubleParameter("Color Cycle Offset", "The color cycle value used for a distance of zero", false, time, NumericParameter.Validate.CLAMP, 0, 1, 0.0);

	BooleanParameter shadingEnabled = new BooleanParameter("Shading Enabled", "Shade surfaces according to their angle relative to the camera", true);
	DoubleParameter brightnessOffset = new DoubleParameter("Brightness Offset", "The minimum brightness value for a surface", false, time, 0.0);
	DoubleParameter brightnessScalar = new DoubleParameter("Brightness Scalar", "The scalar to be applied to the calculated surface brightness value", false, time, 1.0);

	BooleanParameter ghostingEnabled = new BooleanParameter("Ghosting Enabled", "Show a translucent outline of lower iteration levels", true);
	DoubleParameter ghostingIntensity = new DoubleParameter("Ghosting Intensity", "The scalar to be applied to the calculated opacity of the ghost layers", false, time, NumericParameter.Validate.CLAMP, 0, Double.POSITIVE_INFINITY, 1.5);
	BooleanParameter ghostAllLayers = new BooleanParameter("Ghost Everything", "Make the entire shape translucent, including the surface of the highest calculated iteration level", false);

	IntegerParameter threadCount = new IntegerParameter("Thread Count", "Number of CPU threads used for rendering", false, null, NumericParameter.Validate.CLAMP, 1, 256, Runtime.getRuntime().availableProcessors());
	IntegerParameter threadSleepMs = new IntegerParameter("Thread Sleep", "Number of milliseconds between worker thread progress updates", false, null, NumericParameter.Validate.CLAMP, 10, 1000, 50);

	private List<ParameterGroup> parameterGroups = buildParameterGroups();

	private List<ParameterGroup> buildParameterGroups()
	{
		ParameterGroup sequenceParent = new ParameterGroup("Sequence Settings", "Settings for rendering sequences of images using interpolated values", time, startTime, endTime, frameCount);
		ParameterGroup imageParent = new ParameterGroup("Image Settings", "Settings for the image to be rendered", imageWidth, imageHeight, backgroundColor);
		ParameterGroup cameraPositionParent = new ParameterGroup("Camera Position", "Settings for the position and orientation of the camera in world space", cameraLongitude, cameraLatitude, cameraDistance);
		ParameterGroup cameraViewportParent = new ParameterGroup("Camera Viewport", "Settings for the camera viewport", viewportWidth, viewportOffsetX, viewportOffsetY);
		ParameterGroup raytraceParent = new ParameterGroup("Raytrace Settings", "Settings for raytracing precision and distance", rayStepSize, rayLength);
		ParameterGroup iterationParent = new ParameterGroup("Iteration Settings", "Settings for the number of iterations allowed for iterated shapes", maxIterations);
		ParameterGroup colorParent = new ParameterGroup("Color Settings", "Settings for coloring the shape based on each point's distance from the camera", colorCycle, colorCycleSize, colorCycleOffset);
		ParameterGroup shadingParent = new ParameterGroup("Shading Settings", "Settings for shading the shape", shadingEnabled, brightnessOffset, brightnessScalar);
		ParameterGroup ghostingParent = new ParameterGroup("Ghosting Settings", "Settings for the ghosting effect", ghostingEnabled, ghostingIntensity, ghostAllLayers);
		ParameterGroup performanceParent = new ParameterGroup("Performance Settings", "Settings for adjusting rendering performance", threadCount, threadSleepMs);

		return Arrays.asList(sequenceParent, imageParent, cameraPositionParent, cameraViewportParent, raytraceParent, iterationParent, colorParent, shadingParent, ghostingParent, performanceParent);
	}

	public List<ParameterGroup> getParameterGroups()
	{
		ArrayList<ParameterGroup> parameterGroups = new ArrayList<>(this.parameterGroups);
		Parameter[] customParameters = customParameters();
		if (customParameters.length > 0)
		{
			parameterGroups.add(new ParameterGroup(toString(), "Parameters specific to " + toString(), customParameters));
		}
		return parameterGroups;
	}

	Parameter[] customParameters()
	{
		return new Parameter[]{};
	}

	void cacheCustomParameters(double time)
	{
		//overridden by subclasses
	}

	abstract int compute(Vector3 position);

	@Override
	public String toString()
	{
		return getClass().getSimpleName();
	}

	public String getFilename()
	{
		return "jfrt_" + toString().toLowerCase() + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
	}
}
