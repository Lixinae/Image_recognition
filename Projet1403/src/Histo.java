import java.io.IOException;

import fr.unistra.pelican.Image;

public class Histo {
	public static double[] CalculHistogramme(Image img) {
		
		double histo[] = new double[256];
		for (int i = 0; i < histo.length; i++) {

		}

		for (int x = 0; x < img.getXDim(); x++) {
			for (int y = 0; y < img.getYDim(); y++) {
				int niveau = img.getPixelXYBByte(x, y, 0);
				histo[niveau]++;
			}
		}
		return histo;
	}

	public static double[][] CalculHistogrammeCouleur(Image img) throws IOException {
		double histo[][] = null;

	
		histo = new double[3][256];

		for (int i = 0; i < histo.length; i++) {
			histo[i][0] = 0;
			histo[i][1] = 0;
			histo[i][2] = 0;
		}
		if (img.getBDim() != 3) {
			System.out.println(img.getXDim());
			System.out.println("pas assez de canaux"); // a cause des pb dans la
			// base broad
		} else {

			for (int x = 0; x < img.getXDim(); x++) {
				for (int y = 0; y < img.getYDim(); y++) {
					int niveauR = img.getPixelXYBByte(x, y, 0);
					histo[0][niveauR]++;

					int niveauG = img.getPixelXYBByte(x, y, 1);
					histo[1][niveauG]++;

					int niveauB = img.getPixelXYBByte(x, y, 2);
					histo[2][niveauB]++;
				}
			}
		}
		return histo;

	}

	public static double[][] Discretisation(double[][] histo) {

		double histoDiscret[][] = new double[3][11];

		for (int i = 0; i < histo.length; i++) {
			histo[i][0] = 0;
			histo[i][1] = 0;
			histo[i][2] = 0;
		}

		for (int i = 0; i < histoDiscret.length; i++) {
			int borneinf = 0;
			int bornemax = 25;

			for (int j = 0; j < histoDiscret[0].length; j++) {

				for (int k = borneinf; k < bornemax; k++) {
					if (k == 256) {
						break;
					}

//					System.out.println(histoDiscret[i][j]);
//					System.out.println(histo[i][k]);
					histoDiscret[i][j] += histo[i][k];
//					System.out.println("bla2 " + k);

				}

				bornemax += 25;
				borneinf += 25;

			}
//			System.out.print("bla");
//			System.out.println(histoDiscret[0][10]);

		}

		return histoDiscret;

	}

	public static double[][] normalisation(double[][] histo, double taille) {
		for (int i = 0; i < histo.length; i++) {
			for (int j = 0; j < histo[0].length; j++) {
				histo[i][j] = histo[i][j] / taille;
			}
		}

		return histo;
	}

}
