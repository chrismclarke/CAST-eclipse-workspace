package axis;

import java.awt.*;
import java.awt.event.*;

import dataView.*;


public class DragValAxis extends HorizAxis {
	private NumValue axisVal;
	private Color valueColor = Color.red;
	
	private boolean allowDrag = true;
	
	public DragValAxis(XApplet applet) {
		super(applet);
	}
	
	public void setValueColor(Color valueColor) {
		this.valueColor = valueColor;
	}
	
	public void setAllowDrag(boolean allowDrag) {
		this.allowDrag = allowDrag;
	}
	
	public NumValue getAxisVal() {
		if (!allowDrag)
			return null;
		if (axisVal == null)
			try {
				axisVal = positionToNeatNumVal(axisLength / 3);
			} catch (AxisException e) {
			}
		return axisVal;
	}
	
	public int getAxisValPos() throws AxisException {
		return numValToPosition(getAxisVal().toDouble());
	}
	
	public void setAxisVal(NumValue newVal) throws AxisException {
		if (newVal.toDouble() < minOnAxis)
			throw new AxisException(AxisException.TOO_LOW_ERROR);
		else if (newVal.toDouble() > maxOnAxis)
			throw new AxisException(AxisException.TOO_HIGH_ERROR);
		else
			axisVal = newVal;
		repaint();
		if (linkedView != null)
			linkedView.repaint();
		if (otherLinkedViews != null) {
			for (int i=0 ; i<otherLinkedViews.length ; i++)
				otherLinkedViews[i].repaint();
		}
	}
	
	static private final int kSigDigits = 4;
	
	public void setAxisVal(double newVal) throws AxisException {
		double range = maxOnAxis - minOnAxis;
		int decimals = kSigDigits;
		while (range < 1.0) {
			range *= 10.0;
			decimals++;
		}
		while (range >= 10.0) {
			range *= 0.1;
			decimals--;
		}
		if (decimals < 0)
			decimals = 0;
		
		setAxisVal(new NumValue(newVal, decimals));
	}
	
	public void setAxisValPos(int newPos) throws AxisException {
		setAxisVal(positionToNeatNumVal(newPos));
	}

//-----------------------------------------------------------------------------------
	
	private double roundedIndex(double rawVal, double resolution) {
		return Math.floor(Math.rint(rawVal / resolution) / 10.0);
	}
	
	public NumValue positionToNeatNumVal(int thePosition) throws AxisException {
		if (thePosition < 0)
			throw new AxisException(AxisException.TOO_LOW_ERROR);
		else if (thePosition >= axisLength)
			throw new AxisException(AxisException.TOO_HIGH_ERROR);
		else {
			double pixWidth = (maxOnAxis - minOnAxis) / (axisLength - 1);
			double scaledPixWidth = pixWidth;
			double resolution = 1.0;
			int decimals = 0;
			while (scaledPixWidth < 1.0) {
				resolution *= 0.1;
				scaledPixWidth *= 10.0;
				decimals++;
			}
			while (scaledPixWidth >= 10.0) {
				resolution *= 10.0;
				scaledPixWidth *= 0.1;
			}
			
			double idealVal = minOnAxis + thePosition * pixWidth;
			double thisIndex = roundedIndex(idealVal, resolution);
			int tempPos = thePosition;
			int countBelow = -1;				//	so that current pos does not get counted
			while (roundedIndex(minOnAxis + tempPos * pixWidth, resolution) == thisIndex) {
					countBelow++;
					tempPos--;
				}
			tempPos = thePosition;
			int countAbove = -1;				//	so that current pos does not get counted
			while (roundedIndex(minOnAxis + tempPos * pixWidth, resolution) == thisIndex) {
					countAbove++;
					tempPos++;
				}
			
			double newValue = Math.rint(thisIndex * 10.0 + (10.0 * countBelow) / (countBelow + countAbove + 1)) * resolution;
			int newPos = numValToPosition(newValue);
//			while (newPos > thePosition) {
//				newValue -= resolution;
//				newPos = numValToPosition(newValue);
//			}
			while (newPos < thePosition) {
				newValue += resolution;
				newPos = numValToPosition(newValue);
			}
			
			return new NumValue(newValue, decimals);
		}
	}

//-----------------------------------------------------------------------------------
	
	private DataView linkedView = null;
	private DataView otherLinkedViews[] = null;
	
	public void setView(DataView linkedView) {
		this.linkedView = linkedView;
	}
	
	public void setOtherLinkedViews(DataView[] otherLinkedViews) {
		this.otherLinkedViews = otherLinkedViews;
	}
	
	private MouseEvent translateToViewCoords(MouseEvent e) {
		if (getParent() != linkedView.getParent())
			return null;
		Point axisOrigin = getLocation();
		Point viewOrigin = linkedView.getLocation();
		
		int x = e.getX() + axisOrigin.x - viewOrigin.x;
//		int y = e.getY() + axisOrigin.y - viewOrigin.y;
		int y = 20;				//		20 makes sure position is in linkedView
		
		return new MouseEvent(e.getComponent(), e.getID(), e.getWhen(), e.getModifiersEx(),
														x, y, e.getClickCount(), e.isPopupTrigger());
	}

//-----------------------------------------------------------------------------------

	public void mousePressed(MouseEvent e) {
		if (allowDrag) {
			MouseEvent e2 = translateToViewCoords(e);
			linkedView.mousePressed(e2);
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (allowDrag) {
			MouseEvent e2 = translateToViewCoords(e);
			linkedView.mouseReleased(e2);
		}
	}

	public void mouseExited(MouseEvent e) {
		if (allowDrag)
			linkedView.mouseExited(e);
	}

	public void mouseDragged(MouseEvent e) {
		if (allowDrag) {
			MouseEvent e2 = translateToViewCoords(e);
			linkedView.mouseDragged(e2);
		}
	}

//-----------------------------------------------------------------------------------
	
	protected String getConstName() {
		return "k";
	}
	
	private int valHeight;
	private static final int kValueBorder = 2;
	
	public void findAxisWidth() {
		super.findAxisWidth();
		valHeight = ascent + descent + 2 * kValueBorder;
		axisWidth += valHeight;
	}
	
	public void corePaint(Graphics g) {
		if (allowDrag)
			try {
				NumValue theValue = getAxisVal();
				int markedPos = lowBorderUsed + numValToPosition(theValue.toDouble());
				if (((DragViewInterface)linkedView).getDoingDrag()) {
					g.setColor(Color.yellow);
					g.fillRect(markedPos - 2, 0, 5, getSize().height - valHeight);
				}
				g.setColor(valueColor);
				g.drawLine(markedPos, 0, markedPos, getSize().height - valHeight - 1);
				
				int kEqualsWidth = g.getFontMetrics().stringWidth(getConstName() + " = ");
				int valWidth = kEqualsWidth + theValue.stringWidth(g) + 2 * kValueBorder;
				int valBoxStart = markedPos - valWidth / 2;
				if (valBoxStart < 0)
					valBoxStart = 0;
				if (valBoxStart + valWidth >= getSize().width)
					valBoxStart = getSize().width - valWidth;
				
				g.drawString(getConstName() + " = ", valBoxStart, getSize().height - 2 - descent);
				valBoxStart += kEqualsWidth;
				valWidth -= kEqualsWidth;
				g.drawRect(valBoxStart, getSize().height - valHeight, valWidth - 1,
																											valHeight - 1);
				g.setColor(Color.white);
				g.fillRect(valBoxStart + 1, getSize().height - valHeight + 1,
																							valWidth - 2, valHeight - 2);
				g.setColor(valueColor);
				theValue.drawRight(g, valBoxStart + 2, getSize().height - 2 - descent);
			} catch (AxisException e) {
			}
		g.setColor(getForeground());
		super.corePaint(g);
	}
}