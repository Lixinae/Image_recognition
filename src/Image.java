import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

public class Image {
    private Pixel moyenne_rgb;
    private HSV[][] histoHSV;
    private int id_image;
    private Pixel[][] pixels2D; // Largeur / Hauteur pour les coordonnées
    private String pathToImage;
    private String name;
    private int width;
    private int height;

    public Image(int id_image, String pathToImage) {
        this.id_image = id_image;
        this.pathToImage = pathToImage;
        this.name = constructName(pathToImage);
        initPixelTab();
        histoHSV = calcul_HSV();
        moyenne_rgb = calcul_moyenne_rgb();
    }

    private String constructName(String path) {
        String[] s = path.split("/");
        return s[s.length - 1];
    }

    private String getName() {
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

    /**
     * @return Le tableau de pixel 2D de l'image s'il ny pas de probleme , optional.empty() sinon
     */
    private Optional<Pixel[][]> getPixelsTabFromImg() {
        Optional<byte[]> optbyte = extractBytes();

        if (optbyte.isPresent()) {
            byte[] data = optbyte.get();
            int indAdv = 0;
            if (pathToImage.endsWith("jpg") || pathToImage.endsWith("jpeg")) {
                System.out.println("jpg ending");
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
                System.out.println("png ending");
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
        return "la moyenne rgb de l'image " + name + " est de " + moyenne_rgb;
    }
}
