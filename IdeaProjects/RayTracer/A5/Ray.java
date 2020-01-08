import java.util.ArrayList;
import java.util.Arrays;
import java.lang.Math;

public class Ray {
    float originVector [] = new float[3];
    float directionVector [] = new float[3];


    public Ray (float[] originVector, float[] directionVector) {
        this.originVector = originVector;
        this.directionVector = directionVector;
    }

    public boolean sphereInterctTest (float[] sphereCenter, float radius) {

        float directionVector[] = normalize(this.directionVector);
        float baseToCenter [] = subtract(sphereCenter, originVector);

        float v = dotProduct(baseToCenter, directionVector);
        float csq = dotProduct(baseToCenter, baseToCenter);

        float disc = radius*radius - (csq-(v*v));

        if (disc > 0) {
            return true;
        } else {
            return false;
        }
    }

    public float[] raySphereIntersectLocation (float[] sphereCenter, float radius) {
        float directionVector[] = normalize(this.directionVector);
        float baseToCenter [] = subtract(sphereCenter, originVector);

        float v = dotProduct(baseToCenter, directionVector);
        float csq = dotProduct(baseToCenter, baseToCenter);

        float disc = radius*radius - (csq-(v*v));

        if (disc > 0) {
            float d = (float)Math.sqrt(disc);
            float q[] = add(originVector, multiply((v-d),directionVector));
            return q;
        } else {
            return new float[] {-10000,-10000,-10000};
        }
    }

    /*
    Triangle in format:
    [ax, ay, az]
    [bx, by, bz]
    [cx, cy, cz]
     */

    public boolean rayTriangleIntersection (float[][] triangle) {
        float [] values = rayTriangleIntersectionHelper(triangle);
        if (values[0] >= 0 && values[1] >= 0 && (values[0]+values[1]) <= 1 && values[2]>0) {
            return true;
        } else {
            return false;
        }
    }

    public float [] rayTriangleIntersectionPoint (float [][] triangle) {
        float tval = rayTriangleIntersectionHelper(triangle)[2];
        //System.out.println("Tval = " + tval);
        return (add(originVector, multiply(tval, normalize(this.directionVector))));
    }

    public float[] rayTriangleIntersectionHelper (float[][] triangle) {
        float [] directionVector =normalize(this.directionVector);

        float[][] m = new float[][]{{triangle[0][0] - triangle[1][0],triangle[0][0]-triangle[2][0],directionVector[0]},
                {triangle[0][1] - triangle[1][1],triangle[0][1]-triangle[2][1],directionVector[1]},
                {triangle[0][2] - triangle[1][2],triangle[0][2]-triangle[2][2],directionVector[2]}};

        float [][] m1 = new float[][]{{triangle[0][0] - originVector[0],triangle[0][0]-triangle[2][0],directionVector[0]},
                {triangle[0][1] - originVector[1],triangle[0][1]-triangle[2][1],directionVector[1]},
                {triangle[0][2] - originVector[2],triangle[0][2]-triangle[2][2],directionVector[2]}};

        float [][] m2 = new float[][]{{triangle[0][0] - triangle[1][0],triangle[0][0]-originVector[0],directionVector[0]},
                {triangle[0][1] - triangle[1][1],triangle[0][1]-originVector[1],directionVector[1]},
                {triangle[0][2] - triangle[1][2],triangle[0][2]-originVector[2],directionVector[2]}};

        float [][] m3 = new float[][]{{triangle[0][0] - triangle[1][0],triangle[0][0]-triangle[2][0],triangle[0][0]-originVector[0]},
                {triangle[0][1] - triangle[1][1],triangle[0][1]-triangle[2][1],triangle[0][1]-originVector[1]},
                {triangle[0][2] - triangle[1][2],triangle[0][2]-triangle[2][2],triangle[0][2]-originVector[2]}};

//        System.out.println(Arrays.deepToString(m));
//        System.out.println(Arrays.deepToString(m1));
//        System.out.println(Arrays.deepToString(m2));
//        System.out.println(Arrays.deepToString(m3));

        float beta = matrixDeterminant(m1)/matrixDeterminant(m);
        float gamma = matrixDeterminant(m2)/matrixDeterminant(m);
        float tval = matrixDeterminant(m3)/matrixDeterminant(m);

//        System.out.println("b = " + beta + " y = " + gamma + " t = " + tval);

        return new float[] {beta,gamma,tval};
    }

    private float[] normalize (float[] vector) {
        float returnVector [] = new float[3];

        float popPop = (float)Math.sqrt(Math.pow(vector[0],2) + Math.pow(vector[1],2) + Math.pow(vector[2],2));

        for (int i = 0; i < 3; i++) {
            returnVector[i] = vector[i]/popPop;
        }
        return returnVector;
    }

    private float[] subtract (float[] vectorA, float[] vectorB) {
        float[] returningVector = new float[3];

        for (int i = 0; i < 3; i++)
            returningVector[i] = vectorA[i]-vectorB[i];

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

    /**
     Credit to Milan Vit for this Matrix Determinant code
     */
    public float matrixDeterminant (float[][] matrix) {
        float temporary[][];
        float result = 0;

        if (matrix.length == 1) {
            result = matrix[0][0];
            return (result);
        }

        if (matrix.length == 2) {
            result = ((matrix[0][0] * matrix[1][1]) - (matrix[0][1] * matrix[1][0]));
            return (result);
        }

        for (int i = 0; i < matrix[0].length; i++) {
            temporary = new float[matrix.length - 1][matrix[0].length - 1];

            for (int j = 1; j < matrix.length; j++) {
                for (int k = 0; k < matrix[0].length; k++) {
                    if (k < i) {
                        temporary[j - 1][k] = matrix[j][k];
                    } else if (k > i) {
                        temporary[j - 1][k - 1] = matrix[j][k];
                    }
                }
            }

            result += matrix[0][i] * Math.pow (-1, (double) i) * matrixDeterminant (temporary);
        }
        return (result);
    }
}
