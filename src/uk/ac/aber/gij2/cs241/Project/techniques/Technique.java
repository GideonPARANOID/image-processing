package uk.ac.aber.gij2.cs241.Project.techniques;

import java.awt.image.BufferedImage;
import java.io.File;

/*
 * 	Describes what a technique needs for the program to function
 */
public interface Technique{
	float operation(File file,int parameter0,int parameter1);	//	Parameter is not always used, returns the time taken
	void clear();												//	Clearing the arrays so the technique can be used again
	File getFile(String fileName);								//	Getting a file out
	BufferedImage getBufferedImage();							//	Getting an image out
	int getWidth();
	int getHeight();
}
