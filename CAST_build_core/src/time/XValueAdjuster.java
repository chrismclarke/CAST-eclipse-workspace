package time;

import java.awt.*;
import java.awt.event.*;

import dataView.*;
import imageGroups.*;


abstract public class XValueAdjuster extends XPanel implements MouseListener, MouseMotionListener, KeyListener {
	static private final int kArrowHeight = 21;
	static private final int kArrowWidth = 22;
	static private final int kLabelBoxGap = 5;
	static private final int kValueSideGap = 5;
	static private final int kImageCentreRepeat = 8;
	
	static protected Color darkGray = new Color(0x666666);
	
	static private final int NO_SEL = 0;
	static private final int LEFT_SEL = 1;
	static private final int RIGHT_SEL = 2;
	
	private int minVal, maxVal, currentVal;
		
	protected String label;
	protected int ascent, descent, leading, labelWidth, maxValueWidth;
	
	private boolean initialised = false;
	protected int selected = NO_SEL;
	private boolean doingDrag = false;
	
	public XValueAdjuster(String label, int minVal, int maxVal, int startVal, XApplet applet) {
		ValueAdjusterImages.loadValueAdjuster(applet);
		this.label = label;
		setFont(applet.getStandardBoldFont());
		this.minVal = minVal;
		this.maxVal = maxVal;
		currentVal = startVal;
		
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
	}
	
	public int getValue() {
		return currentVal;
	}
	
	@SuppressWarnings("deprecation")
	public void setValue(int newVal) {
		if (this.currentVal != newVal && newVal >= minVal && newVal <= maxVal) {
			this.currentVal = newVal;
			deliverEvent(new Event(this, Event.ACTION_EVENT, null));
			repaint();
		}
	}
	
	protected int getMinValue() {
		return minVal;
	}
	
	protected int getMaxValue() {
		return maxVal;
	}
	
	protected void setValues(int minVal, int maxVal, int newVal) {
		this.minVal = minVal;
		this.maxVal = maxVal;
		setValue(newVal);
		repaint();
	}
	
	private void initialise(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		ascent = fm.getAscent();
		leading = fm.getLeading();
		descent = fm.getDescent();
		if (label != null)
			labelWidth = fm.stringWidth(label);
		maxValueWidth = getMaxValueWidth(g);
		initialised = true;
	}
	
	abstract protected Value translateValue(int val);
	abstract protected int getMaxValueWidth(Graphics g);
		
	public void paintComponent(Graphics g) {
		Graphics2DActions.setAliasing(g, Graphics2DActions.ANTI_ALIASING_ON);
		super.paintComponent(g);
		
		if (!initialised)
			initialise(g);
		
		paintControl(g);
	}
	
	private int getBaseline() {
		return (getSize().height + ascent - 1) / 2;		//		assumes no values have descent
	}
	
	protected void paintControl(Graphics g) {
		Color textBackground = getParent().getBackground();
		Color textColor = isEnabled() ? Color.black : darkGray;
		
		g.setColor(textBackground);
		g.fillRect(0, 0, getSize().width, getSize().height);
		
		g.setColor(textColor);
		g.drawString(label, 0, getBaseline());
		
		drawLeftArrow(g);
		drawRightArrow(g);
		drawValue(g);
	}
	
	private Point leftArrowTopLeft() {
		return new Point(labelWidth + kLabelBoxGap, (getSize().height - kArrowHeight) / 2);
	}
	
	private void drawLeftArrow(Graphics g) {
		Image im = (!isEnabled() || currentVal <= minVal) ? ValueAdjusterImages.dimLeftArrow
							: (selected == LEFT_SEL) ? ValueAdjusterImages.boldLeftArrow
							: ValueAdjusterImages.leftArrow;
		Point topLeft = leftArrowTopLeft();
		g.drawImage(im, topLeft.x, topLeft.y, this);
	}
	
	private Point rightArrowTopLeft() {
		return new Point(labelWidth + kLabelBoxGap + kArrowWidth + 2 * kValueSideGap + maxValueWidth,
																			(getSize().height - kArrowHeight) / 2);
	}
	
	private void drawRightArrow(Graphics g) {
		Image im = (!isEnabled() || currentVal >= maxVal) ? ValueAdjusterImages.dimRightArrow
							: (selected == RIGHT_SEL) ? ValueAdjusterImages.boldRightArrow
							: ValueAdjusterImages.rightArrow;
		Point topLeft = rightArrowTopLeft();
		g.drawImage(im, topLeft.x, topLeft.y, this);
	}
	
	private void drawValue(Graphics g) {
		Image im = !isEnabled() ? ValueAdjusterImages.dimValueBackground : ValueAdjusterImages.valueBackground;
		int boxTop = (getSize().height - kArrowHeight) / 2;
		int boxLeft = labelWidth + kLabelBoxGap + kArrowWidth;
		int boxWidth = 2 * kValueSideGap + maxValueWidth;
		for (int i=0 ; i<boxWidth/kImageCentreRepeat ; i++)
			g.drawImage(im, boxLeft + i * kImageCentreRepeat, boxTop, this);
		g.drawImage(im, boxLeft + boxWidth - kImageCentreRepeat, boxTop, this);
		
		translateValue(currentVal).drawRight(g, boxLeft + kValueSideGap, getBaseline());
	}
	
	public Dimension getMinimumSize() {
		if (!initialised)
			initialise(getGraphics());
		int width = labelWidth + kLabelBoxGap + 2 * (kArrowWidth + kValueSideGap) + maxValueWidth;
		int height = kArrowHeight;
		return new Dimension(width, height);
	}
	
	public Dimension getPreferredSize() {
		return getMinimumSize();
	}
	
	public void enable() {
		boolean changed = !isEnabled();
		if (changed) {
			super.setEnabled(true);
			repaint();
		}
	}
	
	public void disable() {
		boolean changed = isEnabled();
		if (changed) {
			super.setEnabled(false);
			repaint();
		}
	}
	
	private boolean inArrow(int x, int y, boolean leftNotRight) {
		if (leftNotRight && currentVal == minVal || !leftNotRight && currentVal == maxVal)
			return false;
		Point arrowTopLeft = leftNotRight ? leftArrowTopLeft() : rightArrowTopLeft();
		return x >= arrowTopLeft.x && y >= arrowTopLeft.y
								&& x <= arrowTopLeft.x + kArrowWidth && y <= arrowTopLeft.y + kArrowHeight;
	}

	public void mouseClicked(MouseEvent e) {		//		Not used
	}

	public void mousePressed(MouseEvent e) {
		selected = NO_SEL;
		if (!isEnabled())
			return;
		
		int x = e.getX();
		int y = e.getY();
		
		requestFocus();
		if (inArrow(x, y, true)) {
			doingDrag = true;
			selected = LEFT_SEL;
			drawLeftArrow(getGraphics());
		}
		else if (inArrow(x, y, false)) {
			doingDrag = true;
			selected = RIGHT_SEL;
			drawRightArrow(getGraphics());
		}
	}

	@SuppressWarnings("deprecation")
	public void mouseReleased(MouseEvent e) {
		if (!doingDrag)
			return;
			
		mouseDragged(e);
		
		doingDrag = false;
		if (selected != NO_SEL) {
			if (selected == LEFT_SEL)
				currentVal --;
			else
				currentVal ++;
			selected = NO_SEL;
			repaint();
//			update(getGraphics());
			
			postEvent(new Event(this, Event.ACTION_EVENT, label));
		}
	}

	public void mouseEntered(MouseEvent e) {		//		Not used
	}

	public void mouseExited(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		
		if (!doingDrag)
			return;
		int nextSel = NO_SEL;
		if (inArrow(x, y, true))
			nextSel = LEFT_SEL;
		else if (inArrow(x, y, false))
			nextSel = RIGHT_SEL;
		
		if (nextSel == selected)
			return;
		
		boolean redrawLeft = (selected == LEFT_SEL) || (nextSel == LEFT_SEL);
		boolean redrawRight = (selected == RIGHT_SEL) || (nextSel == RIGHT_SEL);
			
		selected = nextSel;
		if (redrawLeft)
			drawLeftArrow(getGraphics());
		if (redrawRight)
			drawRightArrow(getGraphics());
	}

	public void mouseMoved(MouseEvent e) {			//		Not used
	}

	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		
		if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_DOWN)
			setValue(currentVal - 1);
		else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_UP)
			setValue(currentVal + 1);
	}
	
	public void keyReleased(KeyEvent e) {
	}
	
	public void keyTyped(KeyEvent e) {
	}
	
}