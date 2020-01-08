import java.util.Arrays;

public class Sphere {
    float location [] = new float[3];
    float radius;
    float kAmbient [] = new float[3];
    float kDiffuse [] = new float[3];
    float kSpecular [] = new float[3];
    float kAttenuation [] = new float[3];
    float velocity [] = new float[3];
    boolean gravity;
    boolean wallBounce;
    boolean friction;

    public Sphere (float[] sphere) {
        for (int i = 0; i < 3; i ++) {
            location[i] = sphere[i];
            kAmbient[i] = sphere[i+4];
            kDiffuse[i] = sphere[i+7];
            kSpecular[i] = sphere[i+10];
            kAttenuation[i] = sphere[i+13];
            velocity[i] = sphere[i+16];
        }
        radius = sphere[3];
        if (sphere[19] == 1) {
            gravity = true;
        } else {
            gravity = false;
        }
        if (sphere[20] == 1) {
            wallBounce = true;
        } else {
            wallBounce = false;
        }
        if (sphere[21] == 1) {
            friction = true;
        } else {
            friction = false;
        }
    }

    public void print() {
        String printing = "Sphere Location: "+location[0]+","+location[1]+","+location[2]+
                " Radius: "+radius+
                " Ambient: "+kAmbient[0]+","+kAmbient[1]+","+kAmbient[2]+
                " Diffuse: "+kDiffuse[0]+","+kDiffuse[1]+","+kDiffuse[2]+
                " Specular: "+kSpecular[0]+","+kSpecular[1]+","+kSpecular[2]+
                " Attenuation: "+kAttenuation[0]+","+kAttenuation[1]+","+kAttenuation[2] +
                " Velocity: "+velocity[0]+","+velocity[1]+","+velocity[2] +
                " Gravity: "+gravity;
        System.out.println(printing);
    }

    public void physicsEngine() {
        if (gravity) {
            velocity[2] -= 0.65;
        }

        location[0] += velocity[0];
        location[1] += velocity[1];
        location[2] += velocity[2];

        if (location[2] < 0) {
            location[2] = 0;
            velocity[2] *= -1;
        }

        //Friction
//        if (friction) {
//            if (velocity[0] > 0) {
//                velocity[0] -= 0.05;
//            } else if (velocity[0] < 0) {
//                velocity[0] += 0.05;
//            }
//            if (velocity[1] > 0) {
//                velocity[1] -= 0.05;
//            } else if (velocity[1] < 0) {
//                velocity[1] += 0.05;
//            }
//        }

        //Wall bounce
        if (wallBounce) {
            if (location[0] > 20 || location[0] < -20) {
                velocity[0] *= -1;
            }
            if (location[1] > 20 || location[1] < -20) {
                velocity[1] *= -1;
            }
        }
    }

    public void collisionDetection (Sphere sphere) {
        if (Math.sqrt(Math.pow(((sphere.location[0] + sphere.velocity[0])-(location[0] + velocity[0])),2) + Math.pow(((sphere.location[1] + sphere.velocity[1])-(location[1] + velocity[1])),2)) <= (sphere.radius + radius)) {
            float[] thislocation = new float[3];
            thislocation[0] = location[0];
            thislocation[1] = location[1];
            float[] thatlocation = new float[3];
            thatlocation[0] = sphere.location[0];
            thatlocation[1] = sphere.location[1];

            float[] thisVelocity = new float[3];
            thisVelocity[0] = velocity[0];
            thisVelocity[1] = velocity[1];
            float[] thatVelocity = new float[3];
            thatVelocity[0] = sphere.velocity[0];
            thatVelocity[1] = sphere.velocity[1];

            float[] difference = subtract(thislocation, thatlocation);
            float[] velocityDifference = subtract(thisVelocity, thatVelocity);
            float cosine = 0;
            if ((magnitude(difference) * magnitude(velocityDifference)) > 0) {
                cosine = -1 * dotProduct(velocityDifference, difference) / (magnitude(difference) * magnitude(velocityDifference));
                cosine = Math.abs(cosine);
            }
            if (Math.sqrt(Math.pow((sphere.location[0]-location[0]),2) + Math.pow((sphere.location[1]-location[1]),2)) < (sphere.radius + radius)) {
                System.out.println("Cos: " + cosine);
            }
            velocity[0] += cosine * magnitude(thatVelocity) * (difference[0]) / magnitude(difference);
            velocity[1] += cosine * magnitude(thatVelocity) * (difference[1]) / magnitude(difference);
        }
    }

    private float magnitude (float[]vector) {
        return (float)Math.sqrt(Math.pow(vector[0],2) + Math.pow(vector[1],2) + Math.pow(vector[2],2));
    }

    private float[] subtract (float[] vectorA, float[] vectorB) {
        float[] returningVector = new float[3];

        for (int i = 0; i < 3; i++)
            returningVector[i] = vectorA[i]-vectorB[i];

        return returningVector;
    }

    private float dotProduct (float[] vectorA, float[] vectorB) {
        float returningVector = 0;

        for (int i = 0; i < 3; i++) {
            returningVector = returningVector + vectorA[i] * vectorB[i];
        }

        return returningVector;
    }
}