
import java.io.File;
import java.io.IOException;

import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;

public class Application {

	public static void main(String[] args) throws IOException {
		System.setProperty("file.encoding", "UTF-8");
		// Image Imagehsv = ImageLoader.exec("C:/Users/osman_000/Desktop/Java
		// workspace/cougar_face/image_0001.jpg");
		// Image Imagehsv2 = ImageLoader.exec("C:/Users/osman_000/Desktop/Java
		// workspace/cougar_face/image_0003.jpg");
		// Image test = Traitement.Conversion(Imagehsv);
		// Image test2 = Traitement.Conversion(Imagehsv2);
		// //Viewer2D.exec(test);
		// double histo[][] = new double[3][360];
		// double histo2[][] = new double[3][360];
		// histo = TraitementHSV.CalculHistogrammeCouleur(test);
		// histo2 = TraitementHSV.CalculHistogrammeCouleur(test2);
		// HistogramTools.plotHistogram(histo[1], "Niveau de H");
		// double histo[][] = new double[3][256];
		// histo = Histo.CalculHistogrammeCouleur(test);
		// double histoD[][] = new double[3][11];
		// histoD = Histo.Discretisation(histo);
		// double histoN[][] = new double[3][11];
		// histoN = Histo.normalisation(histoD, test.getXDim()*test.getYDim());
		// HistogramTools.plotHistogram(histoN[0], "Niveau de Rouge");
		// HistogramTools.plotHistogram(histoN[1], "Niveau de Vert");
		// HistogramTools.plotHistogram(histoN[2], "Niveau de Bleu");
		// String path = "G:/S4/IA/Projet/projet/images/motos/motos";
		// File[] listFile = DirectoryReader.stockFile(path);
		// Traitement2.Classement(histoN, listFile);
		// double x = Traitement2.Similarity(histo, histo2);
		// x /= 10000000;
		// System.out.println(x);

		// test enrengistrement

		String bdd = "E:/cmp";
		// String ImageNumber = "/image_0001.jpg";
		// String ImageInitiale = bdd + ImageNumber;
//		String ImageInitiale = "G:/cougar_face/image_0001.jpg";
		File[] listFile = DirectoryReader.stockFile(bdd);
		Image test = ImageLoader.exec("E:/cougar_face/image_0001.jpg");
		Image test1 = ImageLoader.exec("E:/Motorbikes/image_0022.jpg");

		// HistogramTools.plotHistogram(histo);
		double histo[][] = new double[3][256];
		histo = Histo.CalculHistogrammeCouleur(test);
		double histoD[][] = new double[3][11];
		histoD = Histo.Discretisation(histo);
		double histoN[][] = new double[3][11];
		histoN = Histo.normalisation(histoD, test.getXDim() * test.getYDim());
		for (int i = 0; i < histoN.length; i++) {
			for (int j = 0; j < histoN[0].length; j++) {
				System.out.print(histoN[i][j] + ";");
			}
			System.out.println();
		}
		 Traitement2.Enrengistrement(listFile);
		Traitement2.Classement(histoN, listFile);
		 Viewer2D.exec(test);
		
		System.out.println("COMPTEUR");
		performence(test, test1);
	}

	public static void performence(Image I1, Image I2) throws IOException {

		// Partie HSV

		Image Res1hsv = Traitement2.Conversion(I1);
		Image Res2hsv = Traitement2.Conversion(I2);
		double HSVhisto[][] = new double[3][360];
		double HSVhisto2[][] = new double[3][360];
		HSVhisto = TraitementHSV.CalculHistogrammeCouleur(Res1hsv);
		HSVhisto2 = TraitementHSV.CalculHistogrammeCouleur(Res2hsv);
		double x = Traitement2.Similarity(HSVhisto, HSVhisto2); // similarité en
																// hsv
		x /= 1000;

		// Partie RGB

		double RGBhisto[][] = new double[3][256];
		double RGBhisto2[][] = new double[3][256];
		RGBhisto = Histo.CalculHistogrammeCouleur(I1);
		RGBhisto2 = Histo.CalculHistogrammeCouleur(I2);
		double histoN1[][] = new double[3][11];
		histoN1 = Histo.normalisation(Histo.Discretisation(RGBhisto), I1.getXDim() * I1.getYDim());
		double histoN2[][] = new double[3][11];
		histoN2 = Histo.normalisation(Histo.Discretisation(RGBhisto2), I2.getXDim() * I2.getYDim());
		double y = Traitement2.Similarity(histoN1, histoN2); // similarité en
																// rgb
		y *= 100;
		System.out.println("La similarité en hsv est de :" + x + "\n La valeur de similarité en rgb est de :" + y);
	}
}
