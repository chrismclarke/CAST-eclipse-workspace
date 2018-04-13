package variance;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import coreGraphics.*;


public class HalfNormPowerView extends MarginalDataView {
//	static final private int kZSteps = 100;
	static final private double kNormalHtPropn = 0.25;
	static final private int kMinDensityHt = 150;
	static final private int kNoOfShades = 50;
	
	
//	private double cumProb[];
	
	private Color fillColor = Color.blue;
	private Color highlightColor = Color.lightGray;
	private Color fillShade[] = new Color[kNoOfShades + 1];
	private Color highlightShade[] = new Color[kNoOfShades + 1];
	
	private boolean doingDrag = false;
	private double selectedVal = 1.0;
	
	public HalfNormPowerView(DataSet theData, XApplet applet, NumCatAxis axis) {
		super(theData, applet, new Insets(0, 0, 0, 0), axis);
																//		no border under histo
//		initialiseCumProbs();
		AccurateDistn2Artist.setShades(fillShade, fillColor);
		AccurateDistn2Artist.setShades(highlightShade, highlightColor);
	}
	
//	private void initialiseCumProbs() {
//		cumProb = new double[kZSteps + 2];
//		double zMax = axis.maxOnAxis;
//		for (int i=0 ; i<=kZSteps ; i++)
//			cumProb[i] = 2.0 * NormalTable.cumulative(i * zMax / kZSteps) - 1.0;
//	}
	
	private double getCumProb(double z) {
//		double zMax = axis.maxOnAxis;
//		int lowIndex = (int)Math.round(Math.floor(z * kZSteps / zMax));
//		double lowCum = cumProb[lowIndex];
//		double highCum = cumProb[lowIndex + 1];
//		double propn = z * kZSteps / zMax - lowIndex;
//		return lowCum + propn * (highCum - lowCum);
		
		return 2.0 * NormalTable.cumulative(z) - 1.0;
	}
	
	private double getPixelProbAtZero() {
		double zMax = axis.maxOnAxis;
		int nPixels = getSize().width;
		return 2.0 * NormalTable.cumulative(zMax / nPixels) - 1.0;
	}
	
	private void setFillShade(double p, boolean highlight, Graphics g) {
		int topShade = (int)Math.round(kNoOfShades * p);
		g.setColor(highlight ? highlightShade[topShade] : fillShade[topShade]);
	}
	
	public void paintView(Graphics g) {
														//		Drawing algorithm assumes pdf is monotonic decreasing
		int selectedPos = axis.numValToRawPosition(selectedVal);
		
		int nPixels = getSize().width;
		int pixHt = getSize().height;
		double scaling = pixHt * kNormalHtPropn / getPixelProbAtZero() ;
		
		double previousPix = Double.POSITIVE_INFINITY;
		double cumToThis = 0.0;
		try {
			cumToThis = getCumProb(axis.positionToNumVal(1));
		} catch (AxisException e) {
		}
		double thisPix = cumToThis * scaling;
		
		for (int i=1 ; i<=nPixels ; i++)
			try {
				boolean selected = (i-1) <= selectedPos;
				double z = axis.positionToNumVal(i+1);
				double cumToNext = getCumProb(z);
				double nextProb = cumToNext - cumToThis;
				cumToThis = cumToNext;
				double nextPix = nextProb * scaling;
				
				int yPix = (int)Math.round(Math.floor(thisPix));
				setFillShade(1.0, selected, g);										//	full colour
				g.drawLine(i-1, pixHt - yPix, i-1, pixHt);
				
				double halfMinStep = Math.min(Math.abs(previousPix - thisPix),
																													Math.abs(thisPix - nextPix)) * 0.5;
				if (halfMinStep <= 1.0 || ((thisPix > previousPix) == (thisPix > nextPix))) {
					setFillShade(thisPix - yPix, selected, g);
					g.fillRect(i-1, pixHt - yPix - 1, 1, 1);
				}
				else {
					boolean goingUp = thisPix < nextPix;
					
					double top = thisPix + halfMinStep;
					double bottom = thisPix - halfMinStep;
					
					double floor = Math.floor(top);
					int pix = pixHt - (int)Math.round(floor);
					double topShade = (top - floor) * (top - floor) / (top - bottom) * 0.5;
					if (goingUp)
						topShade = 1.0 - topShade;
					setFillShade(topShade, selected, g);
					g.fillRect(i-1, pix, 1, 1);
					
					floor -= 1.0;
					pix ++;
					while (floor > bottom) {
						double shade = (top - floor - 0.5) / (top - bottom);
						if (goingUp)
							shade = 1.0 - shade;
						setFillShade(shade, selected, g);
						g.fillRect(i-1, pix, 1, 1);
						floor -= 1.0;
						pix ++;
					}
					
					double ceil = floor + 1.0;
					double bottomShade = 1.0 - (ceil - bottom) * (ceil - bottom) / (top - bottom) * 0.5;
					if (goingUp)
						bottomShade = 1.0 - bottomShade;
					setFillShade(bottomShade, selected, g);
					g.fillRect(i-1, pix, 1, 1);
				}
				
				previousPix = thisPix;
				thisPix = nextPix;
			} catch (AxisException e) {
			}
		
		if (doingDrag) {
			g.setColor(Color.red);
			g.drawLine(selectedPos, 0, selectedPos, getSize().height);
		}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < 0 || y < 0 || x >= getSize().width || y >= getSize().height)
			return null;
		
		return new HorizDragPosInfo(x);			//	Insets are all zero, to no need for translateFromScreen()
	}
	
	protected boolean startDrag(PositionInfo startPos) {
		if (startPos != null) {
			doingDrag = true;
			
			int newAxisPos = ((HorizDragPosInfo)startPos).x;
//			double newVal;
			try {
				selectedVal = axis.positionToNumVal(newAxisPos);
			} catch (AxisException e) {
				selectedVal = (e.axisProblem == AxisException.TOO_LOW_ERROR) ? axis.minOnAxis
																						: axis.maxOnAxis;
			}
			
			repaint();
		}
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		startDrag(toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		doingDrag = false;
		repaint();
	}

//-----------------------------------------------------------------------------------

	public int minDisplayWidth() {
		return kMinDensityHt;
	}
}
	
