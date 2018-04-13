package exerciseBivar;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import dataView.*;
import utils.*;
import exercise2.*;
import imageUtils.*;


public class Drag4LabelPanel extends XPanel implements LayoutManager, MouseListener, MouseMotionListener, StatusInterface {
	static final public String ITEM_COMPONENT = "Item";
	static final public String LABEL_COMPONENT = "Label";
	
	static final private int kImageBorder = 2;
//	static final private int kMinMouseMove = 20;
	
	private ExerciseApplet applet;
	
	private Component item[] = new Component[4];
	private int nItems = 0;
	
	private Image correctImage[] = new Image[4];
	private String correctXKey[] = new String[4];
	private String correctYKey[] = new String[4];
	
	private XPanel label[] = new XPanel[4];
	private int nLabels = 0;
	
	private int[] messagePermutation;
	
	private int horizGap, vertGap, labelGap;
	
	public Drag4LabelPanel(int horizGap, int vertGap, int labelGap, ExerciseApplet applet) {
		this.horizGap = horizGap;
		this.vertGap = vertGap;
		this.labelGap = labelGap;
		this.applet = applet;
		
		setLayout(this);
	}
	
	public String getStatus() {
		String s = "";
		for (int i=0 ; i<messagePermutation.length ; i++)
			s += messagePermutation[i] + " ";
		return s;
	}
	
	public void setStatus(String statusString) {
		StringTokenizer st = new StringTokenizer(statusString);
		int newPerm[] = new int[st.countTokens()];
		for (int i=0 ; i<newPerm.length ; i++)
			newPerm[i] = Integer.parseInt(st.nextToken());
		changeMessagePermutation(newPerm);
	}
	
	public void setGifMessages(String[] messageGif, int[] messagePermutation) {
		for (int i=0 ; i<4 ; i++) {
			String fileName = "scatterInterp/" + messageGif[messagePermutation[i]] + ".gif";
			ImageCanvas imageLabel = (ImageCanvas)label[i];
			imageLabel.setImage(fileName, applet);
			imageLabel.setBorderColor(applet.getBackground());
		}
		
		this.messagePermutation = messagePermutation;
		
		for (int i=0 ; i<4 ; i++) {
			ImageCanvas imageLabel = (ImageCanvas)label[i];
			correctImage[messagePermutation[i]] = imageLabel.getImage();
		}
		
		for (int i=0 ; i<4 ; i++)
			label[i].repaint();
	}
	
	public void setCorrelKeys(String[] xKey, String[] yKey, int[] messagePermutation) {
		for (int i=0 ; i<4 ; i++) {
			CorrelViewPanel corrLabel = (CorrelViewPanel)label[i];
			corrLabel.changeVariables(yKey[messagePermutation[i]], xKey[messagePermutation[i]]);
			corrLabel.lockBackground(applet.getBackground());
		}
		
		this.messagePermutation = messagePermutation;
		
		for (int i=0 ; i<4 ; i++) {
			correctXKey[i] = xKey[i];
			correctYKey[i] = yKey[i];
		}
		
		for (int i=0 ; i<4 ; i++)
			label[i].repaint();
	}
	
	public void showCorrectMessages() {
		for (int i=0 ; i<4 ; i++) {
			messagePermutation[i] = i;
			if (label[i] instanceof ImageCanvas) {
				ImageCanvas imageLabel = (ImageCanvas)label[i];
				imageLabel.setBorderColor(Color.green);
				imageLabel.setImage(correctImage[i]);
			}
			else {
				CorrelViewPanel corrLabel = (CorrelViewPanel)label[i];
				corrLabel.changeVariables(correctYKey[i], correctXKey[i]);
				corrLabel.lockBackground(Color.green);
			}
		}
	}
	
	private void changeMessagePermutation(int[] newPerm) {
		for (int i=0 ; i<4 ; i++) {
			messagePermutation[i] = newPerm[i];
			if (label[i] instanceof ImageCanvas) {
				ImageCanvas imageLabel = (ImageCanvas)label[i];
				imageLabel.setImage(correctImage[newPerm[i]]);
				imageLabel.setBorderColor(applet.getBackground());
			}
			else {
				CorrelViewPanel corrLabel = (CorrelViewPanel)label[i];
				corrLabel.changeVariables(correctYKey[newPerm[i]], correctXKey[newPerm[i]]);
				corrLabel.lockBackground(applet.getBackground());
			}
		}
	}
	
	public boolean[] checkCorrectMessages() {
		boolean correct[] = new boolean[4];
		for (int i=0 ; i<4 ; i++) {
			correct[i] = messagePermutation[i] == i;
//			if (label[i] instanceof ImageCanvas) {
//				ImageCanvas imageLabel = (ImageCanvas)label[i];
//				imageLabel.setBorderColor(correct[i] ? Color.green : Color.red);
//			}
//			else
//				label[i].lockBackground(correct[i] ? Color.green : Color.red);
//			label[i].repaint();
		}
		return correct;
	}
	
	public void highlightCorrectMessages() {
		for (int i=0 ; i<4 ; i++) {
			boolean correct = messagePermutation[i] == i;
			if (label[i] instanceof ImageCanvas) {
				ImageCanvas imageLabel = (ImageCanvas)label[i];
				imageLabel.setBorderColor(correct ? Color.green : Color.red);
			}
			else
				label[i].lockBackground(correct ? Color.green : Color.red);
			label[i].repaint();
		}
	}
	
//------------------------------------------------------------------------------------
	
	
	private int hitItem = -1;
	private int startX, startY;
	private int xOffset, yOffset;
	
	private void swapLabels(int hitItem, int destItem) {
		int temp = messagePermutation[hitItem];
		messagePermutation[hitItem] = messagePermutation[destItem];
		messagePermutation[destItem] = temp;
		
		if (label[hitItem] instanceof ImageCanvas)
			((ImageCanvas)label[hitItem]).setImage(correctImage[messagePermutation[hitItem]]);
		else
			((CorrelViewPanel)label[hitItem]).changeVariables(correctYKey[messagePermutation[hitItem]],
																												correctXKey[messagePermutation[hitItem]]);
		
		if (label[destItem] instanceof ImageCanvas)
			((ImageCanvas)label[destItem]).setImage(correctImage[messagePermutation[destItem]]);
		else
			((CorrelViewPanel)label[destItem]).changeVariables(correctYKey[messagePermutation[destItem]],
																												correctXKey[messagePermutation[destItem]]);
	}
	
	private MouseEvent convertToPanelCoords(MouseEvent e) {
		Component comp = e.getComponent();
		Component child = null;
		int x = e.getX();
		int y = e.getY();
		while (comp != this) {
			x += comp.getLocation().x;
			y += comp.getLocation().y;
			child = comp;
			comp = comp.getParent();
		}
//		return new MouseEvent(child, 0, 0, x, y, 0, 0);
		return new MouseEvent(child, e.getID(), e.getWhen(), e.getModifiersEx(), x, y, e.getClickCount(), e.isPopupTrigger());
	}
	
	
//------------------------------------------------------------------------------------

	
	public void mouseClicked(MouseEvent e) {
	}
	
	public void mousePressed(MouseEvent e) {
//		Event panelEvent = convertToPanelCoords(e);
//		Component dragComponent = (Component)panelEvent.target;
		MouseEvent panelEvent = convertToPanelCoords(e);
		Component dragComponent = (Component)panelEvent.getSource();
		int x = panelEvent.getX();
		int y = panelEvent.getY();
		
		hitItem = -1;
		for (int i=0 ; i<4 ; i++)
			if (dragComponent == label[i])
				hitItem = i;
		
		if (hitItem != -1) {
			Rectangle hitRect = label[hitItem].getBounds();
			startX = hitRect.x;
			startY = hitRect.y;
			xOffset = x - startX;
			yOffset = y - startY;
			for (int i=0 ; i<4 ; i++) {
				if (label[i] instanceof ImageCanvas)
					((ImageCanvas)label[i]).setBorderColor((hitItem == i) ? Color.yellow : applet.getBackground());
				else
					label[i].lockBackground((hitItem == i) ? Color.yellow : applet.getBackground());
				label[i].repaint();
			}
			if (label[hitItem] instanceof ImageCanvas)
				((ImageCanvas)label[hitItem]).lockBackground(Color.white);		//	so it flashes white when dragged, not orange
			
			applet.noteChangedWorking();
		}
	}
	
	public void mouseDragged(MouseEvent e) {
		MouseEvent panelEvent = convertToPanelCoords(e);
//		Component dragComponent = (Component)panelEvent.target;
		int x = panelEvent.getX();
		int y = panelEvent.getY();
		
		if (hitItem >= 0) {
			Rectangle hitRect = label[hitItem].getBounds();
			int newX = x - xOffset;
			if (newX < 0)
				newX = 0;
			else if (newX + hitRect.width >= getSize().width)
				newX = getSize().width - hitRect.width;
				
			int newY = y - yOffset;
			if (newY < 0)
				newY = 0;
			else if (newY + hitRect.height >= getSize().height)
				newY = getSize().height - hitRect.height;
				
			label[hitItem].setLocation(newX, newY);
		}
	}
	
	public void mouseReleased(MouseEvent e) {
//		Event panelEvent = convertToPanelCoords(e);
//		Component dragComponent = (Component)panelEvent.target;
//		int x = panelEvent.x;
//		int y = panelEvent.y;

		if (hitItem >= 0) {
			Rectangle hitRect = label[hitItem].getBounds();
			int centreX = hitRect.x + hitRect.width / 2;
			int destCol = centreX / (getSize().width / 2);
			int centreY = hitRect.y + hitRect.height / 2;
			int destRow = centreY / (getSize().height / 2);
			int destItem = destCol + destRow * 2;
			
			label[hitItem].lockBackground(applet.getBackground());
			label[hitItem].setLocation(startX, startY);
			
			if (hitItem != destItem) {
				if (label[hitItem] instanceof ImageCanvas)
					((ImageCanvas)label[hitItem]).setBorderColor(applet.getBackground());
				else
					label[hitItem].lockBackground(applet.getBackground());
				
				if (label[destItem] instanceof ImageCanvas)
					((ImageCanvas)label[destItem]).setBorderColor(Color.yellow);
				else
					label[destItem].lockBackground(Color.yellow);
				
				swapLabels(hitItem, destItem);
			}
			
			hitItem = -1;
		}
	}
	
	public void mouseEntered(MouseEvent e) {
	}
	
	public void mouseExited(MouseEvent e) {
	}
	
	public void mouseMoved(MouseEvent e) {
	}	
	
//===================================================================================

	public void addLayoutComponent(String name, Component comp) {
		if (name.equals(ITEM_COMPONENT))
			item[nItems ++] = comp;
		else {
			label[nLabels ++] = (XPanel)comp;
			comp.addMouseListener(this);
			comp.addMouseMotionListener(this);
		}
	}

	public void removeLayoutComponent(Component comp) {
	}
	
	public Dimension minimumLayoutSize(Container parent) {
		return new Dimension(0,0);		//	must be added to centre of BorderLayout
	}
	
	public Dimension preferredLayoutSize(Container parent) {
		return new Dimension(0,0);		//	must be added to centre of BorderLayout
	}
	
	public void layoutContainer(Container parent) {
		Insets insets = parent.getInsets();
		int width = parent.getSize().width;
		int height = parent.getSize().height;
		
		int cellWidth = (width - insets.left - insets.right - horizGap) / 2;
		int cellHeight = (height - insets.top - insets.bottom - vertGap) / 2;
		
		int cellLeft[] = new int[4];
		cellLeft[0] = cellLeft[2] = insets.left;
		cellLeft[1] = cellLeft[3] = insets.left  + cellWidth + horizGap;
		
		int cellTop[] = new int[4];
		cellTop[0] = cellTop[1] = insets.top;
		cellTop[2] = cellTop[3] = insets.top  + cellHeight + vertGap;
		
		int labelHeight = 0;
		for (int i=0 ; i<4 ; i++)
			if (label[i] instanceof ImageCanvas)
				labelHeight = Math.max(labelHeight, ((ImageCanvas)label[i]).imageHeight + 2 * kImageBorder);
			else
				labelHeight = label[i].getMinimumSize().height;
		int itemHeight = cellHeight - labelGap - labelHeight;
		
		for (int i=0 ; i<4 ; i++) {
			item[i].setBounds(cellLeft[i], cellTop[i], cellWidth, itemHeight);
			int labelWidth;
			if (label[i] instanceof ImageCanvas)
				labelWidth = ((ImageCanvas)label[i]).imageWidth + 2 * kImageBorder;
			else
				labelWidth = label[i].getMinimumSize().width;
			int labelLeft = cellLeft[i] + (cellWidth - labelWidth) / 2;
			int labelTop = cellTop[i] + itemHeight + labelGap;
//			if (i == 0)
//				label[i].reshape(labelLeft + 40, labelTop - 20, labelWidth, labelHeight);
//			else
				label[i].setBounds(labelLeft, labelTop, labelWidth, labelHeight);
		}
	}
}