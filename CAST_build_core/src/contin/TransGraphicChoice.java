package contin;

import java.awt.*;
import java.awt.event.*;

import dataView.*;


public class TransGraphicChoice extends XPanel implements MouseListener, MouseMotionListener {
	static final public int MARGINAL = 0;
	static final public int CONDITIONAL = 1;
	
	private int transitionType;
	private int currentIndex = 0;
	
	private Rectangle optionBox[] = new Rectangle[3];
	private int probType[] = new int[3];
	private int imageWidth, imageHeight;
	private Image theImage;
	
	private int highlightIndex = -1;
	protected boolean doingDrag = false;
	
	public TransGraphicChoice(int transitionTypeParam, XApplet applet) {
		transitionType = transitionTypeParam;
		if (transitionType == MARGINAL) {
			ContinImages.loadMarginTrans(applet);
			theImage = ContinImages.marginTrans;
			imageWidth = ContinImages.kMTransWidth;
			imageHeight = ContinImages.kMTransHeight;
			probType[0] = RotateContinView.JOINT;
			optionBox[0] = new Rectangle(0, 47, 94, 46);
			probType[1] = RotateContinView.X_MARGIN;
			optionBox[1] = new Rectangle(110, 0, 115, 57);
			probType[2] = RotateContinView.Y_MARGIN;
			optionBox[2] = new Rectangle(110, 83, 115, 57);
		}
		else {
			ContinImages.loadConditTrans(applet);
			theImage = ContinImages.conditTrans;
			imageWidth = ContinImages.kCTransWidth;
			imageHeight = ContinImages.kCTransHeight;
			probType[0] = RotateContinView.JOINT;
			optionBox[0] = new Rectangle(0, 54, 94, 46);
			probType[1] = RotateContinView.X_CONDIT;
			optionBox[1] = new Rectangle(110, 0, 136, 63);
			probType[2] = RotateContinView.Y_CONDIT;
			optionBox[2] = new Rectangle(110, 91, 136, 63);
		}
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	public int getCurrentType() {
		return probType[currentIndex];
	}
	
	public Dimension getPreferredSize() {
		return new Dimension(imageWidth, imageHeight);
	}
	
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		paintChoice(g);
	}
	
	private int getDisabledIndex() {
		int disabledIndex = -1;
		if (probType[currentIndex] == RotateContinView.X_CONDIT) {
			for (int i=0 ; i<probType.length ; i++)
				if (probType[i] == RotateContinView.Y_CONDIT)
					disabledIndex = i;
		}
		else if (probType[currentIndex] == RotateContinView.Y_CONDIT) {
			for (int i=0 ; i<probType.length ; i++)
				if (probType[i] == RotateContinView.X_CONDIT)
					disabledIndex = i;
		}
		return disabledIndex;
	}
	
	public void paintChoice(Graphics g) {
		int disabledIndex = getDisabledIndex();
		for (int i=0 ; i<optionBox.length ; i++) {
			g.setColor(i==currentIndex ? Color.yellow : i==disabledIndex ? Color.lightGray : Color.white);
			g.fillRect(optionBox[i].x, optionBox[i].y, optionBox[i].width, optionBox[i].height);
		}
		
		g.drawImage(theImage, 0, 0, imageWidth, imageHeight, this);
		
		if (doingDrag && highlightIndex >= 0) {
			g.setColor(Color.red);
			g.drawRect(optionBox[highlightIndex].x, optionBox[highlightIndex].y,
							optionBox[highlightIndex].width - 1, optionBox[highlightIndex].height - 1);
			g.drawRect(optionBox[highlightIndex].x + 1, optionBox[highlightIndex].y + 1,
							optionBox[highlightIndex].width - 3, optionBox[highlightIndex].height - 3);
		}
	}

//-----------------------------------------------------------------------------
	
	private int getOption(int x, int y) {
		int disabledIndex = getDisabledIndex();
		for (int i=0 ; i<probType.length ; i++)
			if (i != currentIndex && i != disabledIndex && optionBox[i].contains(x, y))
				return i;
		return -1;
	}

	public void mousePressed(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		int hitOption = getOption(x, y);
		if (hitOption >= 0) {
			highlightIndex = hitOption;
			doingDrag = true;
			repaint();
		}
	}

	public void mouseDragged(MouseEvent e) {
		if (doingDrag) {
			int x = e.getX();
			int y = e.getY();
			int hitOption = getOption(x, y);
			if (hitOption != highlightIndex) {
				highlightIndex = hitOption;
				repaint();
			}
		}
	}

	@SuppressWarnings("deprecation")
	public void mouseReleased(MouseEvent e) {
		if (doingDrag) {
			boolean changedOption = false;
			if (highlightIndex >= 0) {
				currentIndex = highlightIndex;
				highlightIndex = -1;
				changedOption = true;
			}
			doingDrag = false;
			if (changedOption) {
				repaint();
				
				postEvent(new Event(this, Event.ACTION_EVENT, null));
			}
		}
	}

	public void mouseClicked(MouseEvent e) {		//		Not used
	}

	public void mouseEntered(MouseEvent e) {		//		Not used
	}

	public void mouseExited(MouseEvent e) {
		if (doingDrag)
			mouseDragged(e);
	}

	public void mouseMoved(MouseEvent e) {			//		Not used
	}
	
}
