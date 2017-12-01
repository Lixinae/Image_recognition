import java.util.ArrayList;

public class Main {


    private static int recup_image(){
        //recup image dans image_cible
        return 0;
    }
    private static ArrayList<Integer> recup_image_database(){
        //recup image dans image_base
        return new ArrayList<>();
    }
    public static void main(String[] args){
        System.out.println("deb");
        int image = recup_image();
        Image im = new Image(image);
        ArrayList<Integer> image_id_database = recup_image_database();
        ArrayList<Image> image_database = new ArrayList<>();
        for(int img:image_id_database){
            image_database.add(new Image(img));
        }
    }
}
