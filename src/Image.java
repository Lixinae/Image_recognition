import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Stream;

public class Image {
    private Pixel moyenne_rgb;
    private HSV[][] histoHSV;
    private int id_image;
    private Pixel[][] pixels2D; // Largeur / Hauteur pour les coordonnées
    private String pathToImage;
    private String name;
    private int width;
    private int height;
    private int[][][] histo_color;

    public Image(int id_image, String pathToImage) {
        System.out.println("im");
        this.id_image = id_image;
        this.pathToImage = pathToImage;
        this.name = constructName(pathToImage);
        initPixelTab();
        histoHSV = calcul_HSV();
        moyenne_rgb = calcul_moyenne_rgb();
        histo_color = calcul_histo_color();
        SIFT();
    }

    /**
     * Renvoie un double
     *
     * @param target Image avec laquelle comparer
     * @return Un double qui est la difference colorimétrique -> Plus proche de 0 plus l'image target est proche de l'original
     */
    public double compare(Image target) {

        int[][][] histoI = target.histo_color;
        double coef0 = 0, coef1 = 0, coef2 = 0, coef3 = 0, coef4 = 0;

        for (int i = 0; i < histoI.length; i++) {
            for (int j = 0; j < histoI[0].length; j++) {
                for (int k = 0; k < histoI[0][0].length; k++) {
//                    double val = Math.pow(histo_color[i][j][k] - histoI[i][j][k], 2.0);
                    double val = Math.abs(histo_color[i][j][k] - histoI[i][j][k]);
                    switch (k) {
                        case 0:
                            coef0 += val;
                            break;
                        case 1:
                            coef1 += val;
                            break;
                        case 2:
                            coef2 += val;
                            break;
                        case 3:
                            coef3 += val;
                            break;
                        case 4:
                            coef4 += val;
                            break;
                    }
                }
            }
        }

        System.out.println("coef0 = " + coef0);
        System.out.println("coef1 = " + coef1);
        System.out.println("coef2 = " + coef2);
        System.out.println("coef3 = " + coef3);
        System.out.println("coef4 = " + coef4);

        return coef0 + coef1 + coef2 + coef3 + coef4;

//        double d;
//        double r = Math.abs(image.moyenne_rgb.getR()-moyenne_rgb.getR());
//        double g = Math.abs(image.moyenne_rgb.getG()-moyenne_rgb.getG());
//        double b = Math.abs(image.moyenne_rgb.getB()-moyenne_rgb.getB());
//        d=r+g+b;
//        return d;
    }

    private void SIFT() {
        System.out.println("sift");
        keyPoint();
        RidBadKeyPoint();
        AssignOrientationKeyPoint();
        GenerateSiftFeature();
    }

    private Pixel[][] ScaleSpace(double sigma) {
        //TODO chercher pourquoi image noir
        Pixel[][] pixels2DFlou = new Pixel[pixels2D.length][pixels2D[0].length];
        System.out.println("sigma" + sigma);
        double[][] filter = new double[3][3];
        // creation du filtre
        for (int i = 0; i < filter.length; i++) {
            for (int j = 0; j < filter[0].length; j++) {
                filter[i][j] = convol(i, j, sigma);
                System.out.println(filter[i][j]);
            }
        }


//        for (int i = 0; i < pixels2D.length; i++) {
//            for (int j = 0; j < pixels2D[0].length; j++) {
//                pixels2DFlou[i][j] = applyFilterToPixel(i, j, filter);
//            }
//        }


        for (int i = 0; i < pixels2D.length; i++) {
            for (int j = 0; j < pixels2D[0].length; j++) {
                HashMap<Pixel, Double> map_pixel = new HashMap<>();
                map_pixel.put(pixels2D[i][j], convol(i, j, sigma));
                if (i > 0) {
                    if (j > 0) {
                        map_pixel.put(pixels2D[i - 1][j - 1], convol(i - 1, j - 1, sigma));
                    }
                    if (j < pixels2D[0].length - 1) {
                        map_pixel.put(pixels2D[i - 1][j + 1], convol(i - 1, j + 1, sigma));
                    }
                    map_pixel.put(pixels2D[i - 1][j], convol(i - 1, j, sigma));
                }
                if (i < pixels2D.length - 1) {
                    if (j < pixels2D[0].length - 1) {
                        map_pixel.put(pixels2D[i + 1][j + 1], convol(i + 1, j + 1, sigma));
                    }
                    if (j > 0) {
                        map_pixel.put(pixels2D[i + 1][j - 1], convol(i + 1, j - 1, sigma));
                    }
                    map_pixel.put(pixels2D[i + 1][j], convol(i + 1, j, sigma));
                }
                if (j > 0) {
                    map_pixel.put(pixels2D[i][j - 1], convol(i, j - 1, sigma));
                }
                if (j < pixels2D[0].length - 1) {
                    map_pixel.put(pixels2D[i][j + 1], convol(i, j + 1, sigma));
                }
                HashMap.Entry<Pixel, Double> maxEntry = null;
                HashMap.Entry<Pixel, Double> minEntry = null;

                for (HashMap.Entry<Pixel, Double> entry : map_pixel.entrySet()) {
                    if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                        maxEntry = entry;
                    }
                    if (minEntry == null || entry.getValue().compareTo(minEntry.getValue()) > 0) {
                        minEntry = entry;
                    }
                }
                if (maxEntry != null) {
                    pixels2DFlou[i][j] = maxEntry.getKey();
                }

                if (minEntry != null) {
                    if (i > 0) {
                        if (j > 0) {
                            pixels2DFlou[i - 1][j - 1] = minEntry.getKey();
                        }
                        if (j < pixels2D[0].length - 1) {
                            pixels2DFlou[i - 1][j + 1] = minEntry.getKey();
                        }
                        pixels2DFlou[i - 1][j] = minEntry.getKey();
                    }
                    if (i < pixels2D.length - 1) {
                        if (j < pixels2D[0].length - 1) {
                            pixels2DFlou[i + 1][j + 1] = minEntry.getKey();
                        }
                        if (j > 0) {
                            pixels2DFlou[i + 1][j - 1] = minEntry.getKey();
                        }
                        pixels2DFlou[i + 1][j] = minEntry.getKey();
                    }
                    if (j > 0) {
                        pixels2DFlou[i][j - 1] = minEntry.getKey();
                    }
                    if (j < pixels2D[0].length - 1) {
                        pixels2DFlou[i][j + 1] = minEntry.getKey();
                    }
                }
            }
        }
        return pixels2DFlou;
    }

//    private Pixel applyFilterToPixel(int i, int j, double[][] filter) {
//        double sum = 0;
//
//
//        // Centre du kernel en I J
//        // Si k = 1 et l = 1 -> centre
//        //for (int k = 0; i < filter.length; k++) {
//        //    for (int l = 0; j < filter[0].length; l++) {
//        Pixel pixel = pixels2D[i][j];
//        int r = pixel.getR();
//        int g = pixel.getG();
//        int b = pixel.getB();
//        int color = new Color(r, g, b).getRGB();
//        sum += color * filter[0][0]; // Fonctionne par le filtre
//        //    }
//        //}
//        Color color1 = new Color(sum);
//        Pixel out = new Pixel(pixel.getA());
//        return out;
//    }


    private double convol(int x, int y, double sigma) {
        return ((Math.exp(-(x * x + y * y) / (2 * sigma * sigma)))) / (2 * Math.PI * sigma * sigma);
    }

    //la meme taille des image
    private Pixel[][] diffImage(Pixel[][] firstImage, Pixel[][] secondImage) {
        Pixel[][] pixelsDiff = new Pixel[firstImage.length][firstImage[0].length];
        for (int i = 0; i < firstImage.length; i++) {
            for (int j = 0; j < firstImage[0].length; j++) {
//                System.out.println(firstImage[i][j]+" " +secondImage[i][j]);
                pixelsDiff[i][j] = firstImage[i][j].diff(secondImage[i][j]);
            }
        }
        return pixelsDiff;
    }

    private void keyPoint() {

        //Log approx + ScaleSpace
        Pixel[][] pixelDiffOctave1 = diffImage(ScaleSpace(Math.sqrt(2) / 2), ScaleSpace(Math.sqrt(2)));
        Pixel[][] pixelDiffOctave2 = diffImage(ScaleSpace(Math.sqrt(2)), ScaleSpace(Math.sqrt(2) * 2));
        Pixel[][] pixelDiffOctave3 = diffImage(ScaleSpace(Math.sqrt(2) * 2), ScaleSpace(Math.sqrt(2) * 4));
        Pixel[][] pixelDiffOctave4 = diffImage(ScaleSpace(Math.sqrt(2) * 4), ScaleSpace(Math.sqrt(2) * 8));

        System.out.println("write");
        writeImage(pixelDiffOctave1, "png", name + "out1.png");
        writeImage(pixelDiffOctave2, "png", name + "out2.png");
        writeImage(pixelDiffOctave3, "png", name + "out3.png");
        writeImage(pixelDiffOctave4, "png", name + "out4.png");
        System.out.println("finwrite");
        Pixel[][] pixelKeyPoint1 = new Pixel[pixelDiffOctave2.length][pixelDiffOctave2[0].length];
        Pixel[][] pixelKeyPoint2 = new Pixel[pixelDiffOctave2.length][pixelDiffOctave2[0].length];

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
                list_point.addAll(listContour(pixelOct1, i, j));
                list_point.addAll(listContour(pixelOct2, i, j));
                list_point.addAll(listContour(pixelOct3, i, j));
                Optional<Pixel> optPmax = list_point.stream().max(comp);
                if (optPmax.isPresent()) {
                    pmax = optPmax.get();
                    pixelKeyPoint[i][j] = pmax;
                }
            }
        }
        return pixelKeyPoint;
    }

    private ArrayList<Pixel> listContour(Pixel[][] pixelOct1, int i, int j) {
        final ArrayList<Pixel> listP = new ArrayList<>();
        listP.add(pixelOct1[i][j]);
        if (i > 0) {
            if (j > 0) {
                listP.add(pixelOct1[i - 1][j - 1]);
            }
            if (j < pixelOct1[0].length - 1) {
                listP.add(pixelOct1[i - 1][j + 1]);
            }
            listP.add(pixelOct1[i - 1][j]);
        }
        if (i < pixelOct1.length - 1) {
            if (j < pixelOct1[0].length - 1) {
                listP.add(pixelOct1[i + 1][j + 1]);
            }
            if (j > 0) {
                listP.add(pixelOct1[i + 1][j - 1]);
            }
            listP.add(pixelOct1[i + 1][j]);
        }
        if (j > 0) {
            listP.add(pixelOct1[i][j - 1]);
        }
        if (j < pixelOct1[0].length - 1) {
            listP.add(pixelOct1[i][j + 1]);
        }
        return listP;
    }

    private void RidBadKeyPoint() {

    }

    private void AssignOrientationKeyPoint() {

    }

    private void GenerateSiftFeature() {

    }


    private String constructName(String path) {
        Path p = Paths.get(path);
        String fileName = p.getFileName().toString();
        int idx = fileName.lastIndexOf('.');
        if (idx > 0)
            fileName = fileName.substring(0, idx);
        return fileName;
    }

    public String getName() {
        return name;
    }

    /**
     * Initialise le tableau de pixel2D
     */
    private void initPixelTab() {
        Optional<Pixel[][]> optbyte = getPixelsTabFromImg();
        optbyte.ifPresent(pixels -> pixels2D = pixels);
    }

    /**
     * Extrait le tableau de bytes de l'image
     *
     * @return Le tableau de bytes contenant les informations
     */
    private Optional<byte[]> extractBytes() {
        // open image
        File imgPath = new File(pathToImage);
        BufferedImage bufferedImage;
        try {
            bufferedImage = ImageIO.read(imgPath);
            width = bufferedImage.getWidth();
            height = bufferedImage.getHeight();
            // get DataBufferBytes from Raster
            WritableRaster raster = bufferedImage.getRaster();
            DataBufferByte data = (DataBufferByte) raster.getDataBuffer();

            return Optional.of(data.getData());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();

    }

    public void writeImage(Pixel[][] p, String type, String pathToImage) {
        BufferedImage bufferedImage = new BufferedImage(p.length, p[0].length, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < p.length; ++i) {
            for (int j = 0; j < p[0].length; ++j) {
                int r = p[i][j].getR();
                int g = p[i][j].getG();
                int b = p[i][j].getB();
                int color = new Color(r, g, b).getRGB();
                bufferedImage.setRGB(i, j, color);
            }
        }
        try {
            ImageIO.write(bufferedImage, type, new File(pathToImage));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return Le tableau de pixel 2D de l'image s'il ny pas de probleme , optional.empty() sinon
     */
    private Optional<Pixel[][]> getPixelsTabFromImg() {
        Optional<byte[]> optbyte = extractBytes();

        if (optbyte.isPresent()) {
            byte[] data = optbyte.get();
            int indAdv = 0;
            if (pathToImage.endsWith("jpg") || pathToImage.endsWith("jpeg")) {
                ArrayList<Pixel> pixels = new ArrayList<>();
                for (int i = 0; i < data.length; i += 3) {
                    int b = getUnsignedIntFromByte(data[i + indAdv]);
                    indAdv++;
                    int g = getUnsignedIntFromByte(data[i + indAdv]);
                    indAdv++;
                    int r = getUnsignedIntFromByte(data[i + indAdv]);
                    indAdv = 0;
                    Pixel p = new Pixel(r, g, b, 0);
                    pixels.add(p);
                }
                return Optional.of(getPixelsFromList(pixels));
            } else {
                ArrayList<Pixel> pixels = new ArrayList<>();
                for (int i = 0; i < data.length; i += 4) {
                    int a = getUnsignedIntFromByte(data[i + indAdv]);
                    indAdv++;
                    int b = getUnsignedIntFromByte(data[i + indAdv]);
                    indAdv++;
                    int g = getUnsignedIntFromByte(data[i + indAdv]);
                    indAdv++;
                    int r = getUnsignedIntFromByte(data[i + indAdv]);
                    indAdv = 0;
                    Pixel p = new Pixel(r, g, b, a);
                    pixels.add(p);
                }
                return Optional.of(getPixelsFromList(pixels));
            }

        }
        return Optional.empty();

    }

    /**
     * Crée un tableau 2D de Pixel à partir de la liste des pixel de l'image
     *
     * @param pixels Liste de tous les pixels de l'image
     * @return Un tableau 2D de pixel
     */
    private Pixel[][] getPixelsFromList(ArrayList<Pixel> pixels) {
        Pixel[][] pixels2D = new Pixel[width][height];
        int x = 0;
        int y = 0;
        for (Pixel p : pixels) {
            pixels2D[x][y] = p;
            x++;
            if (x == width) {
                x = 0;
                y++;
            }
        }
        return pixels2D;
    }

    /**
     * Affiche le tableau de pixel de l'image
     */
    public void printTab() {
        int sizeX = pixels2D.length;
        int sizeY = pixels2D[0].length;
        for (int i = 0; i < sizeY; i++) {
            for (int j = 0; j < sizeX; j++) {
                System.out.println("Tab[" + j + "][" + i + "] =" + pixels2D[j][i]);
            }
        }
    }

    public void printHSV() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                System.out.println("HSV[" + i + "][" + j + "] =" + histoHSV[i][j]);
            }
        }
    }

    private Pixel calcul_moyenne_rgb() {
        int[] p = {0, 0, 0, 0};
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                p[0] += pixels2D[i][j].getR();
                p[1] += pixels2D[i][j].getG();
                p[2] += pixels2D[i][j].getB();
                p[3] += pixels2D[i][j].getA();
            }
        }
        for (int i = 0; i < p.length; i++) {
            p[i] /= (width * height);
        }
        return new Pixel(p[0], p[1], p[2], p[3]);
    }

    private HSV[][] calcul_HSV() {
        HSV[][] histo = new HSV[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                histo[i][j] = recupHSV(pixels2D[i][j]);
            }
        }
        return histo;
    }

    private int[][][] calcul_histo_color() {
        int[][][] hist = new int[5][5][5];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int r = (int) (pixels2D[i][j].getR() / 51f - 0.01);
                int g = (int) (pixels2D[i][j].getG() / 51f - 0.01);
                int b = (int) (pixels2D[i][j].getB() / 51f - 0.01);
                hist[r][g][b]++;
            }
        }
        return hist;
    }

    private HSV recupHSV(Pixel pixel) {
        int r = pixel.getR();
        int g = pixel.getG();
        int b = pixel.getB();
        int max = r > g ? (r > b ? r : b) : (g > b ? g : b);
        int min = r < g ? (r < b ? r : b) : (g < b ? g : b);
        int h = 0;
        if (max != min) {
            if (max == r) {
                h = 60 * ((g - b) / (max - min)) + 360;
            } else if (max == g) {
                h = 60 * ((b - r) / (max - min)) + 120;
            } else {
                h = 60 * ((r - g) / (max - min)) + 240;
            }
        }
        h = h % 360;
        return new HSV(h, max == 0 ? 0 : 1 - min / max, max);
    }

    private static int getUnsignedIntFromByte(byte x) {
        return Byte.toUnsignedInt(x);
    }

    @Override
    public String toString() {
        return "la moyenne rgb de l'image " + pathToImage + " est de " + moyenne_rgb;
    }
}
