package uk.ac.aber.gij2.cs241.Project.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;

import uk.ac.aber.gij2.cs241.Project.imageUtilities.ImageHistogramExporter;
import uk.ac.aber.gij2.cs241.Project.techniques.*;

public class GUI extends JFrame implements ActionListener{
	JMenuBar menuBar;
	JMenu fileMenu,editMenu,helpMenu;									//	FM, EM & HM
	JMenuItem importFM,exportFM,exportHistogramFM,exportCumulativeHistogramFM,hEEM,aHEEM,cLHEEM,cLAHEEM,helpHM,creditHM;	//	Suffix denotes which menu it belongs to
	JFileChooser fileSelection;											//	Selecting file/folder to open/save from
	JLabel inputDisplay,outputDisplay,log;								//	Displaying the source, result & details about the operations
	File inputFile,outputFile,outputHistogramFile;						//	IO
	String output;														//	Used for deciding which technique's file building algorithm to use
	ImageFilter imageFilter;

	HistogramEqualisation histogramEqualisation;
	AdaptiveHistogramEqualisation adaptiveHistogramEqualisation;
	ContrastLimitedHistogramEqualisation contrastLimitedHistogramEqualisation;
	ContrastLimitedAdaptiveHistogramEqualisation contrastLimitedAdaptiveHistogramEqualisation;

	/*
	 * 	Main method, simply calls a GUI into existence
	 */
	public static void main(String args[]){
		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			new GUI();
		} 
		catch (Exception e){
			System.err.println("Something went wrong with L&F");
		}		
	}

	public GUI(){	
		histogramEqualisation=new HistogramEqualisation();
		adaptiveHistogramEqualisation=new AdaptiveHistogramEqualisation();
		contrastLimitedHistogramEqualisation=new ContrastLimitedHistogramEqualisation();
		contrastLimitedAdaptiveHistogramEqualisation=new ContrastLimitedAdaptiveHistogramEqualisation();

		imageFilter=new ImageFilter();

		new BorderLayout();
		fileSelection=new JFileChooser();
		fileSelection.setAcceptAllFileFilterUsed(false);

		importFM=new JMenuItem("Import image");
		exportFM=new JMenuItem("Export image");
		exportHistogramFM=new JMenuItem("Export histogram data");
		exportCumulativeHistogramFM=new JMenuItem("Export histogram [cumulative] data");
		importFM.addActionListener(this);
		exportFM.addActionListener(this);
		exportHistogramFM.addActionListener(this);
		exportCumulativeHistogramFM.addActionListener(this);
		fileMenu=new JMenu("File");
		fileMenu.add(importFM);
		fileMenu.add(exportFM);
		fileMenu.add(exportHistogramFM);
		fileMenu.add(exportCumulativeHistogramFM);

		hEEM=new JMenuItem("Histogram equalisation [HE]");
		aHEEM=new JMenuItem("Adaptive histogram equalisation [AHE]");
		cLHEEM=new JMenuItem("Contrast limited histogram equalisation [CLHE]");
		cLAHEEM=new JMenuItem("Contrast limited adaptive histogram equalisation [CLAHE]");
		hEEM.addActionListener(this);
		aHEEM.addActionListener(this);
		cLHEEM.addActionListener(this);
		cLAHEEM.addActionListener(this);
		editMenu=new JMenu("Edit");
		editMenu.add(hEEM);
		editMenu.add(aHEEM);
		editMenu.add(cLHEEM);
		editMenu.add(cLAHEEM);

		helpHM=new JMenuItem("Help");
		creditHM=new JMenuItem("Credit");
		helpHM.addActionListener(this);
		creditHM.addActionListener(this);
		helpMenu=new JMenu("Help");
		helpMenu.add(helpHM);
		helpMenu.add(creditHM);

		menuBar=new JMenuBar();
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(helpMenu);

		inputDisplay=new JLabel();
		inputDisplay.setPreferredSize(new Dimension(600,600));
		outputDisplay=new JLabel();
		outputDisplay.setPreferredSize(new Dimension(600,600));

		log=new JLabel(" No file chosen");
		log.setBorder(BorderFactory.createLineBorder(Color.black));

		this.add(menuBar,BorderLayout.PAGE_START);		
		this.add(inputDisplay,BorderLayout.LINE_START);	//	Left for input image
		this.add(outputDisplay,BorderLayout.LINE_END);	//	Right for output image
		this.add(log,BorderLayout.PAGE_END);

		this.setVisible(true);
		this.setSize(1200,700);
		this.setTitle("Image Processing by Gideon MW Jones [gij2]");
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}




	public void setInputImage(BufferedImage inputImageDisplay,int width,int height){
		try{
			float resultWidth=600,resultHeight=650;
			float ratio=(float)width/(float)height;

			System.out.println("GUI: "+ratio);
			if(height>650){
				resultWidth=(600*ratio);		
			}
			else if(width>600){
				resultHeight=(650*ratio);
			}
			inputDisplay.setIcon(new ImageIcon(inputImageDisplay.getScaledInstance((int)resultWidth,(int)resultHeight,Image.SCALE_SMOOTH)));
		}
		catch(NullPointerException nPE){	//	In case an ImageIcon cannot be formed from the file - likely wrong filetype
			log.setText("Invalid file: "+inputFile.getName());
			inputFile=null;
		}
	}

	public void setOutputImage(BufferedImage outputImageDisplay,int width,int height){
		float resultWidth=600,resultHeight=650;
		float ratio=(float)width/(float)height;

		if(height>650){
			resultWidth=(600*ratio);		
		}
		else if(width>600){
			resultHeight=(650*ratio);
		}
		outputDisplay.setIcon(new ImageIcon(outputImageDisplay.getScaledInstance((int)resultWidth,(int)resultHeight,Image.SCALE_SMOOTH)));
	}



	/*
	 * 	Bringing up a dialogue box to get a parameter for AHE
	 * 
	 * 	@return	int				The entered parameter
	 */
	public static int getFrameSize(){
		int frameSize=0;
		boolean valid=false;

		while(valid==false){
			try{
				frameSize=Integer.parseInt(JOptionPane.showInputDialog(null,"Enter a frame size for adaptive histogram equalisation\n[Cannot be smaller than "+AdaptiveHistogramEqualisation.MINIMUM_FRAME_SIZE+" or greater than "+AdaptiveHistogramEqualisation.MAXIMUM_FRAME_SIZE+"]\nWarning: the larger the frame, the longer it will take\n","Enter a value",JOptionPane.QUESTION_MESSAGE));	//	Converting entered value to an int
				if((frameSize<AdaptiveHistogramEqualisation.MINIMUM_FRAME_SIZE)||(frameSize>AdaptiveHistogramEqualisation.MAXIMUM_FRAME_SIZE)){
					JOptionPane.showMessageDialog(null,"Invalid value","Value disallowed",JOptionPane.ERROR_MESSAGE);
				}
				else{
					valid=true;
				}
			}
			catch(NumberFormatException nFE){
				System.out.println("GUI: parameter cannot be null");
			}
		}		
		return frameSize;
	}


	/*
	 * 	@param	boolean		adaptive		Changes the dialogue depending on whether the parameter is for the adaptive algorithm or not
	 */
	public static int getThreshold(boolean adaptive){
		int threshold=100;
		boolean valid=false;
		while(valid==false){
			try{
				if(adaptive){
					threshold=Integer.parseInt(JOptionPane.showInputDialog(null,"Enter a threshold percent for contrast limited adaptive histogram equalisation\nThe value must be between 0 & 100.","Enter a threshold",JOptionPane.QUESTION_MESSAGE));	//	Converting entered value to an int
				}
				else{
					threshold=Integer.parseInt(JOptionPane.showInputDialog(null,"Enter a threshold percent for contrast limited histogram equalisation\nThe value must be between 0 & 100.","Enter a threshold",JOptionPane.QUESTION_MESSAGE));	//	Converting entered value to an int
				}
				if((threshold<0)||(threshold>100)){
					JOptionPane.showMessageDialog(null,"Invalid value","Value disallowed",JOptionPane.ERROR_MESSAGE);
				}
				else{
					valid=true;
				}
			}
			catch(NumberFormatException nFE){
				System.out.println("GUI: parameter cannot be null");
			}
		}		
		return threshold;
	}






	@Override
	public void actionPerformed(ActionEvent aE){
		try{
			if(aE.getSource()==importFM){		//	Import file to edit
				fileSelection.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileSelection.setAccessory(new ImagePreview(fileSelection));
				fileSelection.setFileFilter(imageFilter);
				int option=fileSelection.showOpenDialog(this);
				if(option==JFileChooser.APPROVE_OPTION){
					inputFile=fileSelection.getSelectedFile();
					BufferedImage inputBufferedImage=ImageIO.read(inputFile);
					this.setInputImage(inputBufferedImage,inputBufferedImage.getWidth(),inputBufferedImage.getHeight());					
					log.setText(" File chosen: "+inputFile.getName());
					try{
						this.setOutputImage(null,0,0);
					}
					catch(NullPointerException nPE){
						System.err.println("GUI: null setting of output image");
					}
				}
			}
			if((aE.getSource()==exportFM)&&(outputFile!=null)){	//	Export edited file
				fileSelection.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				fileSelection.setFileFilter(imageFilter);
				int option=fileSelection.showSaveDialog(this);
				if(option==JFileChooser.APPROVE_OPTION){
					outputFile=fileSelection.getSelectedFile();
					if(output.equals("HE")){	//	Constructing the image from the input in one of these techiques
						outputFile=histogramEqualisation.getFile(outputFile.getPath());	
					}
					else if(output.equals("AHE")){
						outputFile=adaptiveHistogramEqualisation.getFile(outputFile.getPath());			
					}
					else if(output.equals("CLHE")){
						outputFile=contrastLimitedHistogramEqualisation.getFile(outputFile.getPath());			
					}
					else if(output.equals("CLAHE")){
						outputFile=contrastLimitedAdaptiveHistogramEqualisation.getFile(outputFile.getPath());
					}
					log.setText(" File exported: "+outputFile.getPath());
				}
			}
			else if((aE.getSource()==exportHistogramFM)||(aE.getSource()==exportCumulativeHistogramFM)){	//	Export histogram of last operation
				boolean cumulative=false;
				if(aE.getSource()==exportCumulativeHistogramFM)		cumulative=true;

				fileSelection.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				fileSelection.setFileFilter(null);
				int option=fileSelection.showSaveDialog(this);
				if(option==JFileChooser.APPROVE_OPTION){
					outputHistogramFile=fileSelection.getSelectedFile();
					try{
						if(output.equals("HE")){	//	Constructing the image from the input in one of these techiques
							ImageHistogramExporter.export(histogramEqualisation.getBufferedImage(),outputHistogramFile,cumulative);	
						}
						else if(output.equals("AHE")){
							ImageHistogramExporter.export(adaptiveHistogramEqualisation.getBufferedImage(),outputHistogramFile,cumulative);			
						}
						else if(output.equals("CLHE")){
							ImageHistogramExporter.export(contrastLimitedHistogramEqualisation.getBufferedImage(),outputHistogramFile,cumulative);			
						}
						else if(output.equals("CLAHE")){
							ImageHistogramExporter.export(contrastLimitedAdaptiveHistogramEqualisation.getBufferedImage(),outputHistogramFile,cumulative);
						}
					}
					catch(NullPointerException nPE){
						ImageHistogramExporter.export(ImageIO.read(inputFile),outputHistogramFile,cumulative);
					}
					if(aE.getSource()==exportHistogramFM)	log.setText(" Histogram data file exported: "+outputHistogramFile.getPath());
					else if(aE.getSource()==exportCumulativeHistogramFM)	log.setText(" Cumulative histogram data file exported: "+outputHistogramFile.getPath());
				}
			}
			else if(inputFile!=null){			//	Operations
				if(aE.getSource()==hEEM){		//	Histogram equalisation
					log.setText(" Please wait, perfoming histogram equalisation");
					log.setText(" Histogram equalisation complete. Time taken: "+histogramEqualisation.operation(inputFile,0,0)+" seconds");
					this.setOutputImage(histogramEqualisation.getBufferedImage(),histogramEqualisation.getWidth(),histogramEqualisation.getHeight());
					output="HE";
				}
				else if(aE.getSource()==aHEEM){		//	Adaptive histogram equalisation
					log.setText(" Please wait, perfoming adaptive histogram equalisation. This may take a long time, due to massive computational compexity");
					log.setText(" Adaptive histogram equalisation complete. Time taken: "+adaptiveHistogramEqualisation.operation(inputFile,getFrameSize(),0)+" seconds");
					this.setOutputImage(adaptiveHistogramEqualisation.getBufferedImage(),adaptiveHistogramEqualisation.getWidth(),adaptiveHistogramEqualisation.getHeight());
					output="AHE";
				}
				else if(aE.getSource()==cLHEEM){	//	Contrast limited histogram equalisation
					log.setText(" Please wait, perfoming contrast limited histogram equalisation");
					log.setText(" Contrast limited histogram equalisation complete. Time taken: "+contrastLimitedHistogramEqualisation.operation(inputFile,getThreshold(false),0)+" seconds");
					this.setOutputImage(contrastLimitedHistogramEqualisation.getBufferedImage(),contrastLimitedHistogramEqualisation.getWidth(),contrastLimitedHistogramEqualisation.getHeight());
					output="CLHE";
				}
				else if(aE.getSource()==cLAHEEM){
					log.setText(" Please wait, perfoming contrast limited adaptive histogram equalisation");
					log.setText(" Contrast limited adaptive histogram equalisation complete. Time taken: "+contrastLimitedAdaptiveHistogramEqualisation.operation(inputFile,getFrameSize(),getThreshold(true))+" seconds");
					this.setOutputImage(contrastLimitedAdaptiveHistogramEqualisation.getBufferedImage(),contrastLimitedAdaptiveHistogramEqualisation.getWidth(),contrastLimitedAdaptiveHistogramEqualisation.getHeight());
					output="CLAHE";
				}
			}
			else if(inputFile==null){
				log.setText(" No file chosen");
				System.out.println("GUI: no file chosen");
			}
			if(aE.getSource()==helpHM){
				JOptionPane.showMessageDialog(null,"Basic steps:\nImport an image [File>Import image]\nSelect an operation [Edit]\nExport to file [File>Export image]\nOr export to a data file of the cumulative frequency histograms for each colour channel [File/Export histogram data]","Help",JOptionPane.INFORMATION_MESSAGE);
			}
			else if(aE.getSource()==creditHM){
				JOptionPane.showMessageDialog(null,"This program was designed & developed by Gideon MW Jones.\ngideon.jones@ntlworld.com\ngideonparanoid.co.uk","Credit",JOptionPane.INFORMATION_MESSAGE);
			}
		}
		catch(IOException iOE){
			System.err.println("GUI: IOException in listening method");
			iOE.printStackTrace();
		}
	}	
}