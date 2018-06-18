package util;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.io.File;

public class Utils
{
	public static double clamp(double d)
	{
		return Math.max(0, Math.min(1, d));
	}

	public static boolean saveImage(Image image, File file)
	{
		if (!file.getAbsolutePath().substring(file.getAbsolutePath().length() - 4).toLowerCase().equals(".png"))
		{
			file = new File(file.getAbsolutePath() + ".png");
		}

		try
		{
			ImageIO.write(SwingFXUtils.fromFXImage(image, null), "PNG", file);
		}
		catch (Exception e)
		{
			return false;
		}

		return true;
	}
}
