package uk.ac.aber.gij2.cs241.Project.imageUtilities;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


public class ImagePacker{
	public static BufferedImage packBufferedImage(int outputY,int outputX,short[][] red,short[][] green,short[][] blue){
		System.out.println("IP: width="+outputX+" height="+outputY);

		int[] pixels=new int[outputY*outputX];
		int index;
		for(int currentY=0;currentY<outputY;currentY++){
			for(int currentX=0;currentX<outputX;currentX++){
				index=(currentY*outputX)+currentX;
				pixels[index]=packPixel(red[currentY][currentX],green[currentY][currentX],blue[currentY][currentX]);
			}
		}

		try{
			BufferedImage outputImage=new BufferedImage(outputX,outputY,BufferedImage.TYPE_INT_RGB);
			for(int currentY=0;currentY<outputY;currentY++){
				for(int currentX=0;currentX<outputX;currentX++){
					index=(currentY*outputX)+currentX;
					outputImage.setRGB(currentX,currentY,pixels[index]);
				}
			}
			System.out.println("IP: BufferedImage packed");
			return outputImage;
		}
		catch(IllegalArgumentException iAE){
			System.out.println("IP: invalid image dimensions");
			return null;
		}
	}

	/*
	 * 	Uses the BufferedImage method & then puts it into a file
	 */
	public static File packFile(int outputY,int outputX,short[][] red,short[][] green,short[][] blue,String fileName){
		File outputFile=new File(fileName);
		try{			
			ImageIO.write(packBufferedImage(outputY,outputX,red,green,blue),"gif",outputFile);
		}
		catch(IOException iOE){
			System.err.println("Somewhere along the line in the packing something went wrong D: ");
		}
		System.out.println("IP: file packed");
		return outputFile;
	}

	/*
	 * 	Packs an individual pixel
	 */
	private static int packPixel(int currentRed,int currentGreen,int currentBlue){
		return ((currentRed<<16)|(currentGreen<<8)|currentBlue);
	}
}