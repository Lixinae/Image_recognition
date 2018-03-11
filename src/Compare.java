public class Compare {
    public static double compare(Image im1, Image im2) {
        Sift s = new Sift(im1, im2);
        return s.compare();
    }
}
