package uk.ac.aber.gij2.cs241.Project.techniques;

import java.awt.image.BufferedImage;
import java.io.File;

import uk.ac.aber.gij2.cs241.Project.imageUtilities.ImagePacker;
import uk.ac.aber.gij2.cs241.Project.imageUtilities.ImageUnpacker;

public class ContrastLimitedHistogramEqualisation implements Technique{
	private ImageUnpacker imageUnpacker;
	private int imageX,imageY;

	private double threshold;

	private short[][] inputRed;		//	Three two dimensional arrays represent an intensity map of each colour - [x][y]
	private short[][] inputGreen;
	private short[][] inputBlue;

	private short[][] outputRed;	//	Three two dimensional arrays represent an intensity map of each colour - [x][y]
	private short[][] outputGreen;
	private short[][] outputBlue;


	/*
	 * 	Loads the file in & performs histogram equalisation to the data
	 * 	Utilises Yonghai's algorithm
	 * 
	 * 	@param	File	inputImageFile		The file on which to equalise
	 * 	@param	int		parameter			The threshold must be between 0 & 100
	 */
	public float operation(File inputImageFile,int thresholdParameter,int unusedParameter){
		clear();

		threshold=thresholdParameter;		
		threshold/=100;			//	Getting the threshold as a percent

		imageUnpacker=new ImageUnpacker(inputImageFile);

		long time=System.currentTimeMillis();

		inputRed=imageUnpacker.getRed();
		inputGreen=imageUnpacker.getGreen();
		inputBlue=imageUnpacker.getBlue();

		imageX=imageUnpacker.getX();
		imageY=imageUnpacker.getY();

		int max=(int)(threshold*2*((imageX*imageY)/255));

		outputRed=new short[imageY][imageX];
		outputGreen=new short[imageY][imageX];
		outputBlue=new short[imageY][imageX];

		int histogramRed[]=new int[256];
		int histogramGreen[]=new int[256];
		int histogramBlue[]=new int[256];

		int cumulativeHistogramRed[]=new int[256];
		int cumulativeHistogramGreen[]=new int[256];
		int cumulativeHistogramBlue[]=new int[256];
		
		int redBin=0;
		int greenBin=0;
		int blueBin=0;

		float tempRed,tempGreen,tempBlue,temp;

		for(int i=0;i<256;i++){	//	Defaulting histogram to zeros
			histogramRed[i]=0;
			histogramGreen[i]=0;
			histogramBlue[i]=0;
		}

		for(int currentY=0;currentY<imageY;currentY++){
			for(int currentX=0;currentX<imageX;currentX++){
				histogramRed[inputRed[currentY][currentX]]++;
				histogramGreen[inputGreen[currentY][currentX]]++;
				histogramBlue[inputBlue[currentY][currentX]]++;

			}
		}

		for(int i=0;i<256;i++){	//	Limiting the contrast & accumulating the excess intensity into respective bins
			if(histogramRed[i]>max){
				//	System.out.println("CLHE: limiting R="+i+" "+histogramRed[i]+" to "+max);
				redBin+=(histogramRed[i]-max);
				histogramRed[i]=max;
			}
			if(histogramGreen[i]>max){
				//	System.out.println("CLHE: limiting G="+i+" "+histogramGreen[i]+" to "+max);
				greenBin+=(histogramGreen[i]-max);
				histogramGreen[i]=max;
			}
			if(histogramBlue[i]>max){
				//	System.out.println("CLHE: limiting B="+i+" "+histogramBlue[i]+" to "+max);
				blueBin+=(histogramBlue[i]-max);
				histogramBlue[i]=max;
			}
		}
		
		System.out.println("CLHE: Bins - red="+redBin+" green="+greenBin+" blue="+blueBin);
		
		for(int i=0;i<256;i++){	//	Redistributing the intensity
			histogramRed[i]+=(redBin/256);
			histogramGreen[i]+=(greenBin/256);
			histogramBlue[i]+=(blueBin/256);			
		}

		for(int i=1;i<256;i++){	//	Accumulating histogram
			cumulativeHistogramRed[i]=cumulativeHistogramRed[i-1]+histogramRed[i];
			cumulativeHistogramGreen[i]=cumulativeHistogramGreen[i-1]+histogramGreen[i];
			cumulativeHistogramBlue[i]=cumulativeHistogramBlue[i-1]+histogramBlue[i];
		}
		temp=255/(float)(imageY*imageX);	//	Constant, so precalculated

		for(int currentY=0;currentY<imageY;currentY++){	//	Equalising
			for(int currentX=0;currentX<imageX;currentX++){
				tempRed=(float)cumulativeHistogramRed[inputRed[currentY][currentX]]*temp;
				tempGreen=(float)cumulativeHistogramGreen[inputGreen[currentY][currentX]]*temp;
				tempBlue=(float)cumulativeHistogramBlue[inputBlue[currentY][currentX]]*temp;

				if(tempRed<0) 		tempRed=0;	//	Making sure intensities aren't outside range
				if(tempRed>255) 	tempRed=255;
				if(tempGreen<0) 	tempGreen=0;
				if(tempGreen>255) 	tempGreen=255;
				if(tempBlue<0) 		tempBlue=0;
				if(tempBlue>255) 	tempBlue=255;

				outputRed[currentY][currentX]=(short)tempRed;
				outputGreen[currentY][currentX]=(short)tempGreen;
				outputBlue[currentY][currentX]=(short)tempBlue;
			}
		}
		System.out.println("CLHE: complete - time taken in seconds="+(((float)(System.currentTimeMillis()-time))/1000));

		return (((float)(System.currentTimeMillis()-time))/1000);
	}





	public void clear(){
		try{
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
			System.err.println("CLHE: previously uninitated");
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