import java.util.ArrayList;

public class main {


    public static int recup_image(String arg){
        return 0;
    }
    public static ArrayList<Integer> recup_image_database(){
        ArrayList<Integer> test = new ArrayList<>();
        return test;
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
