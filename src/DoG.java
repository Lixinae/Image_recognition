import java.util.ArrayList;
import java.util.List;

public class DoG {

    List<Image> dog = new ArrayList<>();

    public DoG(List<Image> images) {
        if (images.size() != 2) {
            throw new IllegalArgumentException();
        }
        dog.addAll(images);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (Image i : dog) {
            Pixel[][] p = i.getTabPixel();
            b.append(i.toString());
            b.append("\n");
//            for(int k=0;k<p.length;k++){
//                for(int j=0;j<p[0].length;j++){
//                    if(p[k][j].getR()!=0){
//                        System.out.println(k+" "+j+" "+p[k][j].getR());
//                    }
//                }
//            }
        }
        return b.toString();
    }

    public List<Image> getDoG() {
        return dog;
    }
}
