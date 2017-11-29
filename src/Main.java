import java.util.ArrayList;

public class Main {


    private static int recup_image(String arg){
        return 0;
    }
    private static ArrayList<Integer> recup_image_database(){
        return new ArrayList<>();
    }
    public static void main(String[] args){
        System.out.println("deb");
        int image = recup_image(args[1]);
        Image im = new Image(image);
        ArrayList<Integer> image_id_database = recup_image_database();
        ArrayList<Image> image_database = new ArrayList<>();
        for(int img:image_id_database){
            image_database.add(new Image(img));
        }
    }
}
