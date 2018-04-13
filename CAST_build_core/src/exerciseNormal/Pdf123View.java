package exerciseNormal;

import java.awt.*;

import dataView.*;
import axis.*;
import distn.*;
import coreGraphics.*;


public class Pdf123View extends DataView implements Interval123Constants {
//	static final public String PDF_123_VIEW = "pdf123View";
	
	static final private Color dimColor = new Color(0xAAAAAA);
	static final private Color boldColor = new Color(0x3366FF);
	
	static final private Color kLineColor = new Color(0x999999);
	static final private Color k2SdBackgroundColor = new Color(0xFFCCCC);
	static final private Color k4SdBackgroundColor = new Color(0xFFDDDD);
	static final private Color k6SdBackgroundColor = new Color(0xFFEEEE);
	
	private String distnKey;
	private HorizAxis horizAxis;
	
	private BackgroundNormalArtist normalArtist;
	private int intervalType = NO_INTERVAL;
	
	public Pdf123View(DataSet theData, XApplet applet, HorizAxis horizAxis, String distnKey) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.distnKey = distnKey;
		this.horizAxis = horizAxis;
		
		normalArtist = new BackgroundNormalArtist(distnKey, theData);
		normalArtist.setFillColor(boldColor);				//	normalArtist uses colours in opposite way to expected
		normalArtist.setHighlightColor(dimColor);
//		normalArtist.setDensityScaling(0.95);
	}
	
	public void paintView(Graphics g) {
		NormalDistnVariable normalDistn = (NormalDistnVariable)getVariable(distnKey);
		double mean = normalDistn.getMean().toDouble();
		double sd = normalDistn.getSD().toDouble();
		
		if (intervalType == NO_INTERVAL) {
			fill123Background(g, mean, sd);
			normalArtist.paintDistn(g, this, horizAxis, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
			draw123Foreground(g, mean, sd);
		}
		else {
			double minSelection, maxSelection;
			if (intervalType <= LESS_THAN_3) {
				minSelection = Double.NEGATIVE_INFINITY;
				maxSelection = mean + sd * (intervalType - 3);
				fillBackgroundSelection(g, horizAxis.minOnAxis, maxSelection);
			}
			else if (intervalType <= GREATER_THAN_3) {
				minSelection = mean + sd * (intervalType - GREATER_THAN_MINUS3 - 3);
				maxSelection = Double.POSITIVE_INFINITY;
				fillBackgroundSelection(g, minSelection, horizAxis.maxOnAxis);
			}
			else			//	BETWEEN or OUTSIDE
				if (intervalType >= OUTSIDE_1) {
														//	same selection for BETWEEN and OUTSIDE (but colours different)
					minSelection = mean - sd * (intervalType - BETWEEN_1 -2);
					maxSelection = mean + sd * (intervalType - BETWEEN_1 -2);
					fillBackgroundSelection(g, horizAxis.minOnAxis, minSelection);
					fillBackgroundSelection(g, maxSelection, horizAxis.maxOnAxis);
				}
				else {
									//	same selection for BETWEEN and OUTSIDE (but colours different)
					minSelection = mean - sd * (intervalType - BETWEEN_1 + 1);
					maxSelection = mean + sd * (intervalType - BETWEEN_1 + 1);
					fillBackgroundSelection(g, minSelection, maxSelection);
				}
			
			normalArtist.paintDistn(g, this, horizAxis, minSelection, maxSelection);
			draw123Foreground(g, mean, sd);
		}
	}
	
	private void fill123Background(Graphics g, double mean, double sd) {
		int lowPos = translateToScreen(horizAxis.numValToRawPosition(mean - 3 * sd), 0, null).x;
		int highPos = translateToScreen(horizAxis.numValToRawPosition(mean + 3 * sd), 0, null).x;
		g.setColor(k6SdBackgroundColor);
		g.fillRect(lowPos, 0, highPos - lowPos, getSize().height);
		
		g.setColor(k4SdBackgroundColor);
		lowPos = translateToScreen(horizAxis.numValToRawPosition(mean - 2 * sd), 0, null).x;
		highPos = translateToScreen(horizAxis.numValToRawPosition(mean + 2 * sd), 0, null).x;
		g.fillRect(lowPos, 0, highPos - lowPos, getSize().height);
		
		g.setColor(k2SdBackgroundColor);
		g.drawLine(lowPos, 0, lowPos, getSize().height);
		lowPos = translateToScreen(horizAxis.numValToRawPosition(mean - sd), 0, null).x;
		highPos = translateToScreen(horizAxis.numValToRawPosition(mean + sd), 0, null).x;
		g.fillRect(lowPos, 0, highPos - lowPos, getSize().height);
	}
	
	private void fillBackgroundSelection(Graphics g, double min, double max) {
		g.setColor(k4SdBackgroundColor);
		
		int lowPos = translateToScreen(horizAxis.numValToRawPosition(min), 0, null).x;
		int highPos = translateToScreen(horizAxis.numValToRawPosition(max), 0, null).x;
		g.fillRect(lowPos, 0, highPos - lowPos, getSize().height);
	}
	
	private void draw123Foreground(Graphics g, double mean, double sd) {
		g.setColor(kLineColor);
		
		int lowPos = translateToScreen(horizAxis.numValToRawPosition(mean - 3 * sd), 0, null).x;
		int highPos = translateToScreen(horizAxis.numValToRawPosition(mean + 3 * sd), 0, null).x;
		g.drawLine(lowPos, 0, lowPos, getSize().height);
		g.drawLine(highPos, 0, highPos, getSize().height);
		
		lowPos = translateToScreen(horizAxis.numValToRawPosition(mean - 2 * sd), 0, null).x;
		highPos = translateToScreen(horizAxis.numValToRawPosition(mean + 2 * sd), 0, null).x;
		g.drawLine(lowPos, 0, lowPos, getSize().height);
		g.drawLine(highPos, 0, highPos, getSize().height);
		
		lowPos = translateToScreen(horizAxis.numValToRawPosition(mean - sd), 0, null).x;
		highPos = translateToScreen(horizAxis.numValToRawPosition(mean + sd), 0, null).x;
		g.drawLine(lowPos, 0, lowPos, getSize().height);
		g.drawLine(highPos, 0, highPos, getSize().height);
		
		int meanPos = translateToScreen(horizAxis.numValToRawPosition(mean), 0, null).x;
		g.drawLine(meanPos, 0, meanPos, getSize().height);
	}
	
	public void reset() {
		normalArtist.resetDistn();
		setIntervalType(NO_INTERVAL);
	}
	
	public void setIntervalType(int intervalType) {
		if (this.intervalType <= BETWEEN_3 && intervalType > BETWEEN_3) {
			normalArtist.setFillColor(dimColor);				//	normalArtist uses colours in opposite way to expected
			normalArtist.setHighlightColor(boldColor);
		}
		else if (this.intervalType > BETWEEN_3 && intervalType <= BETWEEN_3) {
			normalArtist.setFillColor(boldColor);				//	normalArtist uses colours in opposite way to expected
			normalArtist.setHighlightColor(dimColor);
		}
		this.intervalType = intervalType;
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return false;
	}
}
	
