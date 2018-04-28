import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {


    private static ArrayList<Image> recup_image_database(String pathToFolder, boolean withSift) {
        String[] listNomImage = new File(pathToFolder).list();
        if (listNomImage == null) {
            System.err.println("dossier base vide");
            throw new NullPointerException();
        }
        ArrayList<Image> listImage = new ArrayList<>();
        for (String nomImage : listNomImage) {
            System.out.println(nomImage);
            listImage.add(new Image(0, pathToFolder + "/" + nomImage, withSift));
        }
        System.out.print("fin récupération image");
        if (withSift) {
            System.out.print(" et des calculs SIFT");
        }
        System.out.println("");
        return listImage;
    }

    private static void cleanDirectory(Path path) throws IOException {
        if(Files.isDirectory(path)){
            Files.list(path).forEach(p->{
                try {
                    Files.delete(p);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public static void main(String[] args) throws IOException {


        boolean comparaisonByColorimetrie = true;
        boolean withSift = true;
        String pathToImageCible = "./image_cible/cible1.jpg";
        /*
        * initialisation des images en base, avec ou sans SIFT
        * */
        List<Image> image_database = init_base_image(withSift);

        /*
        * comparaison par colorimétrie entre l'image cible et les images en bases
        * */
        if (comparaisonByColorimetrie) {
            Image img = new Image(0, pathToImageCible, false);
            compareByColorimetrie(image_database, img);
        }

    }

    private static List<Image> init_base_image(boolean withSift) throws IOException {
        Path path = Paths.get("./image_test");
        cleanDirectory(path);
        return recup_image_database("./image_base", withSift);
    }

    private static void compareByColorimetrie(List<Image> image_database, Image img) {
        Map<Image, Double> mapImage = new HashMap<>();
        image_database.forEach(image1 -> mapImage.put(image1, image1.compare(img)));

        mapImage.entrySet()
                .stream()
                .min((entry1, entry2) -> {
                    double d1 = entry1.getValue(), d2 = entry2.getValue();
                    return Double.compare(d1, d2);
                })
                .ifPresent(val -> System.out.println("Closest Image is : " + val.getKey().getName() + " Value : " + val.getValue()));
    }


}
