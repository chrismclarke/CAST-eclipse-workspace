package dataView;

import java.awt.*;
import java.awt.event.*;

abstract public class BufferedCanvas extends XPanel implements MouseListener, MouseMotionListener, KeyListener, FocusListener {
	static public boolean isJavaUpToDate = javaUpToDate();
	
	@SuppressWarnings("unused")
	static private boolean javaUpToDate() {
		try {
			Class jButtonClass = Class.forName("javax.swing.JButton");
			Class graphics2DClass = Class.forName("java.awt.Graphics2D");
			Class renderingHintsClass = Class.forName("java.awt.RenderingHints");
			Class affineTransformsClass = Class.forName("java.awt.geom.AffineTransform");
			return true;
		}
		catch (Exception e) {
			return false;
		}
	}
	
	static public void checkAliasing(Graphics g) {
		Graphics2DActions.setAliasing(g, Graphics2DActions.ANTI_ALIASING_ON);
	}
	
	protected boolean isVisible = true;
	private boolean inCardLayout = false;
	
	public BufferedCanvas(XApplet applet) {
		setFont(applet.getStandardFont());
		
		addMouseListener(this);
		addMouseMotionListener(this);
		addKeyListener(this);
		addFocusListener(this);
	}

//-----------------------------------------------------------------------------------
	
	public void setInCardLayout(boolean inCardLayout) {
		this.inCardLayout = inCardLayout;
	}
	
	@SuppressWarnings("deprecation")
	public void show(boolean showNotHide) {
		if (inCardLayout)			//	ordinary show() must be used for component of CardLayout
			super.show(showNotHide);
		else {			//	private version of show() since hidden component is not laid out
			if (isVisible != showNotHide) {
				isVisible = showNotHide;
				repaint();
			}
		}
	}

//-----------------------------------------------------------------------------------
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if (getSize().width <= 0 || getSize().height <= 0)		//	not laid out yet
			return;
		
		checkAliasing(g);
		
		if (isVisible)
			corePaint(g);
	}
	
	public void paintChildren(Graphics g) {
		if (isVisible)
			super.paintChildren(g);
	}
	
	abstract public void corePaint(Graphics g);

//-----------------------------------------------------------------------------------

	abstract protected boolean canDrag();
	abstract protected boolean needsHitToDrag();
	
	protected PositionInfo getPosition(int x, int y) {
		return null;
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		return getPosition(x, y);
	}
	
	abstract protected boolean startDrag(PositionInfo startInfo);
	
	abstract protected void doDrag(PositionInfo fromPos, PositionInfo toPos);
	
	abstract protected void endDrag(PositionInfo startPos, PositionInfo endPos);
	
	private PositionInfo lastItem = null;
	private PositionInfo hitItem = null;		//		implementors of coreGetPosition() need it, so "protected"
	protected boolean doingDrag = false;
	protected boolean shiftKeyDown = false;
	private boolean movedFromHit;
	private int startX, startY;
	private static final int kMinMove = 8;
	
	private boolean stickyDrag = false;
	
	public void setStickyDrag(boolean stickyDrag) {
		this.stickyDrag = stickyDrag;
	}
	
	protected int getMinMouseMove() {
		return kMinMove;
	}
	

	public void mouseClicked(MouseEvent e) {		//		Not used
	}

	public void mousePressed(MouseEvent e) {
		if (canDrag()) {
			int x = e.getX();
			int y = e.getY();
			shiftKeyDown = e.isShiftDown();
			
			doingDrag = false;
//			shiftKeyDown = (evt.modifiers & Event.SHIFT_MASK) != 0;
			hitItem = getInitialPosition(x, y);
			if (hitItem == null && needsHitToDrag())
				return;
			
			startX = x;
			startY = y;
			movedFromHit = false;
			lastItem = hitItem;
			
			doingDrag = startDrag(hitItem);
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (doingDrag) {
			mouseDragged(e);
			
			endDrag(hitItem, lastItem);
			doingDrag = false;
		}
	}

	public void mouseEntered(MouseEvent e) {		//		Not used
	}

	public void mouseExited(MouseEvent e) {
		if (doingDrag)
			mouseDragged(e);
	}

	public void mouseDragged(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		
		if (!doingDrag || (!movedFromHit && (x-startX) * (x-startX) + (y-startY) * (y-startY) <= getMinMouseMove()))
			return;
		movedFromHit = true;
		PositionInfo nextIndex = getPosition(x, y);
		if (stickyDrag && (nextIndex == null) || samePositions(lastItem, nextIndex))
			return;
		
		doDrag(lastItem, nextIndex);
		lastItem = nextIndex;
	}

	public void mouseMoved(MouseEvent e) {			//		Not used
	}
	
	protected boolean samePositions(PositionInfo pos1, PositionInfo pos2) {
		if (pos1 == null) {
			if (pos2 == null)
				return true;
		}
		else if (pos1.equals(pos2))
			return true;
		return false;
	}

//******************************
	
/*
	final public boolean mouseDown(Event evt, int x, int y) {		//	to throw compile error for AWT event handlers
		return false;
	}
	
	final public boolean mouseDrag(Event evt, int x, int y) {		//	to throw compile error for AWT event handlers
		return false;
	}
	
	final public boolean mouseUp(Event evt, int x, int y) {		//	to throw compile error for AWT event handlers
		return false;
	}
	
	final public boolean mouseExit(Event evt, int x, int y) {		//	to throw compile error for AWT event handlers
		return false;
	}
*/
	
//**************************************************************

	public void keyPressed(KeyEvent e) {		//	arrow keys cause keyPressed() not keyTyped()
	}
	
	public void keyReleased(KeyEvent e) {
	}
	
	public void keyTyped(KeyEvent e) {
	}
	
/*
	final public boolean keyDown(Event evt, int key) {		//	to throw compile error for AWT event handlers
		return false;
	}
*/
	
//**************************************************************
	
	public void focusGained(FocusEvent e) {
	}
	
	public void focusLost(FocusEvent e) {
	}
	
/*
	final public boolean lostFocus(Event evt, Object what) {		//	to throw compile error for AWT event handlers
		return true;
	}
	
	final public boolean gotFocus(Event evt, Object what) {		//	to throw compile error for AWT event handlers
		return true;
	}
*/
}