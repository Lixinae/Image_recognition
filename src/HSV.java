public class HSV {
    private float h;
    private float s;
    private float v;

    HSV(float h,float s,float v){
        this.h=h;
        this.s=s;
        this.v=v;
    }

    @Override
    public String toString() {
        return "HSV{" +
                "h=" + h +
                ", s=" + s +
                ", v=" + v +
                '}';
    }

    public float getH() {
        return h;
    }

    public float getS() {
        return s;
    }

    public float getV() {
        return v;
    }
}
