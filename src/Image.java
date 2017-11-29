public class Image {
    private int moyenne_rgb;
    private int data2;
    private int id_image;
    private String name;

    public Image(int id_image){
        moyenne_rgb = calcul_moyenne_rgb();
        data2 = calcul_data2();
        this.id_image = id_image;
        name="";
    }
    private int calcul_moyenne_rgb(){
        return 0;
    }
    private int calcul_data2(){
        return 0;
    }
}
