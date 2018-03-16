import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class Sift {

    Image im;
    Image imGrey;
    List<Octave> listOctaveIm = new ArrayList<>();
    List<TheCon> listConIm = new ArrayList<>();
    List<DoG> listDoGIm = new ArrayList<>();

    public Sift(Image im) {
        this.im = im;
        applySiftIm();
    }

    /**
     * Builds a one dimensional gaussian kernel.
     *
     * @param sigma Standard deviation of the kernel.
     * @return Discretized kernel. Array length is odd, kernel is centered.
     */
    private static double[] buildKernel(final double sigma) {

        int windowSize = (int) Math.ceil(4 * sigma);

        NormalDistribution ndist = new NormalDistribution(0.0, sigma, Double.MIN_NORMAL);

        double[] kernel = new double[2 * windowSize + 1];
        double sum = 0;
        for (int i = 0; i < kernel.length; i++) {
            double x = i - windowSize;
            kernel[i] = ndist.cumulativeProbability(x - 0.5, x + 0.5);
            sum += kernel[i];
        }
        for (int i = 0; i < kernel.length; i++) {
            kernel[i] /= sum;
        }

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

    private void applySiftIm() {
        listOctaveIm = ScaleSpace();
        for (Octave o : listOctaveIm) {
            System.out.println(o);
        }
        keyPoint();
        for (TheCon c : listConIm) {
            System.out.println(c);
        }

    }

    //appliquer 5 niveau de flou aux 4 images
    private List<Octave> ScaleSpace() {
        imGrey = im.greyCopy();
        List<Octave> listOct = new ArrayList<>();
        List<Image> octave = new ArrayList<>();
        double sigmaVal1 = Math.sqrt(2);
        double sigmaVal2 = 1.0;
        //choix de sigma et passage de scale
        for (double scale = 2, nbOctave = 0; nbOctave < 4; scale /= 2, nbOctave++, sigmaVal1 *= 2, sigmaVal2 *= 2) {
            octave.add(imageFiltered(new Image(imGrey, scale), sigmaVal1 / 2));
            octave.add(imageFiltered(new Image(imGrey, scale), sigmaVal2));
            octave.add(imageFiltered(new Image(imGrey, scale), sigmaVal1));
            octave.add(imageFiltered(new Image(imGrey, scale), sigmaVal2 * 2));
            octave.add(imageFiltered(new Image(imGrey, scale), sigmaVal1 * 2));
            listOct.add(new Octave(octave));
            octave.clear();
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

//    private Pixel applyFilterToPixel(Image im, int i, int j, double[][] filter) {
//        double sumPixelR = 0, sumPixelB = 0, sumPixelG = 0, sumPixelA = 0;
//        double sumFiltre = 0;
//
//        for (int k = i - 1; k < i + 2; k++) {
//            for (int l = j - 1; l < j + 2; l++) {
//                if (noOutOfBound(k, l, im.getTabPixel().length, im.getTabPixel()[0].length)) {
//                    sumPixelR += filter[k - (i - 1)][l - (j - 1)] * im.getTabPixel()[k][l].getR();
//                    sumPixelG += filter[k - (i - 1)][l - (j - 1)] * im.getTabPixel()[k][l].getG();
//                    sumPixelB += filter[k - (i - 1)][l - (j - 1)] * im.getTabPixel()[k][l].getB();
//                    sumPixelA += filter[k - (i - 1)][l - (j - 1)] * im.getTabPixel()[k][l].getA();
//                }
//            }
//        }
//        for (int k = 0; k < 3; k++) {
//            for (int l = 0; l < 3; l++) {
//                sumFiltre += filter[k][l];
//            }
//        }
//        System.out.println(sumFiltre);
//        return new Pixel(((Double) (sumPixelR / sumFiltre)).intValue(), ((Double) (sumPixelG / sumFiltre)).intValue(), ((Double) (sumPixelB / sumFiltre)).intValue(), ((Double) (sumPixelA / sumFiltre)).intValue());
//    }


//    private double convol(Image im, int x, int y, double sigma) {
//        x -= im.getTabPixel().length / 2;
//        y -= im.getTabPixel()[0].length / 2;
//        return ((Math.exp(-(x * x + y * y) / (2 * sigma * sigma)))) / (2 * Math.PI * sigma * sigma);
//    }

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

    private void keyPoint() {
        int i = 0;
        for (Octave o : listOctaveIm) {
            List<Image> oct = o.getOctave();
            List<Image> con = new ArrayList<>();
            con.add(new Image("con" + oct.get(0).getName() + i, diffImage(oct.get(0).getTabPixel(), oct.get(1).getTabPixel())));
            i++;
            con.add(new Image("con" + oct.get(1).getName() + i, diffImage(oct.get(1).getTabPixel(), oct.get(2).getTabPixel())));
            i++;
            con.add(new Image("con" + oct.get(2).getName() + i, diffImage(oct.get(2).getTabPixel(), oct.get(3).getTabPixel())));
            i++;
            con.add(new Image("con" + oct.get(3).getName() + i, diffImage(oct.get(3).getTabPixel(), oct.get(4).getTabPixel())));
            i++;
            listConIm.add(new TheCon(con));
        }
        i = 0;
        for (TheCon c : listConIm) {
            List<Image> con = c.getCon();
            List<Image> dog = new ArrayList<>();
            dog.add(new Image("dog" + con.get(0).getName() + i, findKeyPoint(con.get(0).getTabPixel(), con.get(1).getTabPixel(), con.get(2).getTabPixel())));
            dog.add(new Image("dog" + con.get(0).getName() + i, findKeyPoint(con.get(1).getTabPixel(), con.get(2).getTabPixel(), con.get(3).getTabPixel())));

            i++;
        }
//        Pixel[][] pixelKeyPoint1 = new Pixel[pixelDiffOctave2.length][pixelDiffOctave2[0].length];
//        Pixel[][] pixelKeyPoint2 = new Pixel[pixelDiffOctave2.length][pixelDiffOctave2[0].length];

        //pixelKeyPoint1 = findKeyPoint(pixelDiffOctave1, pixelDiffOctave2, pixelDiffOctave3);
        //pixelKeyPoint2 = findKeyPoint(pixelDiffOctave2, pixelDiffOctave3, pixelDiffOctave4);
        System.out.println("fin");

    }

    private Pixel[][] findKeyPoint(Pixel[][] pixelOct1, Pixel[][] pixelOct2, Pixel[][] pixelOct3) {
        ArrayList<Pixel> list_point = new ArrayList<>();
        Pixel[][] pixelKeyPoint = new Pixel[pixelOct2.length][pixelOct2[0].length];
        final Comparator<Pixel> comp = Comparator.comparingInt(p -> p.getR() + p.getG() + p.getB() + p.getA());
        Pixel pmax;
        for (int i = 0; i < pixelOct2.length; i++) {
            for (int j = 0; j < pixelOct2[0].length; j++) {
                for (int k = i - 1; k < i + 2; k++) {
                    for (int l = j - 1; l < j + 2; l++) {
                        if (noOutOfBound(k, l, pixelOct1.length, pixelOct1[0].length)) {
                            list_point.add(pixelOct1[k][l]);
                            list_point.add(pixelOct2[k][l]);
                            list_point.add(pixelOct3[k][l]);
                        }
                    }
                }
                pmax = list_point.stream().max(comp).get();
                pixelKeyPoint[i][j] = pmax;
            }
        }
        return pixelKeyPoint;
    }


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
