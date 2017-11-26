

import java.io.File;
import java.io.FilenameFilter;

public class DirectoryReader {

	public static File[] stockFile(String directoryPath) {
		File dir = new File(directoryPath);
		File[] files = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".png")) {
					return true;
				} else {
					return false;
				}
			}
		});
		return files;
	}

	/*
	 * public File[] listFiles(String directoryPath) { File[] files = null; File
	 * directoryToScan = new File(directoryPath); files =
	 * directoryToScan.listFiles(); return files; }
	 * 
	 * private String getFileExtension(File f) { String name = f.getName(); try
	 * { return name.substring(name.lastIndexOf(".") + 1); } catch (Exception e)
	 * { return ""; } }
	 * 
	 * public boolean isOk(File[] files){
	 * 
	 * for(int i = 0; i < files.length; i++){ String ext =
	 * getFileExtension(files[i]); if(ext != "jpg" || ext != "jpeg" || ext !=
	 * "png"){ files[i].delete(); } } return true; }
	 */
}