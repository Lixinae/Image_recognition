import java.io.IOException;

import fr.unistra.pelican.Image;

public class TraitementHSV {
	public static double[][] CalculHistogrammeCouleur(Image img) throws IOException {
		if (img.getBDim() != 3) {
			System.out.println(img.getXDim());
			System.out.println("pas assez de canaux"); // a cause des pb dans la
														// base broad
		}
		double histo[][] = new double[361][3];

		for (int i = 0; i < histo.length; i++) {
			histo[i][0] = 0;
			histo[i][1] = 0;
			histo[i][2] = 0;
		}

		for (int x = 0; x < img.getXDim(); x++) {
			for (int y = 0; y < img.getYDim(); y++) {
				int niveauH = (int) img.getPixelXYBDouble(x, y, 0);
				histo[niveauH][0]++;

				int niveauS = (int) img.getPixelXYBDouble(x, y, 1)*100;
				histo[niveauS][1]++;

				int niveauV = (int) img.getPixelXYBDouble(x, y, 2)*100;
				histo[niveauV][2]++;
			}
		}
		return histo;
	}
}
