package ebook;

import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;

import pageStructure.*;

public class UiImage extends JPanel{
	static final public int STD_IMAGE = 0;
	static final public int DIM_IMAGE = 1;
	static final public int BOLD_IMAGE = 2;
	
	private File[] imageFile;
	private boolean retinaImage;								// retinaImage means that image is double size for retina displays
	private boolean usePageScaling = false;
	private BufferedImage[] bi;
	private int currentImage = STD_IMAGE;
	
	private int fixedWidth=0, fixedHeight=0;
	

	public UiImage(File stdFile, File dimFile, File boldFile, boolean retinaImage) {
		File[] theImageFile = new File[3];
		theImageFile[STD_IMAGE] = stdFile;
		theImageFile[DIM_IMAGE] = dimFile;
		theImageFile[BOLD_IMAGE] = boldFile;
		setupImages(theImageFile, retinaImage, true);
	}
	
	public UiImage(File stdFile, boolean retinaImage) {
		File[] theImageFile = new File[1];
		theImageFile[0] = stdFile;
		setupImages(theImageFile, retinaImage, false);
	}
	
	public UiImage(File[] files, boolean retinaImage) {
		setupImages(files, retinaImage, false);
	}
	
	public UiImage(File stdFile, int fixedWidth, int fixedHeight) {
		this.fixedWidth = fixedWidth;
		this.fixedHeight = fixedHeight;
		
		File[] theImageFile = new File[1];
		theImageFile[0] = stdFile;
		setupImages(theImageFile, false, false);
	}
	
	public void setPageScaling(boolean usePageScaling) {
		this.usePageScaling = usePageScaling;
	}
	
	
	private void setupImages(File[] files, boolean retinaImage, boolean isButton) {
		setOpaque(false);
		
		imageFile = files;
		this.retinaImage = retinaImage;
		bi = new BufferedImage[files.length];
		
		if (isButton)
			addMouseListener(new MouseListener() {
								public void mouseReleased(MouseEvent e) {}
								public void mousePressed(MouseEvent e) {}
								public void mouseExited(MouseEvent e) {
									if (currentImage != DIM_IMAGE) {
										setImage(STD_IMAGE);
										setCursor(Cursor.getDefaultCursor());
									}
								}
								public void mouseEntered(MouseEvent e) {
									if (currentImage != DIM_IMAGE) {
										setImage(BOLD_IMAGE);
										setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
									}
								}
								public void mouseClicked(MouseEvent e) {
									if (currentImage != DIM_IMAGE)
										doClickAction();
								}
							});
	}
	
	
	public void setImage(int newImage) {
		currentImage = newImage;
		repaint();
	}
	
	public int getImage() {
		return currentImage;
	}
	
	protected void doClickAction() {
	}
	
	private void readImage() {
		if (bi[currentImage] == null)
			try {
				bi[currentImage] = ImageIO.read(imageFile[currentImage]);
			} catch (IOException ex) {
				try {
					System.err.println("Could not read image from file: " + imageFile[currentImage].getCanonicalPath());
				} catch (IOException ex2) {
				}
			}
	}


	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;
    RenderingHints rh = new RenderingHints(
             RenderingHints.KEY_INTERPOLATION,
             RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    g2.setRenderingHints(rh);
		readImage();
		g2.drawImage(bi[currentImage], 0, 0, getWidth(), getHeight(), null);
	}

	public Dimension getPreferredSize() {
		readImage();
		int scaledWidth, scaledHeight;
		if (fixedWidth == 0) {
			int factor = retinaImage ? 2 : 1;
			scaledWidth = bi[currentImage].getWidth() / factor;
			scaledHeight = bi[currentImage].getHeight() / factor;
		}
		else {
			scaledWidth = fixedWidth;
			scaledHeight = fixedHeight;
		}
		if (usePageScaling) {
			scaledWidth = CoreDrawer.scaledSize(scaledWidth);
			scaledHeight = CoreDrawer.scaledSize(scaledHeight);
		}
		return new Dimension(scaledWidth, scaledHeight);
	}

	public Dimension getMinimumSize() {
		return getPreferredSize();
	}
}