public class Vector {
    float [] vector;
    public Vector (float [] vector) {
        this.vector = vector;
    }

    public void normalize () {

        float popPop = (float)Math.sqrt(Math.pow(vector[0],2) + Math.pow(vector[1],2) + Math.pow(vector[2],2));

        for (int i = 0; i < 3; i++) {
            vector[i] = vector[i]/popPop;
        }
    }

    public Vector subtract (Vector vectorB) {
        Vector returningVector = new Vector(new float[]{0,0,0});

        for (int i = 0; i < 3; i++)
            returningVector.vector[i] = vector[i]-vectorB.vector[i];

        return returningVector;
    }

    public Vector add (Vector vectorB) {
        Vector returningVector = new Vector(new float[]{0,0,0});

        for (int i = 0; i < 3; i++)
            returningVector.vector[i] = vector[i]+vectorB.vector[i];

        return returningVector;
    }

    public Vector multiply (float scalar) {
        Vector returning = new Vector(new float[]{});
        for (int i = 0; i < 3; i ++) {
            returning.vector[i] = scalar*vector[i];
        }
        return returning;
    }

    public float dotProduct (Vector vectorB) {
        float returningVector = 0;

        for (int i = 0; i < 3; i++) {
            returningVector = returningVector + vector[i] * vectorB.vector[i];
        }

        return returningVector;
    }
}
