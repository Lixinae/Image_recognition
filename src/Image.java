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
import java.util.Optional;

public class Image {
    private Pixel moyenne_rgb;
    private HSV[][] histoHSV;
    private int id_image = 0;
    private Pixel[][] pixels2D; // Largeur / Hauteur pour les coordonnées
    private String pathToImage = "";
    private String name = "";
    private int width;
    private int height;
    private int[][][] histo_color;
    private double scale = 1.0;
    private double sigma = 0.5;
    private Sift s;

    public Image(int id_image, String pathToImage) {
        this.id_image = id_image;
        this.pathToImage = pathToImage;
        this.name = constructName(pathToImage);
        initPixelTab();
        build();
        s = new Sift(this);
    }

    public Image(Image im, double scale) {
        //TODO modif id image? a voir comment s'en servir
        this.id_image = im.id_image;
        this.pathToImage = im.pathToImage;
        this.name = im.name;
        this.scale = scale;
        this.sigma = im.sigma;
        copyResized(im);
        build();
    }

    /**
     * Image empty
     *
     * @param width
     * @param height
     */
    public Image(int width, int height) {
        this.name = "constructedImage" + width + " " + height;
        this.id_image = -1;
        this.width = width;
        this.height = height;
        this.pixels2D = new Pixel[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                this.pixels2D[i][j] = new Pixel(0, 0, 0, 0);
            }
        }
        build();
    }

    public Image greyCopy() {
        Image imCopy = new Image(this, this.scale);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int val = (int) (pixels2D[i][j].getR() * 0.299 + pixels2D[i][j].getG() * 0.587 + pixels2D[i][j].getB() * 0.114);
                imCopy.setGreyPixel(i, j, val);
            }
        }
        return imCopy;
    }

    private void copyResized(Image im) {
        this.width = (int) (im.width * scale);
        this.height = (int) (im.height * scale);
        this.pixels2D = new Pixel[width][height];
        double x_ratio = im.width / (double) width;
        double y_ratio = im.height / (double) height;
        double px, py;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                px = Math.floor(j * x_ratio);
                py = Math.floor(i * y_ratio);
                pixels2D[i][j] = im.pixels2D[(int) py][(int) px];
            }

        }
    }

    private void build() {
        histoHSV = calcul_HSV();
        moyenne_rgb = calcul_moyenne_rgb();
        histo_color = calcul_histo_color();
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

    public void setGreyPixel(int i, int j, int val) {
        if (val < 0 || val > 255) {
            throw new IllegalArgumentException("grey pixel out of bound");
        }
        pixels2D[i][j] = new Pixel(val);
    }

    public Pixel[][] getTabPixel() {
        return pixels2D;
    }

    public int getGreyPixel(int i, int j) {
        return pixels2D[i][j].getR();
    }

    public double getScale() {
        return scale;
    }

    public double getSigma() {
        return sigma;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public String toString() {
        writeImage(pixels2D, "png", "image_test/" + super.toString() + "_" + "out.png");
        return "image written : " + "image_test/" + name + "_" + "out.png";
    }
}
