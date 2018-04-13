package imageUtils;

import java.awt.*;

import dataView.*;
import images.*;

public class ImageCanvas extends XPanel {
	private Image image;
	public int imageWidth, imageHeight;
	
	private Color borderColor;
	
	public ImageCanvas(String directoryAndFile, XApplet applet) {
		setImage(directoryAndFile, applet);
		
		setSize(imageWidth, imageHeight);
	}
	
	public ImageCanvas(Image image, int imageWidth, int imageHeight, XApplet applet) {
		
		this.image = image;
		this.imageWidth = imageWidth;
		this.imageHeight = imageHeight;
		
		setSize(imageWidth, imageHeight);
	}
	
	public void setImage(String directoryAndFile, XApplet applet) {
		image = CoreImageReader.getImage(directoryAndFile);
		
		MediaTracker mt = new MediaTracker(this);
		mt.addImage(image, 0);
		try {
			mt.waitForAll();
		} catch (InterruptedException e) {
			System.err.println("could not load image");
		}
		imageWidth = image.getWidth(null);
		imageHeight = image.getHeight(null);
	}
	
	public Image getImage() {
		return image;
	}
	
	public void setImage(Image image) {
		this.image = image;
		repaint();
	}
	
	public void setBorderColor(Color borderColor) {
		this.borderColor = borderColor;			//		if ImageCanvas is dragged, backgroundColor is
															//		flashed, so it is sometimes better to have a
															//		separate borderColor. (see JexerciseBivar.Drag4LabelPanel)
	}
	
	public Dimension getPreferredSize() {
		return getSize();
	}
	
	public Dimension getMinimumSize() {
		return getSize();
	}
		
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
//		System.out.println("Painting image: " + image);
		if (image == null) {
			g.setColor(getBackground());
			g.fillRect(0, 0, getSize().width, getSize().height);
		}
		else {
			g.setColor(borderColor == null ? getBackground() : borderColor);
			int left = (getSize().width - imageWidth) / 2;
			int top = (getSize().height - imageHeight) / 2;
			if (top > 0)
				g.fillRect(0, 0, getSize().width, top);
			if (left > 0)
				g.fillRect(0, 0, left, getSize().height);
			if (top + imageHeight < getSize().height)
				g.fillRect(0, top + imageHeight, getSize().width, getSize().height - top - imageHeight);
			if (left + imageWidth < getSize().width)
				g.fillRect(left + imageWidth, 0, getSize().width - left - imageWidth, getSize().height);
//			System.out.println("Painting image, left = " + left + ", top = " + top + ", imageWidth = " + imageWidth + ", imageHeight = " + imageHeight);
			g.drawImage(image, left, top, imageWidth, imageHeight, this);
		}
	}
}