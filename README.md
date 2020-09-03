# JavaFX Fractal Raytracer

A simple raytracing program which renders still images and animations of 3D fractals and other shapes. This program is written in Java and uses JavaFX for its GUI.

## Installation

Download the executable JAR file from [here](https://github.com/justinyaodu/javafx-fractal-raytracer/raw/master/jar/javafx-fractal-raytracer.jar). This program (unsurprisingly) requires Java installed to run.

To start the program, simply open the downloaded JAR file, or execute `java -jar javafx-fractal-raytracer.jar` from the command line.

## Screenshots and Animations

![Screenshot of application with Mandelbrot4 rendered](media/screenshot/screenshot.png)

![Mandelbrot3 with shading](media/gif/jfrt_mandelbrot3_solid.gif) ![Mandelbrot3 ghosted](media/gif/jfrt_mandelbrot3_ghost.gif)

![Menger sponge zoom](media/gif/jfrt_mengersponge_zoom.gif) ![Tangent bubbles at increasing escape thresholds](media/gif/jfrt_tangentbubbles.gif)

_Generated with [apngasm](https://github.com/apngasm/apngasm) and [ffmpeg](https://www.ffmpeg.org/). 1080p videos available for download [here](media/mp4/)._

## Features

### Implemented

* Camera positioning, zooming, movement
* Customizable color palette
* Keyframe-based animation system
* A variety of fractals and other interesting shapes to explore
* Lots of configurable parameters
* Shading and ghosting effects
* Multithreading support

### Todo

* Animated help graphics
* Help and about
* Code documentation
* More fun shapes!

### Out of Scope (for this project)

* GPU acceleration
* Direct export of animated image or video files ([apngasm](https://github.com/apngasm/apngasm) and [ffmpeg](https://www.ffmpeg.org/) can help with creating these from the exported PNG files)

## License

This software is licensed under the [MIT License](LICENSE).
