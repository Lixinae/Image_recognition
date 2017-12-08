import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

public class Image {
    private int moyenne_rgb;
    private int data2;
    private int id_image;
    private Pixel[][] pixels2D; // Largeur / Hauteur pour les coordonnées
    private String pathToImage;
    private int width;
    private int height;

    public Image(int id_image, String pathToImage) {
        moyenne_rgb = calcul_moyenne_rgb();
        data2 = calcul_data2();
        this.id_image = id_image;
        this.pathToImage = pathToImage;
        initPixelTab();
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
     *
     * @return Le tableau de pixel 2D de l'image s'il ny pas de probleme , optional.empty() sinon
     */
    private Optional<Pixel[][]> getPixelsTabFromImg() {
        Optional<byte[]> optbyte = extractBytes();
        byte[] data;
        if (optbyte.isPresent()) {
            data = optbyte.get();
            int indAdv = 0;
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
        return Optional.empty();

    }

    /**
     * Crée un tableau 2D de Pixel à partir de la liste des pixel de l'image
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

    private int calcul_moyenne_rgb() {
        return 0;
    }

    private int calcul_data2() {
        return 0;
    }

    private static int getUnsignedIntFromByte(byte x) {
        return Byte.toUnsignedInt(x);
    }
}
