import java.util.ArrayList;
import java.util.Arrays;

public class Camera {
    float eye[];
    float look[];
    float up[];
    float near;
    float bounds [];
    float res [];
    int recursionlevel;

    public Camera(float eye[], float look[], float up[], float d, float bounds[], float res [], int recursionlevel) {
        this.eye = eye; this.look = look; this.up = up; this.near = d; this.bounds = bounds; this.res = res; this.recursionlevel = recursionlevel;
    }

    public void print () {
        String printing = "Camera Eye: "+eye[0]+","+eye[1]+","+eye[2]+
                " Look: "+look[0]+","+look[1]+","+look[2]+
                " Up: "+up[0]+","+up[1]+","+up[2]+
                " Near clipping: "+near+
                " Bounds: left: "+bounds[0]+",Right:"+bounds[1]+",Bottom:"+bounds[2]+",Top:"+bounds[3]+
                " Res: width: "+res[0]+", height:"+res[1];
        System.out.println(printing);
    }

    public String sendRays (ArrayList<Sphere> spheres, ArrayList<Model> models, float[] ambient, ArrayList<Light> lights) {
        //res = width, height
        //bounds = left, right, bottom, top
        float WV[] = normalize(subtract(eye,look));
        float UV[] = normalize(crossProduct(up, WV));
        float VV[] = crossProduct(WV,UV);

        ArrayList<Ray> intersectingRays = new ArrayList<Ray>();

        String output = "";

        for (int j = (int)res[0]-1; j >= 0; j--) {
            //System.out.println("Remaining " + j);
            for (int i = 0; i < res[1]; i++) {
                boolean background = false;

                float px = i/(res[0]-1)*(bounds[1]-bounds[0])+bounds[0];
                float py = j/(res[1]-1)*(bounds[3]-bounds[2])+bounds[2];
                float partOne[] = multiply(near, WV);
                float partTwo[] = multiply(px,UV);
                float partThree[] = multiply(py,VV);

                float pixpt[] = add(eye, partOne);
                pixpt = add(pixpt, partTwo);
                pixpt = add(pixpt, partThree);

                Ray ray = new Ray(eye,subtract(pixpt, eye));
                float red = 0;
                float green = 0;
                float blue = 0;
                for(Sphere sphere: spheres) {
                    boolean intersectTest = false;
                    float distanceCompare = distance(eye, sphere.location);
                    for (Sphere sphere1: spheres) {
                        if (sphere1 == sphere) {
                        } else if (ray.sphereInterctTest(sphere1.location, sphere1.radius) && (distance(eye,sphere1.location) < distanceCompare)){
                            intersectTest = true;
                        }
                    }
                    if (ray.sphereInterctTest(sphere.location,sphere.radius) && !intersectTest) {
                        background = true;
                       red = (ambient[0]*sphere.kAmbient[0]);
                       green = (ambient[1]*sphere.kAmbient[1]);
                       blue = (ambient[2]*sphere.kAmbient[2]);
                       //System.out.println(red + " " + green + " " + blue + " ");

                        boolean reflectionComplete = false;
                       for (Light light: lights) {
                           float [] intersectPoint = ray.raySphereIntersectLocation(sphere.location,sphere.radius);
                           Ray lightRay = new Ray(light.location, subtract(intersectPoint, light.location));

                           float [] surfaceNormal = normalize(subtract(intersectPoint, sphere.location));
                           float [] lightOut = subtract(lightRay.originVector, intersectPoint);
                           float angleCosine = dotProduct(lightOut,surfaceNormal)/(magnitude(lightOut) * magnitude(surfaceNormal));
                           boolean lightIntersectTest = false;

                           distanceCompare = distance(lightRay.originVector, sphere.location);

                           for (Sphere sphere1: spheres) {
                               if (sphere1 == sphere) {
                               } else if (lightRay.sphereInterctTest(sphere1.location, sphere1.radius) && distance(lightRay.originVector, sphere1.location) < distanceCompare){
                                   lightIntersectTest = true;
                               }
                           }

                           if (angleCosine > 0 && lightIntersectTest == false) {
                               red += (light.color[0]*sphere.kDiffuse[0]*angleCosine);
                               green += (light.color[1]*sphere.kDiffuse[1]*angleCosine);
                               blue += (light.color[2]*sphere.kDiffuse[2]*angleCosine);
                           }

                           lightOut = normalize(lightOut);
                           float [] reflector = normalize(subtract(multiply((2*dotProduct(lightOut, surfaceNormal)),surfaceNormal),lightOut));
                           float [] V = normalize(subtract(eye,intersectPoint));
                           float reflectionCosign = (float)Math.pow(dotProduct(V, reflector)/(magnitude(reflector)*magnitude(V)),16);

                           //System.out.print(" reflector " + Arrays.toString(reflector));
                           //System.out.print(" V " + Arrays.toString(V));
                           if (reflectionCosign > 0 && lightIntersectTest == false) {
                               red += (reflectionCosign * light.color[0] * sphere.kSpecular[0]);
                               green += (reflectionCosign * light.color[1] * sphere.kSpecular[1]);
                               blue += (reflectionCosign * light.color[2] * sphere.kSpecular[2]);

                               float [] VR = subtract(multiply((2*dotProduct(V, surfaceNormal)),surfaceNormal),V);
                               Ray reflectionRay = new Ray(intersectPoint, VR);

                               if (!reflectionComplete) {
                                   float[] reflection = recursiveRay(sphere, reflectionRay, spheres, models, ambient, lights, recursionlevel);
                                   red += (reflection[0] * sphere.kAttenuation[0]);
                                   green += (reflection[1] * sphere.kAttenuation[1]);
                                   blue += (reflection[2] * sphere.kAttenuation[2]);
                                   reflectionComplete = true;
                               }
                           }
                       }
                    }
                }
                //MODELS
                for (Model model: models) {
                    boolean intersectTest = false;
                    for (Sphere sphere1: spheres) {
                        if (ray.sphereInterctTest(sphere1.location, sphere1.radius)){
                            intersectTest = true;
                        }
                    }

                    /*
                    Triangle in format:
                    [ax, ay, az]
                    [bx, by, bz]
                    [cx, cy, cz]

                    Triangle normal format:
                    [anx, any, anz]
                    [bnx, bny, bnz]
                    [cnx, cny, cnz]
                    */

                    //TODO: ITERATE LIGHTS
                    for (String[] face: model.faces) {
                        float [][] vertexes = new float[3][3];
                        float [][] vertexNormals = new float[3][3];
                        int materialNumber = -1;

                        for (int material = 0; material < model.materials.size(); material++) {
                            if (face[3].equals(model.materials.get(material).name)) {
                                materialNumber = material;
                            }
                        }

                        if (materialNumber == -1) {
                            System.out.println("ERROR: MATERIAL NOT FOUND FOR FACE " + Arrays.toString(face) + " WITH MATERIAL NAME " + face[4]);
                        }

                        for (int vertex = 0; vertex < 3; vertex++) {
                            String [] separatedVertex = face[vertex].split("//");
                            for (String string: separatedVertex) {
                                vertexes[vertex] = (model.vertices.get(Integer.valueOf(separatedVertex[0])-1));
                                vertexNormals[vertex] = (model.vertexNormals.get(Integer.valueOf(separatedVertex[1])-1));
                            }
                        }

                        //TODO: Other Model intersect test

                        if (ray.rayTriangleIntersection(vertexes) && !intersectTest) {
                            //System.out.println("Checkpoint A");
                            //System.out.println("Ka: " + Arrays.toString(model.materials.get(materialNumber).ka) + "Ks: " + Arrays.toString(model.materials.get(materialNumber).ks) + "Kd: " + Arrays.toString(model.materials.get(materialNumber).kd));
                            //System.out.println("Vertices: " + Arrays.deepToString(vertexes));
                            //System.out.println("Vertex Normals: " + Arrays.deepToString(vertexNormals));
                            red += (ambient[0] * model.materials.get(materialNumber).ka[0]);
                            green += (ambient[1] * model.materials.get(materialNumber).ka[1]);
                            blue += (ambient[2] * model.materials.get(materialNumber).ka[2]);

                            boolean reflectionComplete = false;
                            for (Light light: lights) {

                                float [] intersectPoint = ray.rayTriangleIntersectionPoint(vertexes);
                                Ray lightRay = new Ray(light.location, subtract(ray.rayTriangleIntersectionPoint(vertexes), light.location));

                                float [] lightOut = subtract(lightRay.originVector, intersectPoint);
                                float angleCosine = dotProduct(lightOut,vertexNormals[0])/(magnitude(lightOut));
                                boolean lightIntersectTest = false;

                                for (Sphere sphere1: spheres) {
                                    if (lightRay.sphereInterctTest(sphere1.location, sphere1.radius)){
                                        lightIntersectTest = true;
                                    }
                                }

                                if (angleCosine > 0 && lightIntersectTest == false) {
                                    //System.out.println("Checkpoint D");
                                    //System.out.println("cos: " + angleCosine);
                                    //System.out.println("KD: " + Arrays.toString(model.materials.get(materialNumber).kd));
                                    red += (light.color[0]*model.materials.get(materialNumber).kd[0])*angleCosine;
                                    green += (light.color[1]*model.materials.get(materialNumber).kd[1])*angleCosine;
                                    blue += (light.color[2]*model.materials.get(materialNumber).kd[2])*angleCosine;
                                }

                                lightOut = normalize(lightOut);
                                float [] reflector = normalize(subtract(multiply((2*dotProduct(lightOut, vertexNormals[0])),vertexNormals[0]),lightOut));
                                float [] V = normalize(subtract(eye,intersectPoint));
                                float reflectionCosign = (float)Math.pow(dotProduct(V, reflector),model.materials.get(materialNumber).Ns);
//                                System.out.println("reflector = " + Arrays.toString(reflector));
//                                System.out.println("dot: " + dotProduct(V, reflector));
//                                System.out.println("mag: " + magnitude(reflector));
//                                System.out.println("V = " + Arrays.toString( V));
//                                System.out.println("Cos = " + reflectionCosign);
                                //System.out.println("Material: " + materialNumber);

                                //System.out.print(" reflector " + Arrays.toString(reflector));
                                //System.out.print(" V " + Arrays.toString(V));
                                if (reflectionCosign > 0 && lightIntersectTest == false) {
                                    //System.out.println("Checkpoint S");
                                    //System.out.println("Pre-S: " + red + ", " + green + ", " + blue + " Reflection cosign = " + reflectionCosign + " Intersect point: " + Arrays.toString(intersectPoint));

                                    red += (reflectionCosign * light.color[0] * model.materials.get(materialNumber).ks[0]);
                                    green += (reflectionCosign * light.color[1] * model.materials.get(materialNumber).ks[0]);
                                    blue += (reflectionCosign * light.color[2] * model.materials.get(materialNumber).ks[0]);
                                    //System.out.println("Post-S: " + red + ", " + green + ", " + blue);


                                    float [] VR = subtract(multiply((2*dotProduct(V, vertexNormals[0])),vertexNormals[0]),V);
                                    Ray reflectionRay = new Ray(intersectPoint, VR);

                                    if (!reflectionComplete) {
                                        float[] reflection = recursiveRay(null,reflectionRay, spheres, models, ambient, lights, recursionlevel);
                                        red += reflection[0]  * model.materials.get(materialNumber).ks[0];
                                        green += reflection[1]  * model.materials.get(materialNumber).ks[1];
                                        blue += reflection[2]  * model.materials.get(materialNumber).ks[2];
                                        reflectionComplete = true;
                                    }
                                }
                            }
                        }
                    }

                }
                red = red*255;
                green = green*255;
                blue = blue*255;

                if (red > 255)
                    red = 255;
                if (green > 255)
                    green = 255;
                if (blue > 255)
                    blue = 255;

                if (!background) {
                    green = 153;
                }

                output += (int)red + " " + (int)green + " " + (int)blue + " ";
            }
            output += "\n";
        }
        return output;
    }


    private float [] recursiveRay (Sphere reflectingSphere, Ray ray, ArrayList<Sphere> spheres, ArrayList<Model> models, float[] ambient, ArrayList<Light> lights, int recursionlevel) {
        float RGB [] = new float[]{0,0,0};

        if (recursionlevel < 1) {
            return new float[]{0,0,0};
        }

        for(Sphere sphere: spheres) {
            boolean intersectTest = false;
            float distanceCompare = distance(ray.originVector, sphere.location);
            for (Sphere sphere1: spheres) {
                        if (sphere1 == sphere) {
                        } else if (ray.sphereInterctTest(sphere1.location, sphere1.radius) && distance(ray.originVector,sphere1.location) < distanceCompare){
                            intersectTest = true;
                        }
            }
            if (ray.sphereInterctTest(sphere.location, sphere.radius) && !intersectTest && sphere != reflectingSphere) {
                RGB[0] = (ambient[0] * sphere.kAmbient[0]);
                RGB[1] = (ambient[1] * sphere.kAmbient[1]);
                RGB[2] = (ambient[2] * sphere.kAmbient[2]);
                //System.out.println(red + " " + green + " " + blue + " ");

                boolean reflectionComplete = false;
                for (Light light : lights) {
                    float[] intersectPoint = ray.raySphereIntersectLocation(sphere.location, sphere.radius);
                    Ray lightRay = new Ray(light.location, subtract(ray.raySphereIntersectLocation(sphere.location, sphere.radius), light.location));

                    float[] surfaceNormal = normalize(subtract(intersectPoint, sphere.location));
                    float[] lightOut = subtract(lightRay.originVector, intersectPoint);
                    float angleCosine = dotProduct(lightOut, surfaceNormal) / (magnitude(lightOut) * magnitude(surfaceNormal));
                    boolean lightIntersectTest = false;

                    distanceCompare = distance(lightRay.originVector, sphere.location);

                    for (Sphere sphere1: spheres) {
                               if (sphere1 == sphere) {
                               } else if (lightRay.sphereInterctTest(sphere1.location, sphere1.radius) && distance(lightRay.originVector, sphere1.location) < distanceCompare){
                                   lightIntersectTest = true;
                               }
                    }

                    if (angleCosine > 0 && lightIntersectTest == false) {
                        RGB[0] += (light.color[0] * sphere.kDiffuse[0]) * angleCosine;
                        RGB[1] += (light.color[1] * sphere.kDiffuse[1]) * angleCosine;
                        RGB[2] += (light.color[2] * sphere.kDiffuse[2]) * angleCosine;
                    }

                    lightOut = normalize(lightOut);
                    float[] reflector = normalize(subtract(multiply((2 * dotProduct(lightOut, surfaceNormal)), surfaceNormal), lightOut));
                    float[] V = normalize(subtract(ray.originVector, intersectPoint));
                    float reflectionCosign = (float) Math.pow(dotProduct(V, reflector) / (magnitude(reflector) * magnitude(V)), 16);

                    //System.out.print(" reflector " + Arrays.toString(reflector));
                    //System.out.print(" V " + Arrays.toString(V));
                    if (reflectionCosign > 0 && lightIntersectTest == false) {
                        RGB[0] += (reflectionCosign * light.color[0] * sphere.kSpecular[0]);
                        RGB[1] += (reflectionCosign * light.color[1] * sphere.kSpecular[1]);
                        RGB[2] += (reflectionCosign * light.color[2] * sphere.kSpecular[2]);

                        float[] VR = subtract(multiply((2 * dotProduct(V, surfaceNormal)), surfaceNormal), V);
                        Ray reflectionRay = new Ray(intersectPoint, VR);

                        if (!reflectionComplete) {
                            float[] reflection = recursiveRay(sphere, reflectionRay, spheres, models, ambient, lights, recursionlevel-1);
                            RGB[0] += reflection[0]* sphere.kAttenuation[0];
                            RGB[1] += reflection[1]* sphere.kAttenuation[1];
                            RGB[2] += reflection[2]* sphere.kAttenuation[2];
                            reflectionComplete = true;
                        }
                    }
                }
            } else {
            }
        }
            for (Model model: models) {
                boolean intersectTest = false;
                for (Sphere sphere1: spheres) {
                    if (ray.sphereInterctTest(sphere1.location, sphere1.radius)){
                        intersectTest = true;
                    }
                }

                //TODO: ITERATE LIGHTS
                for (String[] face: model.faces) {
                    float [][] vertexes = new float[3][3];
                    float [][] vertexNormals = new float[3][3];
                    int materialNumber = -1;

                    for (int material = 0; material < model.materials.size(); material++) {
                        if (face[3].equals(model.materials.get(material).name)) {
                            materialNumber = material;
                        }
                    }

                    if (materialNumber == -1) {
                        System.out.println("ERROR: MATERIAL NOT FOUND FOR FACE " + Arrays.toString(face) + " WITH MATERIAL NAME " + face[4]);
                    }

                    for (int vertex = 0; vertex < 3; vertex++) {
                        String [] separatedVertex = face[vertex].split("//");
                        for (String string: separatedVertex) {
                            vertexes[vertex] = (model.vertices.get(Integer.valueOf(separatedVertex[0])-1));
                            vertexNormals[vertex] = (model.vertexNormals.get(Integer.valueOf(separatedVertex[1])-1));
                        }
                    }

                    //TODO: Other Model intersect test

                    if (ray.rayTriangleIntersection(vertexes) && !intersectTest) {
                        //System.out.println("Checkpoint A");
                        //System.out.println("Ka: " + Arrays.toString(model.materials.get(materialNumber).ka) + "Ks: " + Arrays.toString(model.materials.get(materialNumber).ks) + "Kd: " + Arrays.toString(model.materials.get(materialNumber).kd));
                        //System.out.println("Vertices: " + Arrays.deepToString(vertexes));
                        //System.out.println("Vertex Normals: " + Arrays.deepToString(vertexNormals));
                        RGB[0] += (ambient[0] * model.materials.get(materialNumber).ka[0]);
                        RGB[1] += (ambient[1] * model.materials.get(materialNumber).ka[1]);
                        RGB[2] += (ambient[2] * model.materials.get(materialNumber).ka[2]);
                        boolean reflectionComplete = false;
                        for (Light light: lights) {

                            float [] intersectPoint = ray.rayTriangleIntersectionPoint(vertexes);
                            Ray lightRay = new Ray(light.location, subtract(ray.rayTriangleIntersectionPoint(vertexes), light.location));

                            float [] lightOut = subtract(lightRay.originVector, intersectPoint);
                            float angleCosine = dotProduct(lightOut,vertexNormals[0])/(magnitude(lightOut));
                            boolean lightIntersectTest = false;

                            for (Sphere sphere1: spheres) {
                                if (lightRay.sphereInterctTest(sphere1.location, sphere1.radius)){
                                    lightIntersectTest = true;
                                }
                            }

                            if (angleCosine > 0 && lightIntersectTest == false) {
                                //System.out.println("Checkpoint D");
                                //System.out.println("cos: " + angleCosine);
                                //System.out.println("KD: " + Arrays.toString(model.materials.get(materialNumber).kd));
                                RGB[0] += (light.color[0]*model.materials.get(materialNumber).kd[0])*angleCosine;
                                RGB[1] += (light.color[1]*model.materials.get(materialNumber).kd[1])*angleCosine;
                                RGB[2] += (light.color[2]*model.materials.get(materialNumber).kd[2])*angleCosine;
                            }

                            lightOut = normalize(lightOut);
                            float [] reflector = normalize(subtract(multiply((2*dotProduct(lightOut, vertexNormals[0])),vertexNormals[0]),lightOut));
                            float [] V = normalize(subtract(ray.originVector,intersectPoint));
                            float reflectionCosign = (float)Math.pow(dotProduct(V, reflector),model.materials.get(materialNumber).Ns);
//                                System.out.println("reflector = " + Arrays.toString(reflector));
//                                System.out.println("dot: " + dotProduct(V, reflector));
//                                System.out.println("mag: " + magnitude(reflector));
//                                System.out.println("V = " + Arrays.toString( V));
//                                System.out.println("Cos = " + reflectionCosign);
                            //System.out.println("Material: " + materialNumber);

                            //System.out.print(" reflector " + Arrays.toString(reflector));
                            //System.out.print(" V " + Arrays.toString(V));
                            if (reflectionCosign > 0 && lightIntersectTest == false) {
                                //System.out.println("Checkpoint S");
                                //System.out.println("Pre-S: " + red + ", " + green + ", " + blue + " Reflection cosign = " + reflectionCosign + " Intersect point: " + Arrays.toString(intersectPoint));

                                RGB[0] += (reflectionCosign * light.color[0] * model.materials.get(materialNumber).ks[0]);
                                RGB[1] += (reflectionCosign * light.color[1] * model.materials.get(materialNumber).ks[0]);
                                RGB[2] += (reflectionCosign * light.color[2] * model.materials.get(materialNumber).ks[0]);
                                //System.out.println("Post-S: " + red + ", " + green + ", " + blue);


                                float [] VR = subtract(multiply((2*dotProduct(V, vertexNormals[0])),vertexNormals[0]),V);
                                Ray reflectionRay = new Ray(intersectPoint, VR);

                                if (!reflectionComplete) {
                                    float[] reflection = recursiveRay(null, reflectionRay, spheres, models, ambient, lights, recursionlevel-1);
                                    RGB[0] += reflection[0] * model.materials.get(materialNumber).ks[0];
                                    RGB[1] += reflection[1] * model.materials.get(materialNumber).ks[1];
                                    RGB[2] += reflection[2] * model.materials.get(materialNumber).ks[2];
                                    reflectionComplete = true;
                                }
                            }
                        }
                    }
                }

            }

        return RGB;
    }

    private float[] subtract (float[] vectorA, float[] vectorB) {
        float[] returningVector = new float[3];

        for (int i = 0; i < 3; i++)
            returningVector[i] = vectorA[i]-vectorB[i];

        return returningVector;
    }

    private float[] normalize (float[] vector) {
        float returnVector [] = new float[3];

        float popPop = (float)Math.sqrt(Math.pow(vector[0],2) + Math.pow(vector[1],2) + Math.pow(vector[2],2));

        for (int i = 0; i < 3; i++) {
            returnVector[i] = vector[i]/popPop;
        }
        return returnVector;
    }

    private static float [] crossProduct (float [] vectorA, float[] vectorB) {
        float returningVector [] = new float[3];

        returningVector[0] = vectorA[1] * vectorB[2] - vectorA[2] * vectorB[1];
        returningVector[1] = vectorA[2] * vectorB[0] - vectorA[0] * vectorB[2];
        returningVector[2] = vectorA[0] * vectorB[1] - vectorA[1] * vectorB[0];

        return returningVector;
    }

    private float[] add (float[] vectorA, float[] vectorB) {
        float[] returningVector = new float[3];

        for (int i = 0; i < 3; i++)
            returningVector[i] = vectorA[i]+vectorB[i];

        return returningVector;
    }

    private float[] multiply (float scalar, float[] vector) {
        float[] returning = new float[3];
        for (int i = 0; i < 3; i ++) {
            returning[i] = scalar*vector[i];
        }
        return returning;
    }

    private float dotProduct (float[] vectorA, float[] vectorB) {
        float returningVector = 0;

        for (int i = 0; i < 3; i++) {
            returningVector = returningVector + vectorA[i] * vectorB[i];
        }

        return returningVector;
    }

    private float magnitude (float[]vector) {
        return (float)Math.sqrt(Math.pow(vector[0],2) + Math.pow(vector[1],2) + Math.pow(vector[2],2));
    }

    private float distance (float[] vectorA, float[] vectorB) {
        float returning = 0;
        returning = (float)Math.sqrt(Math.pow((vectorA[0]-vectorB[0]),2) + Math.pow((vectorA[1]-vectorB[1]),2) + Math.pow((vectorA[2]-vectorB[2]),2));
        return returning;
    }
}
