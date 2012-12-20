package uk.ac.aber.gij2.cs241.Project.imageUtilities;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/*
 * 	Exports the current image's cumulative frequency histograms to a file easily readable by database programs
 * 	Space delimited in format:
 * 	Index		channel name		
 * 	Intensity	frequency		
 */
public class ImageHistogramExporter{
	public static void export(BufferedImage bufferedImage,File outputHistogramFile,boolean cumulative){
		try{
			ImageUnpacker imageUnpacker=new ImageUnpacker(bufferedImage);

			FileWriter fileWriter=new FileWriter(outputHistogramFile.getAbsolutePath());
			BufferedWriter bufferedWriter=new BufferedWriter(fileWriter);

			int[] histogramRed=getHistogramChannel(imageUnpacker.getRed(),cumulative);
			int[] histogramGreen=getHistogramChannel(imageUnpacker.getGreen(),cumulative);
			int[] histogramBlue=getHistogramChannel(imageUnpacker.getBlue(),cumulative);

			bufferedWriter.write("Index Red Green Blue");
			bufferedWriter.newLine();
			for(int i=0;i<histogramRed.length;i++){
				bufferedWriter.write(((Integer)i).toString()+" "+((Integer)histogramRed[i]).toString()+" "+((Integer)histogramGreen[i]).toString()+" "+((Integer)histogramBlue[i]).toString());
				bufferedWriter.newLine();
			}

			bufferedWriter.close();

			System.out.println("IHE: image histogram exported");
		}catch (Exception e){	//	Catch exception if any
			System.err.println("IHE: error: " + e.getMessage());
		}
	}

	/*
	 * 	Gets the histogram from a 2D channel array of an image
	 * 
	 * 	@param	short[][]	channel		The 2D array on which to perform the operation
	 * 	@param	boolean		cumulative	Determines whether the method returns a cumulative array or not
	 * 
	 * 	@return int[]					The histogram in the format of an int array
	 */
	private static int[] getHistogramChannel(short[][] channel,boolean cumulative){
		int[] histogram=new int[256],cumulativeHistogram=new int[256];

		for(int i=0;i<256;i++){	//	Defaulting histogram to zeros
			histogram[i]=0;
		}

		for(int currentY=0;currentY<channel.length;currentY++){
			for(int currentX=0;currentX<channel[currentY].length;currentX++){
				histogram[channel[currentY][currentX]]++;
			}
		}		

		if(cumulative==true){
			for(int i=1;i<256;i++){	//	Accumulating histogram
				cumulativeHistogram[i]=cumulativeHistogram[i-1]+histogram[i];
			}		
			histogram=cumulativeHistogram;
		}
		return histogram;
	}
}