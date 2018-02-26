import javax.imageio.ImageIO;
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
    private int id_image;
    private Pixel[][] pixels2D; // Largeur / Hauteur pour les coordonnées
    private String pathToImage;
    private String name;
    private int width;
    private int height;
    private int[][][] histo_color;

    public Image(int id_image, String pathToImage) {
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

    private void SIFT(){
        findKeyPoint();
        RidBadKeyPoint();
        AssignOrientationKeyPoint();
        GenerateSiftFeature();
    }

    private Pixel[][] ScaleSpace(double sigma) {
        Pixel[][] pixels2DFlou = new Pixel[pixels2D.length][pixels2D[0].length];
        for(int i=0;i<pixels2D.length;i++){
            for (int j=0;j<pixels2D[0].length;j++){
                Double convol = convol(i,j,sigma);
                pixels2DFlou[i][j] = new Pixel(((Double)(pixels2D[i][j].getR()*convol)).intValue()
                                                ,((Double)(pixels2D[i][j].getG()*convol)).intValue()
                                                , ((Double)(pixels2D[i][j].getB()*convol)).intValue()
                                                , ((Double)(pixels2D[i][j].getA()*convol)).intValue()
                                                );
            }
        }
        return pixels2DFlou;
    }

    private double convol(int x,int y, double sigma) {
        return (1/2*Math.PI*sigma*sigma)*((Math.exp(-(x*x+y*y) / 2*sigma*sigma)));
    }

    //la meme taille des image
    private Pixel[][] diffImage(Pixel[][] firstImage, Pixel[][] secondImage) {
        Pixel[][] pixelsDiff = new Pixel[firstImage.length][firstImage[0].length];
        for(int i=0;i<firstImage.length;i++){
            for(int j=0;j<firstImage[0].length;j++){
                pixelsDiff[i][j] = firstImage[i][j].diff(secondImage[i][j]);
            }
        }
        return pixelsDiff;
    }

    private void findKeyPoint() {
        //Log approx + ScaleSpace
        Pixel[][] pixelDiffOctave1 = diffImage(ScaleSpace(Math.sqrt(2)/2),ScaleSpace( Math.sqrt(2)));
        Pixel[][] pixelDiffOctave2 = diffImage(ScaleSpace( Math.sqrt(2)),ScaleSpace(Math.sqrt(2)*2));
        Pixel[][] pixelDiffOctave3 = diffImage(ScaleSpace(Math.sqrt(2)*2),ScaleSpace(Math.sqrt(2)*4));
        Pixel[][] pixelDiffOctave4 = diffImage(ScaleSpace(Math.sqrt(2)*4),ScaleSpace(Math.sqrt(2)*8));
        Pixel[][] pixelKeyPoint1 = new Pixel[pixelDiffOctave2.length][pixelDiffOctave2[0].length];
        Pixel[][] pixelKeyPoint2 = new Pixel[pixelDiffOctave2.length][pixelDiffOctave2[0].length];
        for(int i=0;i<pixelDiffOctave2.length;i++){
            for(int j=0;j<pixelDiffOctave2[0].length;j++){
                
            }
        }
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
