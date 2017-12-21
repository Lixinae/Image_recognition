import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Main {


    private static ArrayList<Image> recup_image_database(String pathToFolder) {
        String[] listNomImage = new File(pathToFolder).list();
        if(listNomImage==null){
            System.out.println("dossier base vide");
            throw new NullPointerException();
        }
        ArrayList<Image> listImage = new ArrayList<>();
        for(String nomImage:listNomImage){
            listImage.add(new Image(0,pathToFolder+"/"+nomImage));
        }
        return listImage;
    }


//    public static int[]

    public static void main(String[] args) throws IOException {

        BufferedImage image = new BufferedImage(833, 1200, BufferedImage.TYPE_INT_ARGB);
        ArrayList<Image> image_database = recup_image_database("./image_base");
        String pathToImage = "./image_cible/cible2.jpg";
        Image img = new Image(0, pathToImage);
        double max_diff = 100000;
        String name = "";
        System.out.println(img);
        for(Image i:image_database){
            System.out.println(i);
            double d = i.compare(img);
            System.out.println("différence colorimétrique : "+ d);
            System.out.println("");
            if(max_diff>d){
                max_diff = d;
                name = i.getName();
            }
        }
        System.out.println("la cible est "+name);
    }


}
