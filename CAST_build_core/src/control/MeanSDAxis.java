package control;

import java.awt.*;
import java.awt.event.*;

import dataView.*;
import axis.*;
import formula.*;


public class MeanSDAxis extends HorizAxis {
	static final public int VALUES = 0;
	static final public int MEAN_SD = 1;
	
	private DensityView linkedView = null;
	private int drawMode = VALUES;
	
	private String kMeanString = "mean";
	
	public MeanSDAxis(XApplet applet) {
		super(applet);
		kMeanString = applet.translate("mean");
	}
	
	public void setView(DensityView linkedView) {
		this.linkedView = linkedView;
	}
	
	public int getDrawMode() {
		return drawMode;
	}
	
	public void setDrawMode(int drawMode) {
		if (drawMode != this.drawMode) {
			this.drawMode = drawMode;
			repaint();
		}
	}

//-----------------------------------------------------------------------------------
	
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
		MouseEvent e2 = translateToViewCoords(e);
		linkedView.mousePressed(e2);
	}

	public void mouseReleased(MouseEvent e) {
		MouseEvent e2 = translateToViewCoords(e);
		linkedView.mouseReleased(e2);
	}

	public void mouseExited(MouseEvent e) {
		linkedView.mouseExited(e);
	}

	public void mouseDragged(MouseEvent e) {
		MouseEvent e2 = translateToViewCoords(e);
		linkedView.mouseDragged(e2);
	}

//-----------------------------------------------------------------------------------
	
	private static final int kBottomBorder = 3;
	private static final int kMeanLineExtra = 3;
	private static final int kSDLineExtra = 6;
	private static final int kArrowHt = 7;
	private static final int kArrowBorder = 2;
	
	public void findAxisWidth() {
		super.findAxisWidth();
		int meanSDHeight = 1 + 3 * (kArrowHt + kArrowBorder) + 2 * ascent + kMeanLineExtra + kBottomBorder;
		axisWidth = Math.max(axisWidth, meanSDHeight);
	}
	
	private void drawArrow(Graphics g, int meanPos, int sdPos, boolean meanOnAxis, boolean sdOnAxis,
																				int sdCount, double labelProportion) {
		int vertPos = 1 + kArrowBorder + kArrowHt / 2 + (3 - sdCount) * (kArrowHt + kArrowBorder);
		int minArrowPos, maxArrowPos;
		boolean minOnAxis, maxOnAxis;
		if (meanPos <= sdPos) {
			minArrowPos = meanPos;
			maxArrowPos = sdPos;
			minOnAxis = meanOnAxis;
			maxOnAxis = sdOnAxis;
		}
		else {
			minArrowPos = sdPos;
			maxArrowPos = meanPos;
			minOnAxis = sdOnAxis;
			maxOnAxis = meanOnAxis;
		}
		g.drawLine(minArrowPos + 1, vertPos, maxArrowPos - 1, vertPos);
		if (minOnAxis) {
			g.drawLine(minArrowPos + 1, vertPos, minArrowPos + 4, vertPos - 3);
			g.drawLine(minArrowPos + 1, vertPos, minArrowPos + 4, vertPos + 3);
		}
		if (minOnAxis) {
			g.drawLine(maxArrowPos - 1, vertPos, maxArrowPos - 4, vertPos - 3);
			g.drawLine(maxArrowPos - 1, vertPos, maxArrowPos - 4, vertPos + 3);
		}
		if (minOnAxis && maxOnAxis) {
			int centrePos = (int)Math.round(meanPos + labelProportion * (sdPos - meanPos));
			String valString = (sdCount == 1) ? "#sigma#" : (sdCount == 2) ? "2#sigma#" : "3#sigma#";
			valString = MText.expandText(valString);
			int stringWidth = g.getFontMetrics().stringWidth(valString);
			g.drawString(valString, centrePos - stringWidth / 2, vertPos + kArrowHt / 2 + 1 + ascent);
		}
	}
	
	public void corePaint(Graphics g) {
		if (drawMode == VALUES)
			super.corePaint(g);
		else {
			double mean = linkedView.getDistnMean().toDouble();
			double sd = linkedView.getDistnSD().toDouble();
			
			int meanSDPos[] = new int[7];
			boolean meanSDOnAxis[] = new boolean[7];
			for (int i=0 ; i<7 ; i++)
				try {
					meanSDPos[i] = lowBorderUsed + numValToPosition(mean + (i - 3) * sd);
					meanSDOnAxis[i] = true;
				} catch (AxisException e) {
					if (e.axisProblem == AxisException.TOO_LOW_ERROR)
						meanSDPos[i] = lowBorderUsed;
					else
						meanSDPos[i] = lowBorderUsed + (axisLength - 1);
				}
				
			g.drawLine(lowBorderUsed, 0, lowBorderUsed + (axisLength - 1), 0);
			
			if (meanSDOnAxis[3]) {
				g.setColor(Color.blue);
				g.drawLine(meanSDPos[3], 1, meanSDPos[3], 3 * (kArrowHt + kArrowBorder)
																							+ ascent + kMeanLineExtra);
				int labelVert = 1 + 3 * (kArrowHt + kArrowBorder) + 2 * ascent + kMeanLineExtra;
				int meanWidth = g.getFontMetrics().stringWidth(kMeanString);
				g.drawString(kMeanString, meanSDPos[3] - meanWidth / 2, labelVert);
			}
			for (int i=0 ; i<7 ; i++)
				if (meanSDOnAxis[i]) {
					if (i == 3) {
						g.setColor(Color.blue);
						g.drawLine(meanSDPos[i], 1, meanSDPos[i], 3 * (kArrowHt + kArrowBorder)
																							+ ascent + kMeanLineExtra);
					}
					else {
						g.setColor(Color.gray);
						g.drawLine(meanSDPos[i], 1, meanSDPos[i],
												(4 - Math.abs(i-3)) * (kArrowHt + kArrowBorder) + kSDLineExtra);
					}
				}
			
			g.setColor(Color.red);
			for (int i=0 ; i<7 ; i++)
				if (i != 3) {
					int sdCount = Math.abs(i-3);
					drawArrow(g, meanSDPos[3], meanSDPos[i], meanSDOnAxis[3], meanSDOnAxis[i], sdCount,
																									1.0 - 1.0 / (2 * sdCount));
				}
			g.setColor(getForeground());
		}
	}
}