package exper;

import java.awt.*;

import dataView.*;
import images.*;

public class MultiImageCanvas extends XPanel {
	private Image image[];
	private int imageHeight, totalWidth;
	private int imageWidth[];
	
	private boolean visible[] = null;
	
	public MultiImageCanvas(String directoryAndFile, String fileSuffix, int nParts, XApplet applet) {
		image = new Image[nParts];
		imageWidth = new int[nParts];
		for (int i=0 ; i<nParts ; i++) {
			String fileDir = directoryAndFile + i + fileSuffix;
			image[i] = CoreImageReader.getImage(fileDir);
		}
		
		MediaTracker mt = new MediaTracker(this);
		for (int i=0 ; i<nParts ; i++)
			mt.addImage(image[i], 0);
			
		try {
			mt.waitForAll();
		} catch (InterruptedException e) {
			System.err.println("could not load image");
		}
		
		totalWidth = 0;
		for (int i=0 ; i<nParts ; i++) {
			imageWidth[i] = image[i].getWidth(null);
			totalWidth += imageWidth[i];
		}
			
		imageHeight = image[0].getHeight(null);
		
		setSize(totalWidth, imageHeight);
	}
	
	public void setVisible(boolean[] visible) {
		this.visible = visible;
		repaint();
	}
	
	public Dimension getPreferredSize() {
		return getSize();
	}
	
	public Dimension getMinimumSize() {
		return getSize();
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		int left = 0;
		for (int i=0 ; i<imageWidth.length ; i++)
			if (visible == null || (visible[i] && image[i] != null)) {
				int partWidth = imageWidth[i];
				g.drawImage(image[i], (getSize().width - totalWidth) / 2 + left, (getSize().height - imageHeight),
																						partWidth, imageHeight, this);
				left += partWidth;
			}
	}
}