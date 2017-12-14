import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Main {


    private static BufferedImage recup_image() throws IOException {
        //recup image dans image_cible
        File f = new File("K:\\users\\kev\\documents\\Image_recognition\\image_cible\\cible1.jpg");
        return ImageIO.read(f);

    }

    private static ArrayList<Integer> recup_image_database(String pathToFolder) {
        //recup image dans image_base

        return new ArrayList<>();
    }


//    public static int[]

    public static void main(String[] args) throws IOException {

        System.out.println("deb");
        BufferedImage image = new BufferedImage(833, 1200, BufferedImage.TYPE_INT_ARGB);
//        File f = null;
//        try {
//            image = recup_image();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        //https://docs.oracle.com/javase/tutorial/2d/images/index.html
        //https://www.programcreek.com/java-api-examples/index.php?class=java.awt.image.BufferedImage&method=setRGB
        //https://docs.oracle.com/javase/7/docs/api/java/awt/image/BufferedImage.html
        ArrayList<Integer> image_id_database = recup_image_database("");
        ArrayList<Image> image_database = new ArrayList<>();
        for (int img : image_id_database) {
            image_database.add(new Image(img,"./image_cible/test.png"));
        }
        Image img =new Image(0,"./image_cible/test.png");
        //img.printTab();
        System.out.println(img);
        img.printHSV();
    }


}
