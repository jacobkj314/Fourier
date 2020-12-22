import java.awt.Color;
import java.util.Arrays;

public class Fourier {
	
	public static final int MIN_FREQUENCY = 20;//currently only working with ints
	public static final int MAX_FREQUENCY = 300;
	
	public static final int MAX_SUBSAMPLE_DURATION = 441; // 1/100 of a second
	
	public static final int SPECTROGRAM_WIDTH = 1024;
	public static final int SPECTROGRAM_HEIGHT = 256;
	
	
	
	//this will find the peak of a given frequency for a given sample
	public static double height(double frequency, double[] samples)
	{
		int length = samples.length;
		double x = 0.0;
		double y = 0.0;
		
		for(int i = 0; i < length; i++)
		{
			x += samples[i] * Math.cos(2 * Math.PI * (1.0/frequency) * i);
			y += samples[i] * Math.sin(2 * Math.PI * (1.0/frequency) * i);
		}
		
		return Math.sqrt(x*x + y*y);		
	}
	
	public static double[][] spectrogram(double[] samples)
	{
		double[][] spectrogram = new double[SPECTROGRAM_HEIGHT][SPECTROGRAM_WIDTH];
		int length = samples.length;
		double sampleJump = (double)length / (double)SPECTROGRAM_WIDTH;
		double frequencyJump = (double)(MAX_FREQUENCY - MIN_FREQUENCY) / (double)SPECTROGRAM_HEIGHT;
		int subsampleDuration = Math.min(length-1, MAX_SUBSAMPLE_DURATION);
		
		for(int widthIndex = 0; widthIndex < SPECTROGRAM_WIDTH; widthIndex++)
		{
			int sampleIndex = (int)(widthIndex * sampleJump);
						
			double[] subsamples = new double[subsampleDuration];
			for(int i = 0; i < subsampleDuration && sampleIndex+i < length; i++)
				subsamples[i] = samples[sampleIndex + i];

			for(int heightIndex = SPECTROGRAM_HEIGHT-1; heightIndex >= 0; heightIndex--)//this one goes from high to low
			{
				int frequency = (int)(MIN_FREQUENCY + frequencyJump * heightIndex);
				spectrogram[heightIndex][widthIndex] = height(frequency, subsamples);
			}
		}
		
		double maxHeight = 0.0;
		for(int i = 0; i < spectrogram.length; i++)
			for(int j = 0; j < spectrogram[0].length; j++)
				if(spectrogram[i][j] > maxHeight)
					maxHeight = spectrogram[i][j];
		for(int i = 0; i < spectrogram.length; i++)
			for(int j = 0; j < spectrogram[0].length; j++)
				spectrogram[i][j] = spectrogram[i][j] / maxHeight;
		
		return spectrogram;
	}
	
	public static Picture display(double[][] spectrogram)
	{
		int height = spectrogram.length;
		int width = spectrogram[0].length;
		
		Picture graph = new Picture(width, height);
		
		for(int col = 0; col < width; col++)
			for(int row = 0; row < height; row++)
				graph.set(col, row, gray(spectrogram[row][col]));
				
		return graph;
	}
	
	public static Color gray(double level)
	{
		int value;
		value = 255 - (int)(255 * level);
		if(value > 255)//this will filter out lighter parts
			value = 255;
		return new Color(value, value, value);
	}
	
	public static void main(String[] args)
	{
		double[] sound = StdAudio.read("cello.wav");
		double[][] spectrogram = spectrogram(sound);
		Picture picture = display(spectrogram);
		picture.show();
	}
}
