package structure;

import java.awt.*;
import java.awt.image.*;

import dataView.*;
import images.*;

public class NoiseImageCanvas extends XPanel {
	private Image image, noisyImage;
	public int imageWidth, imageHeight;
	
	AddNoiseFilter noiseFilter;
	
	public NoiseImageCanvas(String directoryAndFile, XApplet applet) {
		MediaTracker mt = new MediaTracker(this);
			image = CoreImageReader.getImage(directoryAndFile);
		mt.addImage(image, 0);
		try {
			mt.waitForAll();
		} catch (InterruptedException e) {
			System.err.println("could not load image");
		}
		imageWidth = image.getWidth(null);
		imageHeight = image.getHeight(null);
		
		noiseFilter = new AddNoiseFilter(imageWidth, imageHeight, 0.0);
		noisyImage = createImage(new FilteredImageSource(image.getSource(), noiseFilter));
		
		setSize(imageWidth, imageHeight);
	}
	
	public void setNoisePropn(double noisePropn) {
		noiseFilter.setNoisePropn(noisePropn);
		noisyImage.flush();
		repaint();
	}
	
	public Dimension getPreferredSize() {
		return getSize();
	}
	
	public Dimension getMinimumSize() {
		return getSize();
	}
	
	public void paintComponent(Graphics g) {
//		super.paintComponent(g);						//	calling super.paintComponent() causes flicker
		if (noisyImage != null)
			g.drawImage(noisyImage, (getSize().width - imageWidth) / 2, (getSize().height - imageHeight) / 2,
																					imageWidth, imageHeight, this);
	}
}