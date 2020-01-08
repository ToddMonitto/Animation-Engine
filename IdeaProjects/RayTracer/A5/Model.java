import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.io.*;
import java.lang.Math;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;


public class Model {
    float wx;
    float wy;
    float wz;
    float theta;
    float scale;
    float tx;
    float ty;
    float tz;
    String name;

    ArrayList<float[]> vertices;
    ArrayList<String[]> faces;
    ArrayList<float[]> vertexNormals;

    ArrayList<Material> materials = new ArrayList<Material>();

    public Model (String [] model) {
        this.wx = Float.valueOf(model[0]);
        this.wy = Float.valueOf(model[1]);
        this.wz = Float.valueOf(model[2]);
        this.theta = Float.valueOf(model[3]);
        this.scale = Float.valueOf(model[4]);
        this.tx = Float.valueOf(model[5]);
        this.ty = Float.valueOf(model[6]);
        this.tz = Float.valueOf(model[7]);
        this.name = model[8];

        readObjVertices();
        readObjNormals();
        //normals();
        rotate();
        translateAndScale();
        readMtllib();
        readObjFaces();
    }

    public void print() {
        System.out.println("wx: " + this.wx + ", " + "wy: " + this.wy + ", " + "wz: " + this.wz + ", " + "thetz: " + this.theta + ", " + "scale: " + this.scale + ", " + "tx: " + this.tx + ", " + "ty: " + this.ty + ", " + "tz: " + this.tz);
//        for (String[] face: faces) {
//            System.out.print(Arrays.toString(face)+ " ");
//        }
        for (Material mat: materials) {
            mat.print();
        }
        System.out.println();
    }

    private void readObjVertices () {
        ArrayList<float[]> vertices = new ArrayList<float[]>();

        try {
            File file = new File(this.name);
            Scanner sc = new Scanner(file);
            while (sc.hasNextLine()) {
                String thisLine = sc.nextLine();
                String [] lineSplit = thisLine.split(" ");
                float vertex[] = new float[3];

                if (lineSplit[0].equals("v")) {
                    vertex[0] = Float.parseFloat(lineSplit[1]);
                    vertex[1] = Float.parseFloat(lineSplit[2]);
                    vertex[2] = Float.parseFloat(lineSplit[3]);
                    vertices.add(vertex);
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Object File Not Found");
        }

        this.vertices = vertices;
    }

    private void readObjNormals () {
        ArrayList<float[]> vertices = new ArrayList<float[]>();

        try {
            File file = new File(this.name);
            Scanner sc = new Scanner(file);
            while (sc.hasNextLine()) {
                String thisLine = sc.nextLine();
                String [] lineSplit = thisLine.split(" ");
                float vertex[] = new float[3];

                if (lineSplit[0].equals("vn")) {
                    vertex[0] = Float.parseFloat(lineSplit[1]);
                    vertex[1] = Float.parseFloat(lineSplit[2]);
                    vertex[2] = Float.parseFloat(lineSplit[3]);
                    vertices.add(vertex);
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Object File Not Found");
        }

        this.vertexNormals = vertices;
    }

    private void readObjFaces () {
        ArrayList<String[]> faces = new ArrayList<String[]>();

        try {
            File file = new File(this.name);
            Scanner sc = new Scanner(file);
            String material = "";
            while (sc.hasNextLine()) {
                String thisLine = sc.nextLine();
                String [] lineSplit = thisLine.split(" ");
                String face[] = new String[4];
                if (lineSplit[0].equals("usemtl")) {
                    material = lineSplit[1];
                }
                if (lineSplit[0].equals("f")) {
                    face[0] = lineSplit[1];
                    face[1] = lineSplit[2];
                    face[2] = lineSplit[3];
                    face[3] = material;
                    faces.add(face);
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Object File Not Found");
        }

        this.faces = faces;
    }

    private void readMtllib () {
        String mtllib = new String();

        try {
            File file = new File(this.name);
            Scanner sc = new Scanner(file);
            while (sc.hasNextLine()) {
                String thisLine = sc.nextLine();
                String [] lineSplit = thisLine.split(" ");

                if (lineSplit[0].equals("mtllib")) {
                    mtllib = lineSplit[1];
                }
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Object File Not Found");
        }

        ArrayList<Material> materials = new ArrayList<>();

        try {
            File file = new File(mtllib);
            Scanner sc = new Scanner(file);
            String name = "";
            float [] ka = new float[3];
            float [] kd = new float[3];
            float [] ks = new float[3];
            float Ns = 0;
            float illum = 0;

            while (sc.hasNextLine()) {
                String thisLine = sc.nextLine();
                String [] lineSplit = thisLine.split(" ");

                switch (lineSplit[0]) {
                    case "Ka":
                        ka[0] = Float.valueOf(lineSplit[1]);
                        ka[1] = Float.valueOf(lineSplit[2]);
                        ka[2] = Float.valueOf(lineSplit[3]);
                        break;
                    case "Kd":
                        kd[0] = Float.valueOf(lineSplit[1]);
                        kd[1] = Float.valueOf(lineSplit[2]);
                        kd[2] = Float.valueOf(lineSplit[3]);
                        break;
                    case "Ks":
                        ks[0] = Float.valueOf(lineSplit[1]);
                        ks[1] = Float.valueOf(lineSplit[2]);
                        ks[2] = Float.valueOf(lineSplit[3]);
                        break;
                    case "Ns":
                        Ns = Float.valueOf(lineSplit[1]);
                        break;
                    case "illum":
                        illum = Float.valueOf(lineSplit[1]);
                        break;
                    case "newmtl":
                        if (!name.isEmpty()) {
                            System.out.println(" Reading KD: " + Arrays.toString(kd));
                            materials.add(new Material(name,ka,kd,ks,Ns,illum));
                        }
                        ka = new float[3];
                        kd = new float[3];
                        ks = new float[3];
                        Ns = 0;
                        illum = 0;
                        name = lineSplit[1];
                }
            }
            if (!name.isEmpty()) {
                materials.add(new Material(name,ka,kd,ks,Ns,illum));
            }
        } catch (FileNotFoundException ex) {
            System.out.println("Mtllib File Not Found");
        }

        this.materials = materials;
    }

    private void rotate () {
        ArrayList<float[]> returnVertices = new ArrayList<float[]>();


        for (float vertex[]: this.vertices) {
            float returnVertex [] = new float[3];

            float [] partOne = new float[3];
            float [] partTwo = new float[3];
            float [] partThree = new float[3];

            float [] rotationAxis = new float[3];
            rotationAxis[0] = this.wx;
            rotationAxis[1] = this.wy;
            rotationAxis[2] = this.wz;
            rotationAxis = unitVector(rotationAxis);

            float [] crossResult = crossProduct(rotationAxis, vertex);

            for (int i = 0; i < 3; i++) {
                float radianAngle = (float)Math.toRadians(this.theta);
                partOne[i] = (float)Math.cos(radianAngle) * vertex[i];
                partTwo[i] = (float)Math.sin(radianAngle) * crossResult[i];
                partThree[i] = (float)(1-Math.cos(radianAngle)) * dotProduct(rotationAxis,vertex) * rotationAxis[i];
            }

            for (int i = 0; i < 3; i++) {
                returnVertex[i] = partOne[i] + partTwo[i] + partThree[i];
            }

            returnVertices.add(returnVertex);
        }

        this.vertices = returnVertices;
    }

    private void translateAndScale () {
        ArrayList<float[]> returnVertices = new ArrayList<float[]>();

        for (float [] vertex: this.vertices) {
            float [] returnVertex = new float[3];
            //scale

            returnVertex[0] = vertex[0] * this.scale;
            returnVertex[1] = vertex[1] * this.scale;
            returnVertex[2] = vertex[2] * this.scale;

            //translate
            returnVertex[0] = returnVertex[0] + this.tx;
            returnVertex[1] = returnVertex[1] + this.ty;
            returnVertex[2] = returnVertex[2] + this.tz;

            returnVertices.add(returnVertex);

        }

        this.vertices = returnVertices;
    }

    private void normals () {
        ArrayList<float[]> returnNormalsTwo = new ArrayList<float[]>();

        for (float vertex[]: this.vertexNormals) {
            float returnVertex [] = new float[3];

            float [] partOne = new float[3];
            float [] partTwo = new float[3];
            float [] partThree = new float[3];

            float [] rotationAxis = new float[3];
            rotationAxis[0] = this.wx;
            rotationAxis[1] = this.wy;
            rotationAxis[2] = this.wz;
            rotationAxis = unitVector(rotationAxis);

            float [] crossResult = crossProduct(rotationAxis, vertex);

            for (int i = 0; i < 3; i++) {
                float radianAngle = (float)Math.toRadians(this.theta);
                partOne[i] = (float)Math.cos(radianAngle) * vertex[i];
                partTwo[i] = (float)Math.sin(radianAngle) * crossResult[i];
                partThree[i] = (float)(1-Math.cos(radianAngle)) * dotProduct(rotationAxis,vertex) * rotationAxis[i];
            }

            for (int i = 0; i < 3; i++) {
                returnVertex[i] = (int)partOne[i] + (int)partTwo[i] + (int)partThree[i];
            }

            returnNormalsTwo.add(returnVertex);
        }

        this.vertexNormals = returnNormalsTwo;

        ArrayList<float[]> returnNormals = new ArrayList<float[]>();

        for (float [] vertex: this.vertexNormals) {
            float [] returnVertex = new float[3];
            //scale

            returnVertex[0] = vertex[0] * this.scale;
            returnVertex[1] = vertex[1] * this.scale;
            returnVertex[2] = vertex[2] * this.scale;

            //translate
            returnVertex[0] = returnVertex[0] + this.tx;
            returnVertex[1] = returnVertex[1] + this.ty;
            returnVertex[2] = returnVertex[2] + this.tz;

            returnNormals.add(unitVector(returnVertex));

        }

        this.vertexNormals = returnNormals;
    }

    private static float [] crossProduct (float [] vectorA, float[] vectorB) {
        float returningVector [] = new float[3];

        returningVector[0] = vectorA[1] * vectorB[2] - vectorA[2] * vectorB[1];
        returningVector[1] = vectorA[2] * vectorB[0] - vectorA[0] * vectorB[2];
        returningVector[2] = vectorA[0] * vectorB[1] - vectorA[1] * vectorB[0];

        return returningVector;
    }

    private static float dotProduct (float [] vectorA, float[] vectorB) {
        float returningVector = 0;

        for (int i = 0; i < 3; i++) {
            returningVector = returningVector + vectorA[i] * vectorB[i];
        }


        return returningVector;
    }

    private static float [] unitVector (float [] vector) {
        float returnVector [] = new float[3];

        float popPop = (float)Math.sqrt(Math.pow(vector[0],2) + Math.pow(vector[1],2) + Math.pow(vector[2],2));


        for (int i = 0; i < 3; i++) {
            returnVector[i] = vector[i]/popPop;
        }


        return returnVector;
    }

    private static float [][] multiplyMatrices (float matrixA[][],float matrixB[][]) {
        float [][] returningMatrix = new float[4][4];
        for (int i = 0; i < 4; i ++) {
            for (int j = 0; j < 4; j ++) {
                for (int k = 0; k < 4; k ++) {
                    returningMatrix[i][j] += matrixA[i][k] * matrixB[k][j];
                }
            }
        }

        return returningMatrix;
    }
}