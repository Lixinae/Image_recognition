import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.TreeMap;

import fr.unistra.pelican.ByteImage;
import fr.unistra.pelican.DoubleImage;
import fr.unistra.pelican.Image;
import fr.unistra.pelican.algorithms.io.ImageLoader;
import fr.unistra.pelican.algorithms.visualisation.Viewer2D;


public class Traitement2 {
	
	public static TreeMap<Double,String> getTreeMap(double[][] histo,File[] listFile) throws IOException{
		 TreeMap<Double, String> tmap = new TreeMap<Double, String>();
		 ArrayList<Image> listImages = transformerFilesintoArray(listFile) ;
//		 for(Image img:listImages){
//			 tmap.put(listFile[img].getPath(), value)
//		 }
		 for(int i=0;i<listFile.length;i++){
			 double histo2[][] = new double[3][256];
				histo2 = Histo.CalculHistogrammeCouleur(listImages.get(i));
				double histoD[][] = new double[3][11];
				histoD = Histo.Discretisation(histo2);
				double histoN[][] = new double[3][11];
				histoN = Histo.normalisation(histoD, listImages.get(i).getXDim()*listImages.get(i).getYDim());
				double x =Similarity(histo, histoN);
				tmap.put(x,listFile[i].getName() );
		 }
		 return tmap;
	}
	
	public static void Classement(double[][] histo,File[] listFile) throws IOException{
		TreeMap<Double, String> tmap = getTreeMap(histo, listFile);
		  Set<Double> keys = tmap.keySet();
		  int cpt=0;
		for(Double key: keys){
			if(cpt==10){
				break;
			}
           System.out.println("La valeur de "+key+" est: "+tmap.get(key));
           String bdd = "E:/cmp/";
          Image I = ImageLoader.exec(bdd + tmap.get(key));
          Viewer2D.exec(I);
           cpt++;
        }
		
			
			
		
		
	}
	
	public static ArrayList<Image> transformerFilesintoArray(File[] listFile){
		ArrayList<Image> listImages = new ArrayList<Image>();
		for(int i = 0; i<listFile.length; i++){
			Image image = ImageLoader.exec(listFile[i].getPath());
			Median(image);
//			listImages.add(Traitement.Median(image));
			listImages.add(image);
			System.out.println(i);
		}
		return listImages;
	}
	
	public static TreeMap<String, Image> transformerFilesintoTreeMap(File[] listFile) {
		TreeMap<String, Image> listImages = new TreeMap<String, Image>();
		Set<String> keys = listImages.keySet();
		for (int i = 5; i < listFile.length; i++) {
			Image image = ImageLoader.exec(listFile[i].getPath());
			// Image I = Traitement.Median(image);
			listImages.put(listFile[i].getName(), image);

		}
		return listImages;
	}
	


	public static void Enrengistrement(File[] listFile) throws IOException {

		TreeMap<String, Image> listImages = transformerFilesintoTreeMap(listFile);
		Set<String> keys = listImages.keySet();
		// for (int i = 0; i < listImages.size(); i++) {
		for (String key : keys) {
			System.out.println("BLA 3" + key);
			// Traitement.Median(listImages.get(key));
			// Viewer2D.exec(listImages.get(key));
			// System.out.println(listFile.length);
			double histo2[][] = new double[3][256];
			histo2 = Histo.CalculHistogrammeCouleur(listImages.get(key));
			double histoD[][] = new double[3][11];
			histoD = Histo.Discretisation(histo2);
			double histoN[][] = new double[3][11];
			histoN = Histo.normalisation(histoD, listImages.get(key).getXDim() * listImages.get(key).getYDim());

			String path = "E:/res/";

			System.out.println("CHECK HERE " + key);

			String nb = key;
			nb = nb.substring(0, nb.length() - 4);
			nb += ".txt";
			// String PathTT = path + nb ;
			//
			//// System.out.println(PathTT);
			File ff = new File(path + nb);
			// définir"
			// l'arborescence
			ff.createNewFile();
			
			FileWriter ffw = new FileWriter(ff);
			for (int j = 0; j < histoN.length; j++) {
				for (int k = 0; k < histoN[0].length; k++) {
					// pas les bon numéros
					ffw.write(String.valueOf(histoN[j][k]));
					ffw.write(",");
				}
				ffw.write(" ; \n");
			}
			// ffw.write(); // écrire une ligne dans le fichier
			// resultat.txt
			// ffw.write("\n"); // forcer le passage à la ligne
			ffw.close(); // fermer le fichier à la fin des traitements
		}
	}
	public static double Similarity(double H1[][], double H2[][]){
		double x1 = 0.0, x2 = 0.0, x3 = 0.0;
		for(int i = 0; i<H1.length; i++){
			for(int j = 0; j<H1[0].length; j++){
				switch (j) {
				case 0:
					x1 += Math.pow(H1[i][j] - H2[i][j], 2.0);
					break;
				case 1:
					x2 += Math.pow(H1[i][j] - H2[i][j], 2.0);
					break;
				case 2:
					x3 += Math.pow(H1[i][j] - H2[i][j], 2.0);
					break;
				}
			}
		}
		return Math.sqrt(x1) + Math.sqrt(x2) + Math.sqrt(x3);
	}
	public static ByteImage Median(Image i) {

		int nbcanaux = i.getBDim();
		ByteImage res;
		if (nbcanaux == 3) {
			res = new ByteImage(i.getXDim(), i.getYDim(), 1, 1, 3);
			res = (ByteImage) i.copyImage(false);
		} else {
			res = new ByteImage(i.getXDim(), i.getYDim(), 1, 1, 1);
		}

		ArrayList<Integer> value = new ArrayList<Integer>();
		for (int z = 0; z < 3; z++) {
			for (int x = 1; x < i.getXDim() - 2; x++) {
				for (int y = 1; y < i.getYDim() - 2; y++) {

					value.add(i.getPixelXYBByte(x, y, z));
					value.add(i.getPixelXYBByte(x, y - 1, z));
					value.add(i.getPixelXYBByte(x, y + 1, z));
					value.add(i.getPixelXYBByte(x + 1, y, z));
					value.add(i.getPixelXYBByte(x + 1, y - 1, z));
					value.add(i.getPixelXYBByte(x + 1, y + 1, z));
					value.add(i.getPixelXYBByte(x - 1, y, z));
					value.add(i.getPixelXYBByte(x - 1, y - 1, z));
					value.add(i.getPixelXYBByte(x - 1, y + 1, z));
					Collections.sort(value);

					res.setPixelXYBByte(x, y, z, value.get(4));
					value.clear(); // IMPORTANT
				}
			}
		}
		return res;
	}

	public static Image Conversion(Image i) {
		double h, s, v, r, g, b;
		Image img;
		int nbcanaux = i.getBDim();
		if (nbcanaux == 3) {
			img = new DoubleImage(i.getXDim(), i.getYDim(), 1, 1, 3);

		} else {
			img = new DoubleImage(i.getXDim(), i.getYDim(), 1, 1, 1);
		}
		for (int x = 1; x < i.getXDim() - 1; x++) {
			for (int y = 1; y < i.getYDim() - 1; y++) {
				r = i.getPixelXYBByte(x, y, 0);
				g = i.getPixelXYBByte(x, y, 1);
				b = i.getPixelXYBByte(x, y, 2);
				double rx = r / 255;
				double gx = g / 255;
				double bx = b / 255;
				double Cmin = Math.min(Math.min(rx, gx), bx);
				double Cmax = Math.max(Math.max(rx, gx), bx);
				double delta = Cmax - Cmin;
				// V de HSV
				v = Cmax;
				// S de HSV
				if (Cmax != 0)
					s = delta / Cmax;
				else {
					s = 0;
				}
				// H de HSV
				if (rx == Cmax) {
					h = ((gx - bx) / delta) % 6;
				} else if (gx == Cmax) {
					h = 2 + (bx - rx) / delta;
				} else if (delta == 0) {
					h = 0;
				} else {
					h = 4 + (rx - gx) / delta;
				}
				h *= 60;
				if(h<0){
					h+=360;
				}
				img.setPixelXYBDouble(x, y, 0, h);
				img.setPixelXYBDouble(x, y, 1, s);
				img.setPixelXYBDouble(x, y, 2, v);
				img.setColor(true);
			}
		}
		return img;
	}
	
	public static ByteImage Seuillage(Image i, int S){
		ByteImage res = new ByteImage(i.getXDim(), i.getYDim(), 1, 1, 1);
		for(int x = 1; x <i.getXDim()-1; x++){
			for(int y = 1; y <i.getYDim()-1; y++){
				int valueMax = 255;
				int valueMin = 0;
				if(i.getPixelXYBByte(x, y, 0) > S){
					res.setPixelXYBByte(x, y, 0, valueMax);
				} else{
					res.setPixelXYBByte(x, y, 0, valueMin);
				}	
			}
		}
		return res;
	}
}
