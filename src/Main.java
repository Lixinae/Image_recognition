import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {


    private static ArrayList<Image> recup_image_database(String pathToFolder) {
        String[] listNomImage = new File(pathToFolder).list();
        if (listNomImage == null) {
            System.out.println("dossier base vide");
            throw new NullPointerException();
        }
        ArrayList<Image> listImage = new ArrayList<>();
        for (String nomImage : listNomImage) {
            System.out.println(nomImage);
            listImage.add(new Image(0, pathToFolder + "/" + nomImage));
        }
        System.out.println("test");
        return listImage;
    }


//    public static int[]

    public static void main(String[] args) throws IOException {

        BufferedImage image = new BufferedImage(833, 1200, BufferedImage.TYPE_INT_ARGB);
        List<Image> image_database = recup_image_database("./image_base");
        String pathToImage = "./image_cible/cible1.jpg";
        int id = 0;
        Image img = new Image(id, pathToImage);

//        double max_diff = 1000000;
//        String name = "";
//        System.out.println(img);
//        for (Image i : image_database) {
//            System.out.println(i);
//            double d = i.compare(img);
//            System.out.println("différence colorimétrique : " + d);
//            System.out.println("");
//            if (max_diff > d) {
//                max_diff = d;
//                name = i.getName();
//            }
//        }
//        System.out.println("la cible est " + name);

        System.out.println("///////////////////////////");

        Map<Image, Double> mapImage = new HashMap<>();
//        image_database.forEach(image1 -> mapImage.put(image1, image1.compare(img)));
        image_database.forEach(image1 -> mapImage.put(image1, Compare.compare(image1, img)));

        // todo -> utiliser la moyenne plutot que 1 à 1
        mapImage.entrySet()
                .stream()
                .min((entry1, entry2) -> {
                    double d1 = entry1.getValue(), d2 = entry2.getValue();
                    return Double.compare(d1, d2);
                })
                .ifPresent(val ->System.out.println("Closest is : "+val.getKey().getName() + " Value : "+val.getValue()));

    }


}
