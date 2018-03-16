import java.util.ArrayList;
import java.util.List;

public class DoG {

    List<Image> dog = new ArrayList<>();

    public DoG(List<Image> images) {
        if (images.size() != 2) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < images.size(); i++) {
            images.get(i).addSuffixeName("DoG" + i);
        }
        dog.addAll(images);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (Image i : dog) {
            b.append(i.toString());
            b.append("\n");
        }
        return b.toString();
    }

    public List<Image> getDoG() {
        return dog;
    }
}
