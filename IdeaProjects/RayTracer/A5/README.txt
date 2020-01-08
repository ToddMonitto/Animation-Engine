This code is a raytracer project that I made for a computer graphics class. It
contains two drivers that create that generate two sphere-based animations.

This was a solo project for a class and lacks a lot of clean code. It is not easy
to read and lacks scalability, due to poor planning, but I am still proud of the
functionality and linear algebra I was able to implement.

Command to compile: javac Raytracer.java
Command to execute: java Raytracer AnimationDrivers/driver##.txt driver##.ppm

Once Executed, a series of ppm files will appear in the folder marked "output"
Type commands:
$ cd output
$ ffmpeg -framerate 15 -i driver##-%03d.ppm driver##.gif