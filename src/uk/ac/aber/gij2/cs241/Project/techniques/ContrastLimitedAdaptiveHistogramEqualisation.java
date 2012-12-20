package uk.ac.aber.gij2.cs241.Project.techniques;

import java.awt.image.BufferedImage;
import java.io.File;

import uk.ac.aber.gij2.cs241.Project.imageUtilities.ImagePacker;
import uk.ac.aber.gij2.cs241.Project.imageUtilities.ImageUnpacker;

public class ContrastLimitedAdaptiveHistogramEqualisation implements Technique{
	private ImageUnpacker imageUnpacker;
	private int imageX,imageY;
	private static int frameSize;	//	Static fields so can be used by static submatrix generation method
	public static int MINIMUM_FRAME_SIZE=3,MAXIMUM_FRAME_SIZE=51;	//	Accessed in dialogue

	private double threshold;

	private short[][] inputRed;		//	Three two dimensional arrays represent an intensity map of each colour - [x][y]
	private short[][] inputGreen;
	private short[][] inputBlue;

	private short[][] outputRed;		//	Three two dimensional arrays represent an intensity map of each colour - [x][y]
	private short[][] outputGreen;
	private short[][] outputBlue;

	public float operation(File inputImageFile,int frameSizeParameter,int thresholdParameter){
		clear();

		frameSize=frameSizeParameter;
		int offset=frameSize/2;

		threshold=thresholdParameter;		
		threshold/=100;			//	Getting the threshold as a percent

		imageUnpacker=new ImageUnpacker(inputImageFile);

		long time=System.currentTimeMillis();

		inputRed=imageUnpacker.getRed();
		inputGreen=imageUnpacker.getGreen();
		inputBlue=imageUnpacker.getBlue();

		imageX=imageUnpacker.getX();
		imageY=imageUnpacker.getY();

		int max=(int)(threshold*2*((frameSize*frameSize)/255));

		outputRed=new short[imageY][imageX];
		outputGreen=new short[imageY][imageX];
		outputBlue=new short[imageY][imageX];

		System.out.println("CLAHE: width="+imageX+" height="+imageY+" frame size="+frameSize);

		for(int matrixY=0;matrixY<(imageY);matrixY++){				//	Looping through the matrix
			for(int matrixX=0;matrixX<(imageX);matrixX++){
				outputRed[matrixY][matrixX]=histogramEqualisation(generateSubmatrix(inputRed,frameSize,offset,matrixY,matrixX,imageY,imageX),frameSize,offset,imageY,imageX,max);
				outputGreen[matrixY][matrixX]=histogramEqualisation(generateSubmatrix(inputGreen,frameSize,offset,matrixY,matrixX,imageY,imageX),frameSize,offset,imageY,imageX,max);
				outputBlue[matrixY][matrixX]=histogramEqualisation(generateSubmatrix(inputBlue,frameSize,offset,matrixY,matrixX,imageY,imageX),frameSize,offset,imageY,imageX,max);
			}
		}
		System.out.println("CLAHE: complete - time taken in seconds="+(((float)(System.currentTimeMillis()-time))/1000));

		return (((float)(System.currentTimeMillis()-time))/1000);
	}












	/*
	 * 	Performs histogram equalisation on the 2D array parameter
	 * 
	 * 	@param	short[][]	input		The matrix on which to apply the equalisation
	 * 	@param	int			frameSize	The size of the frame
	 * 	@param	int			offset		The distance from the side of the array to the centre
	 * 	@param	int			imageX		The image width
	 * 	@param	int			imageY		The image height
	 * 	@param	int			max			The limit for contrast limited equalisation
	 * 	
	 * 	@return	short					The histogram equalised value of the centre pixel
	 */
	private static short histogramEqualisation(short[][] input,int frameSize,int offset,int imageX,int imageY,int max){
		float tempColour,temp;

		short[][] output=new short[frameSize][frameSize];

		int histogram[]=new int[256];
		int cumulativeHistogram[]=new int[256];
		
		int bin=0;

		for(int currentY=0;currentY<frameSize;currentY++){
			for(int currentX=0;currentX<frameSize;currentX++){
				histogram[input[currentY][currentX]]++;
			}
		}
		
		for(int i=0;i<256;i++){	//	Limiting the contrast & accumulating the excess intensity into bin
			if(histogram[i]>max){
				bin+=(histogram[i]-max);
				histogram[i]=max;
			}	
		}
		
		//System.out.println("CLAHE: bin="+bin+" redistributing per intensity="+(bin/256));
		
		for(int i=0;i<256;i++){	//	Redistributing the intensity
			histogram[i]+=(bin/256);		
		}

		for(int i=1;i<256;i++){	//	Accumulating histogram
			cumulativeHistogram[i]=cumulativeHistogram[i-1]+histogram[i];
		}
		
		temp=255/(float)(frameSize*frameSize);	//	Constant, so precalculated

		tempColour=(float)cumulativeHistogram[input[offset][offset]]*temp;

		if(tempColour<0) 	tempColour=0;	//	Making sure intensities aren't outside range
		if(tempColour>255) 	tempColour=255;

		output[offset][offset]=(short)tempColour;

		//System.out.println("AHE: start="+input[offset][offset]+" end="+output[offset][offset]);

		return output[offset][offset];
	}





	/*
	 * 	Generates a 2D array based on the neighbours of a specified pixel in a 2D array, mirrors edges if on edge
	 * 
	 * 	@param	short[][]	input		The matrix on which to apply the equalisation
	 * 	@param	int			frameSize	The size of the frame
	 * 	@param	int			offset		The distance from the side of the array to the centre
	 * 	@param	int			matrixY		The y coordinate of the specified pixel in the matrix
	 * 	@param	int			matrixX		The x coordinate of the specified pixel in the matrix
	 * 	@param	int			imageY		The image height
	 * 	@param	int			imageX		The image width
	 * 	
	 * 	@return	short[][]				The submatrix of neighbouring pixels, of size determined by the frameSize parameter
	 */
	private static short[][] generateSubmatrix(short[][] array,int frameSize,int offset,int matrixY,int matrixX,int imageY,int imageX){
		short[][] temp=new short[frameSize][frameSize];

		for(int currentSubmatrixY=(matrixY-offset),submatrixY=0;currentSubmatrixY<(matrixY+offset+1);currentSubmatrixY++,submatrixY++){		//	Building the submatrix
			for(int currentSubmatrixX=(matrixX-offset),submatrixX=0;currentSubmatrixX<(matrixX+offset+1);currentSubmatrixX++,submatrixX++){
				int resultSubmatrixY=currentSubmatrixY,resultSubmatrixX=currentSubmatrixX;

				if(resultSubmatrixY>=imageY)	resultSubmatrixY=((2*imageY)-1)-(currentSubmatrixY+1);							
				if(resultSubmatrixX>=imageX)	resultSubmatrixX=((2*imageX)-1)-(currentSubmatrixX+1);

				//System.out.println("CLAHE: Unfixed matrix y="+currentSubmatrixY+" x="+currentSubmatrixX+"   Matrix y="+resultSubmatrixY+" x="+resultSubmatrixX+"   Submatrix y="+submatrixY+" x="+submatrixX);
				temp[submatrixY][submatrixX]=array[Math.abs(resultSubmatrixY)][Math.abs(resultSubmatrixX)];
			}
		}
		return temp;
	}




	public void clear(){
		try{
			frameSize=0;
			imageX=0;
			imageY=0;
			for(int i=0;i<inputRed.length;i++){
				for(int j=0;j<inputRed[i].length;j++){
					inputRed[i][j]=0;		
					inputGreen[i][j]=0;
					inputBlue[i][j]=0;

					outputRed[i][j]=0;	
					outputGreen[i][j]=0;
					outputBlue[i][j]=0;
				}
			}
		}catch(NullPointerException nPE){
			System.err.println("CLAHE: previously uninitated");
		}
	}

	public File getFile(String fileName){
		return ImagePacker.packFile(imageY,imageX,outputRed,outputGreen,outputBlue,fileName);
	}

	public BufferedImage getBufferedImage(){
		return ImagePacker.packBufferedImage(imageY,imageX,outputRed,outputGreen,outputBlue);
	}	

	public int getWidth(){
		return imageX;
	}
	public int getHeight(){
		return imageY;
	}
}