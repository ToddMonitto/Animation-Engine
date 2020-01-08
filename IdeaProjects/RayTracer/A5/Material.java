import java.util.Arrays;

public class Material {
    String name;
    float [] ka;
    float [] kd;
    float [] ks;
    float Ns;
    float illum;

    public Material (String name, float[] ka, float[] kd, float[] ks, float Ns, float illum) {
        this.name = name;
        this.ka = ka;
        this.kd = kd;
        this.ks = ks;
        this.Ns = Ns;
        this.illum = illum;
    }

    public void print () {
        System.out.println("Name: " + name + " kA: " + Arrays.toString(ka) + "kD: " + Arrays.toString(kd) + "kS: " + Arrays.toString(ks));
    }
}
