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
    Pixel(Pixel cp){
        this.r=cp.r;
        this.g=cp.g;
        this.b=cp.b;
        this.a=cp.a;
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

    public Pixel diff(Pixel pixel) {
        return new Pixel(Math.abs(r-pixel.r),Math.abs(g-pixel.g),Math.abs(b-pixel.b),Math.abs(a-pixel.a));
    }
}
