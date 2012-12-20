package uk.ac.aber.gij2.cs241.Project.imageUtilities;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


public class ImageUnpacker {
	private int imageX,imageY;

	private int[] pixels;

	private short[][] red;		//	Three two dimensional arrays represent an intensity map of each colour - [x][y]
	private short[][] green;
	private short[][] blue;


	/*
	 * 	Front for the private image unpacking method
	 */
	public ImageUnpacker(File imageFile){
		try{
			ImageUnpacker(ImageIO.read(imageFile));	
		}
		catch(IOException iOE){
			System.err.println("Somewhere along the line in the unpacking something went wrong D: ");
		}
	}

	/*
	 * 	Front for the private image unpacking method
	 */
	public ImageUnpacker(BufferedImage image){
		ImageUnpacker(image);
	}

	private void ImageUnpacker(BufferedImage image){
		int index;

		imageX=image.getWidth();
		imageY=image.getHeight();

		System.out.println("IU: source image - width="+imageX+" height="+imageY+" pixels="+imageX*imageY);

		red=new short[imageY][imageX];
		green=new short[imageY][imageX];
		blue=new short[imageY][imageX];

		pixels=image.getRGB(0,0,image.getWidth(),image.getHeight(),null,0,image.getWidth());	// 	Getting the pixels as ints of the form 0xRRGGBB

		for(int currentY=0; currentY<imageY; currentY++){
			for(int currentX=0; currentX<imageX; currentX++){
				index=(currentY*imageX)+currentX;
				unpackPixel(pixels[index],red,green,blue,currentY,currentX);
			}
		}		
		System.out.println("IU: image unpacked");

	}

	private static void unpackPixel(int currentPixel,short [][] red,short [][] green,short [][] blue,int currentY,int currentX){
		red[currentY][currentX]=(short)((currentPixel>>16)&0xFF);
		green[currentY][currentX]=(short)((currentPixel>>8)&0xFF);
		blue[currentY][currentX]=(short)((currentPixel>>0)&0xFF);
		//System.out.println("Unpacker: pixel #"+currentPixel+" ("+currentX+","+currentY+") - R="+red[currentY][currentX]+" G="+green[currentY][currentX]+" B="+blue[currentY][currentX]);
	}



	public int getX() {
		return imageX;
	}
	public int getY() {
		return imageY;
	}
	public int[] getPixels() {
		return pixels;
	}
	public short[][] getRed() {
		return red;
	}
	public short[][] getGreen() {
		return green;
	}
	public short[][] getBlue() {
		return blue;
	}	
}