import javafx.util.Pair;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Sift {

    Image im;
    Image imGrey;
    List<Octave> listOctaveIm = new ArrayList<>();
    List<TheCon> listConIm = new ArrayList<>();
    List<DoG> listDoGIm = new ArrayList<>();
    List<DoG> listDoGFilteredIm = new ArrayList<>();

    public Sift(Image im) {
        this.im = im;
        applySiftIm();
    }

    private void applySiftIm() {
        listOctaveIm = ScaleSpace();
        for (Octave o : listOctaveIm) {
//            System.out.println(o);
        }
        keyPoint();
        for (TheCon c : listConIm) {
//            System.out.println(c);
        }
        DoG();
        for (DoG d : listDoGIm) {
            System.out.println(d);
        }
        filtreDoG();
        for (DoG d : listDoGFilteredIm) {
            System.out.println(d);
        }


        System.out.println("fin");
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

    private Image imageFiltered(Image im, double sigma) {
        if (im == null) {
            throw new NullPointerException("image must not be null");
        }
        if (sigma < im.getSigma()) {
            throw new IllegalArgumentException("cannot reduce sigma");
        }

        final double scale = im.getScale();
        final double imageSigma = im.getSigma() * scale;
        final double targetSigma = sigma * scale;
        double filterSigma = Math.sqrt(targetSigma * targetSigma - imageSigma * imageSigma);

        if (filterSigma == 0.0) {
            // Dirac delta function, output is input
            return new Image(im, 1);
        }

        double[] kernel = buildKernel(filterSigma);
        double[] cumulativeKernel = cumulativeSum(kernel);
        int window = (kernel.length - 1) / 2;

        // horizontal pass
        double[][] horizontal = new double[im.getWidth()][im.getHeight()];
        for (int row = 0; row < im.getWidth(); row++) {
            for (int col = 0; col < im.getHeight(); col++) {

                int kernelFrom = Math.max(window - col, 0);
                int kernelTo = window
                        + Math.min(im.getHeight() - 1 - col, window);

                double value = 0;
                for (int i = kernelFrom; i <= kernelTo; i++) {
                    value += kernel[i] * im.getGreyPixel(row, col - window + i);
                }
                double weight = cumulativeKernel[kernelTo + 1]
                        - cumulativeKernel[kernelFrom];
                value /= weight;
                horizontal[row][col] = value;
            }
        }

        Image result = new Image(im.getWidth(), im.getHeight());

        // vertical pass (in-place)
        double[][] vertical = new double[im.getWidth()][im.getHeight()];
        for (int col = 0; col < im.getHeight(); col++) {
            for (int row = 0; row < im.getWidth(); row++) {

                int kernelFrom = Math.max(window - row, 0);
                int kernelTo = window
                        + Math.min(im.getWidth() - 1 - row, window);

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

        for (int row = 0; row < im.getWidth(); row++) {
            for (int col = 0; col < im.getHeight(); col++) {
                result.setGreyPixel(row, col, (int) vertical[row][col]);
            }
        }

        return result;

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

    }

    private void DoG() {

        int i = 0;
        for (TheCon c : listConIm) {
            List<Image> con = c.getCon();
            List<Image> dog = new ArrayList<>();
            dog.add(new Image("dog" + con.get(0).getName() + i, findKeyPoint(con.get(0).getTabPixel(), con.get(1).getTabPixel(), con.get(2).getTabPixel())));
            dog.add(new Image("dog" + con.get(0).getName() + i, findKeyPoint(con.get(1).getTabPixel(), con.get(2).getTabPixel(), con.get(3).getTabPixel())));
            listDoGIm.add(new DoG(dog));
            i++;
        }
    }

    private void filtreDoG() {
        int i = 0;
        for (DoG d : listDoGIm) {
            List<Image> dog = d.getDoG();
            List<Image> dogF = new ArrayList<>();
            dogF.add(new Image("dogF" + dog.get(0).getName() + i, RidBadKeyPoint(dog.get(0).getTabPixel())));
            dogF.add(new Image("dogF" + dog.get(0).getName() + i, RidBadKeyPoint(dog.get(1).getTabPixel())));
            listDoGFilteredIm.add(new DoG(dogF));
            i++;
        }
    }

    private Pixel[][] findKeyPoint(Pixel[][] pixelOct1, Pixel[][] pixelOct2, Pixel[][] pixelOct3) {

        Pixel[][] pixelKeyPoint = new Pixel[pixelOct2.length][pixelOct2[0].length];

        blackPixels(pixelKeyPoint);

        HashMap<Pair<Integer, Integer>, Pixel> map = new HashMap<>();

        for (int i = 1; i < pixelOct2.length - 1; i++) {
            for (int j = 1; j < pixelOct2[0].length - 1; j++) {
                boolean isMax = true;
                float value = pixelOct2[i][j].getR();
                float sign = Math.signum(value - pixelOct2[i][j - 1].getR());

                isMax &= pixelOct1[i - 1][j - 1].getR() * sign < value;
                isMax &= pixelOct1[i - 1][j].getR() * sign < value;
                isMax &= pixelOct1[i - 1][j + 1].getR() * sign < value;
                isMax &= pixelOct1[i][j - 1].getR() * sign < value;
                isMax &= pixelOct1[i][j].getR() * sign < value;
                isMax &= pixelOct1[i][j + 1].getR() * sign < value;
                isMax &= pixelOct1[i + 1][j - 1].getR() * sign < value;
                isMax &= pixelOct1[i + 1][j].getR() * sign < value;
                isMax &= pixelOct1[i + 1][j + 1].getR() * sign < value;

                isMax &= pixelOct2[i - 1][j - 1].getR() * sign < value;
                isMax &= pixelOct2[i - 1][j].getR() * sign < value;
                isMax &= pixelOct2[i - 1][j + 1].getR() * sign < value;
                isMax &= pixelOct2[i][j - 1].getR() * sign < value;
                isMax &= pixelOct2[i][j + 1].getR() * sign < value;
                isMax &= pixelOct2[i + 1][j - 1].getR() * sign < value;
                isMax &= pixelOct2[i + 1][j].getR() * sign < value;
                isMax &= pixelOct2[i + 1][j + 1].getR() * sign < value;

                isMax &= pixelOct3[i - 1][j - 1].getR() * sign < value;
                isMax &= pixelOct3[i - 1][j].getR() * sign < value;
                isMax &= pixelOct3[i - 1][j + 1].getR() * sign < value;
                isMax &= pixelOct3[i][j - 1].getR() * sign < value;
                isMax &= pixelOct3[i][j].getR() * sign < value;
                isMax &= pixelOct3[i][j + 1].getR() * sign < value;
                isMax &= pixelOct3[i + 1][j - 1].getR() * sign < value;
                isMax &= pixelOct3[i + 1][j].getR() * sign < value;
                isMax &= pixelOct3[i + 1][j + 1].getR() * sign < value;
                if (isMax) {
                    map.put(new Pair<>(i, j), pixelOct2[i][j]);
                }
            }
        }

        map.forEach((p, v) -> {
            pixelKeyPoint[p.getKey()][p.getValue()] = v;
        });
        return pixelKeyPoint;
    }

    private void blackPixels(Pixel[][] pixelKeyPoint) {
        for (int i = 0; i < pixelKeyPoint.length; i++) {
            for (int j = 0; j < pixelKeyPoint[0].length; j++) {
                pixelKeyPoint[i][j] = new Pixel(0, 0, 0, 0);
            }
        }
    }


    private boolean noOutOfBound(int i, int j, int size_i, int size_j) {
        return i >= 0 && j >= 0 && i < size_i && j < size_j;
    }

    private Pixel[][] RidBadKeyPoint(Pixel[][] pixels) {

        Pixel[][] pixelsFiltered = edgeFilter(contrastFilter(pixels));
        return pixelsFiltered;
    }

    private Pixel[][] contrastFilter(Pixel[][] pixels) {
        Pixel[][] newPixels = new Pixel[pixels.length][pixels[0].length];
//        int k=0;
        for (int i = 0; i < pixels.length; i++) {
            for (int j = 0; j < pixels[0].length; j++) {
                if (pixels[i][j].getR() < 2 && pixels[i][j].getR() > 0) {
                    newPixels[i][j] = new Pixel(0, 0, 0, 0);
//                    k++;
                } else {
                    newPixels[i][j] = new Pixel(pixels[i][j]);
                }
            }
        }
//        System.out.println("nbContrastRetire = "+k);
        return newPixels;
    }

    private Pixel[][] edgeFilter(Pixel[][] pixels) {
        Pixel[][] newPixels = new Pixel[pixels.length][pixels[0].length];
        blackPixels(newPixels);
//        int k=0;
        for (int i = 1; i < pixels.length - 1; i++) {
            for (int j = 1; j < pixels[0].length - 1; j++) {
                if (pixels[i][j].getR() != 0) {
                    if (!isCoin(pixels[i][j], pixels[i - 1][j], pixels[i + 1][j], pixels[i][j - 1], pixels[i][j + 1])) {
                        newPixels[i][j] = new Pixel(0, 0, 0, 0);
//                        k++;
                    } else {
                        newPixels[i][j] = new Pixel(pixels[i][j]);
                    }
                }
            }
        }
//        System.out.println("nbNonCoinRetire = "+k);
        return newPixels;
    }

    private boolean isCoin(Pixel p, Pixel pLeft, Pixel pRight, Pixel pDown, Pixel pUp) {
        return (pRight.getR() - pLeft.getR()) / 2 > 0 && (pUp.getR() - pDown.getR()) / 2 > 0;
    }


    private void AssignOrientationKeyPoint() {

    }

    private void GenerateSiftFeature() {

    }

    public Double getCompare() {
        return 0.0;
    }

}
