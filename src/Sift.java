import javafx.util.Pair;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
//        for (Octave o : listOctaveIm) {
//            System.out.println(o);
//        }
        keyPoint();
//        for (TheCon c : listConIm) {
//            System.out.println(c);
//        }
        DoG();
//        for (DoG d : listDoGIm) {
//            System.out.println(d);
//        }
        filtreDoG();
//        for (DoG d : listDoGFilteredIm) {
//            System.out.println(d);
//        }

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
        for (int i = 0; i < pixels.length; i++) {
            for (int j = 0; j < pixels[0].length; j++) {
                if (pixels[i][j].getR() < 2 && pixels[i][j].getR() > 0) {
                    newPixels[i][j] = new Pixel(0, 0, 0, 0);
                } else {
                    newPixels[i][j] = new Pixel(pixels[i][j]);
                }
            }
        }
        return newPixels;
    }

    private Pixel[][] edgeFilter(Pixel[][] p) {
        int radius = 10;
        int minMeasure = 15;
        int minDistance = p.length / 100;
        int height = p[0].length;
        int width = p.length;
        List<Corner> allCorner = new ArrayList<>();

        Pixel[][] pReturn = new Pixel[width][height];

        double[][] lx2 = new double[width][height];
        double[][] ly2 = new double[width][height];
        double[][] lxy = new double[width][height];

        double[][][] grad = new double[width][height][];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grad[x][y] = sob(p, x, y);
            }
        }

        double[][] filter = new double[2 * radius + 1][2 * radius + 1];
        double filtersum = 0;
        for (int j = -radius; j <= radius; j++) {
            for (int i = -radius; i <= radius; i++) {
                double g = gaussian(i, j, im.getSigma());
                filter[i + radius][j + radius] = g;
                filtersum += g;
            }
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                for (int dy = -radius; dy <= radius; dy++) {
                    for (int dx = -radius; dx <= radius; dx++) {
                        int xk = x + dx;
                        int yk = y + dy;
                        if (xk < 0 || xk >= width) continue;
                        if (yk < 0 || yk >= height) continue;

                        // filter weight
                        double f = filter[dx + radius][dy + radius];

                        // convolution
                        lx2[x][y] += f * grad[xk][yk][0] * grad[xk][yk][0];
                        ly2[x][y] += f * grad[xk][yk][1] * grad[xk][yk][1];
                        lxy[x][y] += f * grad[xk][yk][0] * grad[xk][yk][1];
                    }
                }
                lx2[x][y] /= filtersum;
                ly2[x][y] /= filtersum;
                lxy[x][y] /= filtersum;
            }
        }
        // Harris measure map
        double[][] harrismap = new double[width][height];
        double max = 0;

        // for each pixel in the image
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // compute ans store the harris measure
                harrismap[x][y] = R(x, y, lx2, lxy, ly2);
                if (harrismap[x][y] > max) max = harrismap[x][y];
            }
        }

        // rescale measures in 0-100
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double h = harrismap[x][y];
                if (h < 0) h = 0;
                else h = 100 * Math.log(1 + h) / Math.log(1 + max);
                harrismap[x][y] = h;
            }
        }

        // copy of the original image (a little darker)
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Pixel cpy = new Pixel((int) (p[x][y].getR() * 0.80));
                pReturn[x][y] = cpy;
            }
        }

        // for each pixel in the hmap, keep the local maxima
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                double h = harrismap[x][y];
                if (h < minMeasure) continue;
                if (!isSpatialMaxima(harrismap, (int) x, (int) y)) continue;
                // add the corner to the list
//              getCorner(canal).add( new Corner(x,y,h) );
                allCorner.add(new Corner(x, y, h));
                pReturn[x][y] = new Pixel(255);
            }
        }

        Iterator<Corner> iter = allCorner.iterator();
        while (iter.hasNext()) {
            Corner c = iter.next();
            for (Corner ctmp : allCorner) {
                if (ctmp == c) continue;
                int dist = (int) Math.sqrt((c.x - ctmp.x) * (c.x - ctmp.x) + (c.y - ctmp.y) * (c.y - ctmp.y));
                if (dist > minDistance) continue;
                if (ctmp.measure < c.measure) continue;
                iter.remove();
                p[c.x][c.y] = new Pixel(0);
                break;
            }
        }

//        // Display corners over the image (cross)
//        for (Corner p:getCorner(canal)) {
//            for (int dx=-2; dx<=2; dx++) {
//                if (p.x+dx<0 || p.x+dx>=width) continue;
//                setInsidePixel(output, (int)p.x+dx, (int)p.y, canal, 255);
//            }
//            for (int dy=-2; dy<=2; dy++) {
//                if (p.y+dy<0 || p.y+dy>=height) continue;
//                setInsidePixel(output, (int)p.x,(int)p.y+dy, canal, 255);
//            }
//        }
        return pReturn;
    }

    private double gaussian(double x, double y, double sigma2) {
        double t = (x * x + y * y) / (2 * sigma2);
        double u = 1.0 / (2 * Math.PI * sigma2);
        double e = u * Math.exp(-t);
        return e;
    }

    private double[] sob(Pixel[][] p, int x, int y) {
        int x0 = x - 1, x1 = x, x2 = x + 1;
        int y0 = y - 1, y1 = y, y2 = y + 1;
        int height = p[0].length;
        int width = p.length;
        if (x0 < 0) x0 = 0;
        if (y0 < 0) y0 = 0;
        if (x2 >= width) x2 = width - 1;
        if (y2 >= height) y2 = height - 1;

        double sobx = (p[x2][y0].getR() + 2 * p[x2][y1].getR() + p[x2][y2].getR()) - (p[x0][y0].getR() + 2 * p[x0][y1].getR() + p[x0][y2].getR());
        double soby = (p[x0][y2].getR() + 2 * p[x1][y2].getR() + p[x2][y2].getR()) - (p[x0][y0].getR() + 2 * p[x1][y0].getR() + p[x2][y0].getR());
        return new double[]{sobx / 4, soby / 4};
    }

    private double R(int x, int y, double[][] lx2, double[][] lxy, double[][] ly2) {
        double m00 = lx2[x][y];
        double m01 = lxy[x][y];
        double m10 = lxy[x][y];
        double m11 = ly2[x][y];
        return m00 * m11 - m01 * m10 - 0.06 * (m00 + m11) * (m00 + m11);
    }

    private boolean isSpatialMaxima(double[][] hmap, int x, int y) {
        int n = 8;
        int[] dx = new int[]{-1, 0, 1, 1, 1, 0, -1, -1};
        int[] dy = new int[]{-1, -1, -1, 0, 1, 1, 1, 0};
        double w = hmap[x][y];
        for (int i = 0; i < n; i++) {
            double wk = hmap[x + dx[i]][y + dy[i]];
            if (wk >= w) return false;
        }
        return true;
    }

    private class Corner {
        int x, y;
        double measure;

        Corner(int x, int y, double measure) {
            this.x = x;
            this.y = y;
            this.measure = measure;
        }
    }


    private void AssignOrientationKeyPoint() {

    }

    private void GenerateSiftFeature() {

    }

    public Double getCompare() {
        return 0.0;
    }



    /* aider par Copyright 2009 by Humbert Florent pour harris corner detector
    lien : http://subversion.developpez.com/projets/Millie/trunk/Millie/src/millie/operator/detection/HarrisFastDetectionOperator.java
     */
}
