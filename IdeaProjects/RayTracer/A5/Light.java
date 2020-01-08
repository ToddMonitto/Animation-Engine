import java.util.ArrayList;

public class Light {
    float[] location = new float[3];
    float[] color = new float[3];
    int infinityLight;

    ArrayList<Ray> rays = new ArrayList<Ray>();

    public Light (float[] light) {
        for (int i = 0; i < 3; i++) {
            location[i] = light[i];
            color[i] = light[i+4];
        }
        infinityLight = (int)light[3];
    }

    public void print () {
        String printing = "Light Location: "+location[0]+","+location[1]+","+location[2]+
                " Color: Red: "+color[0]+", Green: "+color[1]+", Blue: "+color[2]+
                " Infinity Light: "+infinityLight;
        System.out.println(printing);
    }

    public void createRays(int res[]) {
        for (int i = 0; i < res[0]; i++) {
            for (int j = 0; i < res[0]; i++) {
                for (int k = 0; i < res[1]; i++) {

                }
            }
        }
    }
}
