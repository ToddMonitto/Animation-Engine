import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;

public class Raytracer {

    public static void main(String[] args) {
        float eye[] = new float[3];
        float look[] = new float[3];
        float up[] = new float[3];
        float d = 0;
        float bounds [] = new float[4];
        float res [] = new float[2];
        int recursionlevel = 0;

        float ambient[] = new float[3];

        ArrayList<Light> lights = new ArrayList<Light>();
        ArrayList<Sphere> spheres = new ArrayList<Sphere>();
        ArrayList<Model> models = new ArrayList<Model>();

        try {
            File file = new File(args[0]);
            Scanner sc = new Scanner(file);

            while (sc.hasNextLine()) {
                String[] thisLine = sc.nextLine().split("\\s+");
                switch (thisLine[0]) {
                    case "eye":
                        eye[0] = Float.valueOf(thisLine[1]);
                        eye[1] = Float.valueOf(thisLine[2]);
                        eye[2] = Float.valueOf(thisLine[3]);
                        break;
                    case "look":
                        look[0] = Float.valueOf(thisLine[1]);
                        look[1] = Float.valueOf(thisLine[2]);
                        look[2] = Float.valueOf(thisLine[3]);
                        break;
                    case "up":
                        up[0] = Float.valueOf(thisLine[1]);
                        up[1] = Float.valueOf(thisLine[2]);
                        up[2] = Float.valueOf(thisLine[3]);
                        break;
                    case "d":
                        d = Float.valueOf(thisLine[1]);
                        break;
                    case "bounds":
                        bounds[0] = Float.valueOf(thisLine[1]);
                        bounds[1] = Float.valueOf(thisLine[2]);
                        bounds[2] = Float.valueOf(thisLine[3]);
                        bounds[3] = Float.valueOf(thisLine[4]);
                        break;
                    case "res":
                        res[0] = Float.valueOf(thisLine[1]);
                        res[1] = Float.valueOf(thisLine[2]);
                        break;
                    case "ambient":
                        ambient[0] = Float.valueOf(thisLine[1]);
                        ambient[1] = Float.valueOf(thisLine[2]);
                        ambient[2] = Float.valueOf(thisLine[3]);
                        break;
                    case "light":
                        float [] light = new float[7];
                        for (int i = 0; i < 7; i ++) {
                            light[i] = Float.valueOf(thisLine[i+1]);
                        }
                        lights.add(new Light(light));
                        break;
                    case "sphere":
                        float [] sphere = new float[22];
                        for (int i = 0; i < 22; i ++) {
                            sphere[i] = Float.valueOf(thisLine[i+1]);
                        }
                        spheres.add(new Sphere(sphere));
                        break;
                    case "model":
                        String [] model = new String[9];
                        for (int i = 0; i < 9; i++) {
                            model[i] = thisLine[i+1];
                        }
                        models.add(new Model(model));
                        break;
                    case "recursionlevel":
                        recursionlevel = Integer.valueOf(thisLine[1]);
                        break;
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("First argument file not found");
        }

        Camera camera = new Camera(eye,look,up,d,bounds,res,recursionlevel);

        camera.print();

        Ray ray = new Ray(new float[]{0,0,0},new float[]{1,1,1});

        System.out.println(Arrays.toString(ray.rayTriangleIntersectionHelper(new float[][]{{3,0,0},
                {0,3,0},
                {0,0,3}})));
        System.out.println(ray.rayTriangleIntersection(new float[][]{{3,0,0},
                {0,3,0},
                {0,0,3}}));
        System.out.println(Arrays.toString(ray.rayTriangleIntersectionPoint(new float[][]{{3,0,0},
                {0,3,0},
                {0,0,3}})));


        for (int i = 0; i < spheres.size(); i++) {
            spheres.get(i).print();
        }

        for (int i = 0; i < lights.size(); i++) {
            lights.get(i).print();
        }

        for (int i = 0; i < models.size(); i++) {
            models.get(i).print();

        }
        for (int i = 0; i < 150; i ++) {

            String output = "P3" + "\n" + (int) res[0] + " " + (int) res[1] + " 255" + "\n";
            String outputName = "output" + File.separator + args[1] + "-" +  String.format("%03d", i) + ".ppm";

            output += camera.sendRays(spheres, models, ambient, lights);


            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputName));
                writer.write(output);
                writer.close();
//            BufferedWriter writer2 = new BufferedWriter(new FileWriter("drivertest.txt"));
//            writer2.write(output);
//            writer2.close();
            } catch (IOException ex) {
                System.out.println("Output file not found");
            }

            for (Sphere sphere: spheres) {
                sphere.physicsEngine();
                for (Sphere sphere1: spheres) {
                    if (sphere1 != sphere) {
                        sphere.collisionDetection(sphere1);
                    }
                }
            }
            System.out.println("Frame: " + i);
        }

    }
}
