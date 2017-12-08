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
}
