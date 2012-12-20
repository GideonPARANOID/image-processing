package uk.ac.aber.gij2.cs241.Project.gui;

import java.io.File;
import javax.swing.filechooser.*;

/* 
 *	http://docs.oracle.com/javase/tutorial/uiswing/examples/components/index.html#FileChooserDemo2 
 */
public class ImageFilter extends FileFilter{

	public boolean accept(File file){
		boolean result = false;
		if(file.isDirectory()){
			return true;
		}
		String extension=ImageFilter.getExtension(file);
		if(extension!=null){
			if((extension.equals("tiff"))||(extension.equals("tif"))||(extension.equals("gif"))||(extension.equals("jpeg"))||(extension.equals("jpg"))||(extension.equals("png"))){
				result=true;
			}
		}
		return result;
	}

	/*
	 * Description of the filter
	 */
	public String getDescription(){
		return "Images [tiff, tif, gif, jpeg, jpg, png]";
	}    

	public static String getExtension(File file){
		String extension=null;
		String fileName=file.getName();
		int i=fileName.lastIndexOf('.');

		if((i>0)&&(i<fileName.length()-1)){
			extension=fileName.substring(i+1).toLowerCase();
		}
		return extension;
	}
}