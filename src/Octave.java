import java.util.ArrayList;
import java.util.List;

public class Octave {
    List<Image> octave = new ArrayList<>();

    public Octave(List<Image> images) {
        if (images.size() != 5) {
            throw new IllegalArgumentException();
        }
        octave.addAll(images);
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (Image i : octave) {
            b.append(i.toString());
            b.append("\n");
        }
        return b.toString();
    }

    public List<Image> getOctave() {
        return octave;
    }
}
