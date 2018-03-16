import java.util.ArrayList;
import java.util.List;

public class TheCon {


    List<Image> con = new ArrayList<>();

    public TheCon(List<Image> images) {
        if (images.size() != 4) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < images.size(); i++) {
            images.get(i).addSuffixeName("Con" + i);
        }
        con.addAll(images);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (Image i : con) {
            b.append(i.toString());
            b.append("\n");
        }
        return b.toString();
    }

    public List<Image> getCon() {
        return con;
    }

}
