import java.util.ArrayList;
import java.util.List;


public class Sift {

    Image im1;
    Image im2;
    List<Octave> listOctaveIm1 = new ArrayList<>();
    List<Octave> listOctaveIm2 = new ArrayList<>();

    public Sift(Image im1, Image im2) {
        this.im1 = im1;
        this.im2 = im2;
    }

    //TODO Builds a one dimensional gaussian kernel.
    private static double[] buildKernel(final double sigma) {

        int windowSize = (int) Math.ceil(4 * sigma);

        double[] kernel;
        kernel = new double[2 * windowSize + 1];
        //TODO

        return kernel;
    }

    private static double[] cumulativeSum(final double[] kernel) {
        double[] sum = new double[kernel.length + 1];
        sum[0] = 0.0;
        for (int i = 0; i < kernel.length; i++) {
            sum[i + 1] = sum[i] + kernel[i];
        }
        return sum;
    }

    public double compare() {
        System.out.println("sift");
        applySiftIm1();
        applySiftIm2();
        return 0.0;
    }

    private void applySiftIm1() {
        listOctaveIm1 = ScaleSpace(im1);
        for (Octave o : listOctaveIm1) {
            System.out.println(o);
        }

    }

    private void applySiftIm2() {
        listOctaveIm2 = ScaleSpace(im2);
        for (Octave o : listOctaveIm1) {
            System.out.println(o);
        }

    }

    //appliquer 5 niveau de flou aux 4 images
    private List<Octave> ScaleSpace(Image im) {
        Image imGrey = im.greyCopy();
        List<Octave> listOct = new ArrayList<>();
        List<Image> octave = new ArrayList<>();
        double sigmaVal1 = Math.sqrt(2);
        double sigmaVal2 = 1.0;
        //choix de sigma et passage de scale
        for (int scale = 2, nbOctave = 0; nbOctave < 4; scale /= 2, nbOctave++, sigmaVal1 *= 2, sigmaVal2 *= 2) {
            octave.add(imageFiltered(new Image(imGrey, scale), sigmaVal1 / 2));
            octave.add(imageFiltered(new Image(imGrey, scale), sigmaVal2));
            octave.add(imageFiltered(new Image(imGrey, scale), sigmaVal1));
            octave.add(imageFiltered(new Image(imGrey, scale), sigmaVal2 * 2));
            octave.add(imageFiltered(new Image(imGrey, scale), sigmaVal1 * 2));
            listOct.add(new Octave(octave));
        }
        return listOct;
    }

    private Image imageFiltered(Image image, double sigma) {
        if (image == null) {
            throw new NullPointerException("image must not be null");
        }
        if (sigma < image.getSigma()) {
            throw new IllegalArgumentException("cannot reduce sigma");
        }

        final double scale = image.getScale();
        final double imageSigma = image.getSigma() * scale;
        final double targetSigma = sigma * scale;
        double filterSigma = Math.sqrt(targetSigma * targetSigma - imageSigma * imageSigma);

        if (filterSigma == 0.0) {
            // Dirac delta function, output is input
            return new Image(image, 1);
        }

        double[] kernel = buildKernel(filterSigma);
        double[] cumulativeKernel = cumulativeSum(kernel);
        int window = (kernel.length - 1) / 2;

        // horizontal pass
        double[][] horizontal = new double[image.getWidth()][image.getHeight()];
        for (int row = 0; row < image.getWidth(); row++) {
            for (int col = 0; col < image.getHeight(); col++) {

                int kernelFrom = Math.max(window - col, 0);
                int kernelTo = window
                        + Math.min(image.getHeight() - 1 - col, window);

                double value = 0;
                for (int i = kernelFrom; i <= kernelTo; i++) {
                    value += kernel[i] * image.getGreyPixel(row, col - window + i);
                }
                double weight = cumulativeKernel[kernelTo + 1]
                        - cumulativeKernel[kernelFrom];
                value /= weight;
                horizontal[row][col] = value;
            }
        }

        Image result = new Image(image.getWidth(), image.getHeight());

        // vertical pass (in-place)
        double[][] vertical = new double[image.getWidth()][image.getHeight()];
        for (int col = 0; col < image.getHeight(); col++) {
            for (int row = 0; row < image.getWidth(); row++) {

                int kernelFrom = Math.max(window - row, 0);
                int kernelTo = window
                        + Math.min(image.getWidth() - 1 - row, window);

                double value = 0;
                for (int i = kernelFrom; i <= kernelTo; i++) {
                    value += kernel[i] * horizontal[row - window + i][col];
                }
                double weight = cumulativeKernel[kernelTo + 1]
                        - cumulativeKernel[kernelFrom];
                value /= weight;
                vertical[row][col] = value;

            }
        }

        for (int row = 0; row < image.getWidth(); row++) {
            for (int col = 0; col < image.getHeight(); col++) {
                result.setGreyPixel(row, col, (int) vertical[row][col]);
            }
        }

        return result;

    }

    private Pixel applyFilterToPixel(Image im, int i, int j, double[][] filter) {
        double sumPixelR = 0, sumPixelB = 0, sumPixelG = 0, sumPixelA = 0;
        double sumFiltre = 0;

        for (int k = i - 1; k < i + 2; k++) {
            for (int l = j - 1; l < j + 2; l++) {
                if (noOutOfBound(k, l, im.getTabPixel().length, im.getTabPixel()[0].length)) {
                    sumPixelR += filter[k - (i - 1)][l - (j - 1)] * im.getTabPixel()[k][l].getR();
                    sumPixelG += filter[k - (i - 1)][l - (j - 1)] * im.getTabPixel()[k][l].getG();
                    sumPixelB += filter[k - (i - 1)][l - (j - 1)] * im.getTabPixel()[k][l].getB();
                    sumPixelA += filter[k - (i - 1)][l - (j - 1)] * im.getTabPixel()[k][l].getA();
                }
            }
        }
        for (int k = 0; k < 3; k++) {
            for (int l = 0; l < 3; l++) {
                sumFiltre += filter[k][l];
            }
        }
        System.out.println(sumFiltre);
        return new Pixel(((Double) (sumPixelR / sumFiltre)).intValue(), ((Double) (sumPixelG / sumFiltre)).intValue(), ((Double) (sumPixelB / sumFiltre)).intValue(), ((Double) (sumPixelA / sumFiltre)).intValue());
    }


    private double convol(Image im, int x, int y, double sigma) {
        x -= im.getTabPixel().length / 2;
        y -= im.getTabPixel()[0].length / 2;
        return ((Math.exp(-(x * x + y * y) / (2 * sigma * sigma)))) / (2 * Math.PI * sigma * sigma);
    }

    //la meme taille des image
    private Pixel[][] diffImage(Pixel[][] firstImage, Pixel[][] secondImage) {
        Pixel[][] pixelsDiff = new Pixel[firstImage.length][firstImage[0].length];
        for (int i = 0; i < firstImage.length; i++) {
            for (int j = 0; j < firstImage[0].length; j++) {
                pixelsDiff[i][j] = firstImage[i][j].diff(secondImage[i][j]);
            }
        }
        return pixelsDiff;
    }

//    private void keyPoint(Image im) {
//
//        //Log approx + ScaleSpace
//        for (double d = Math.sqrt(2) / 2; d < 18; d *= 2) {
//            im.writeImage(ScaleSpace(im1,d), "png", "image_test/" + im.getName() + "_" + d + "out.png");
//        }
//        Pixel[][] pixelDiffOctave1 = diffImage(ScaleSpace(im,Math.sqrt(2) / 2), ScaleSpace(im,Math.sqrt(2)));
//        Pixel[][] pixelDiffOctave2 = diffImage(ScaleSpace(im,Math.sqrt(2)), ScaleSpace(im,Math.sqrt(2) * 2));
//        Pixel[][] pixelDiffOctave3 = diffImage(ScaleSpace(im,Math.sqrt(2) * 2), ScaleSpace(im,Math.sqrt(2) * 4));
//        Pixel[][] pixelDiffOctave4 = diffImage(ScaleSpace(im,Math.sqrt(2) * 4), ScaleSpace(im,Math.sqrt(2) * 8));
//
//        System.out.println("write");
//        im.writeImage(pixelDiffOctave1, "png", "image_test/" + im.getName() + "out1.png");
//        im.writeImage(pixelDiffOctave2, "png", "image_test/" + im.getName() + "out2.png");
//        im.writeImage(pixelDiffOctave3, "png", "image_test/" + im.getName() + "out3.png");
//        im.writeImage(pixelDiffOctave4, "png", "image_test/" + im.getName() + "out4.png");
//        System.out.println("finwrite");
//        Pixel[][] pixelKeyPoint1 = new Pixel[pixelDiffOctave2.length][pixelDiffOctave2[0].length];
//        Pixel[][] pixelKeyPoint2 = new Pixel[pixelDiffOctave2.length][pixelDiffOctave2[0].length];
//
//        //pixelKeyPoint1 = findKeyPoint(pixelDiffOctave1, pixelDiffOctave2, pixelDiffOctave3);
//        //pixelKeyPoint2 = findKeyPoint(pixelDiffOctave2, pixelDiffOctave3, pixelDiffOctave4);
//        System.out.println("fin");
//
//    }
//
//    private Pixel[][] findKeyPoint(Pixel[][] pixelOct1, Pixel[][] pixelOct2, Pixel[][] pixelOct3) {
//        ArrayList<Pixel> list_point = new ArrayList<>();
//        Pixel[][] pixelKeyPoint = new Pixel[pixelOct2.length][pixelOct2[0].length];
//        final Comparator<Pixel> comp = Comparator.comparingInt(p -> p.getR() + p.getG() + p.getB() + p.getA());
//        Pixel pmax;
//        for (int i = 0; i < pixelOct2.length; i++) {
//            for (int j = 0; j < pixelOct2[0].length; j++) {
//                for (int k = i - 1; k < i + 2; k++) {
//                    for (int l = j - 1; l < j + 2; l++) {
//                        if (noOutOfBound(k, l, pixelOct1.length, pixelOct1[0].length)) {
//                            list_point.add(pixelOct1[k][l]);
//                            list_point.add(pixelOct2[k][l]);
//                            list_point.add(pixelOct3[k][l]);
//                        }
//                    }
//                }
//                pmax = list_point.stream().max(comp).get();
//                pixelKeyPoint[i][j] = pmax;
//            }
//        }
//        return pixelKeyPoint;
//    }


    private boolean noOutOfBound(int i, int j, int size_i, int size_j) {
        return i >= 0 && j >= 0 && i < size_i && j < size_j;
    }

    private void RidBadKeyPoint() {

    }

    private void AssignOrientationKeyPoint() {

    }

    private void GenerateSiftFeature() {

    }

    public Double getCompare() {
        return 0.0;
    }

}
