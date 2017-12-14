public class Pixel {
    private int r;
    private int g;
    private int b;
    private int a;
    Pixel(int r, int g, int b,int a){
        this.r=r;
        this.g=g;
        this.b=b;
        this.a=a;
    }

    @Override
    public String toString() {
        return "Pixel{" +
                "r=" + r +
                ", g=" + g +
                ", b=" + b +
                ", a=" + a +
                '}';
    }

    public int getA() {
        return a;
    }

    public int getB() {
        return b;
    }

    public int getG() {
        return g;
    }

    public int getR() {
        return r;
    }
}
