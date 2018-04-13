package linMod;

import java.awt.*;

import dataView.*;
import models.*;

import regn.*;


public class PredictionEqnView extends EquationView {
//	static public final String PREDICTION_EQUATION = "prediction";
	
	static final private int kPlusWidth = 14;
	static final private int kTimesWidth = 14;
	static final private int kEqualsWidth = 14;
	static final protected int kYEqualsGap = 6;
	static final protected int kLeftRightBorder = 3;
	static final protected int kTopExtra = 2;
	static final protected int kBottomExtra = 2;
	
	private String interceptKey, slopeKey, modelKey;
	private NumValue maxIntercept, maxSlope, maxX, maxPrediction;
	
	private boolean estimated;
	
	private XValueSlider xSlider;
	
	private int maxInterceptWidth, maxSlopeWidth, maxXWidth, maxPredictionWidth;
	private Dimension predictionSize;
	private int baseline;
	
	public PredictionEqnView(DataSet theData, XApplet applet,
									String interceptKey, String slopeKey, String modelKey,
									NumValue maxIntercept, NumValue maxSlope, NumValue maxX,
									NumValue maxPrediction, XValueSlider xSlider) {
		super(theData, applet);
		this.interceptKey = interceptKey;
		this.slopeKey = slopeKey;
		this.modelKey = modelKey;
		RegnImages.loadRegn(applet);
		this.maxIntercept = maxIntercept;
		this.maxSlope = maxSlope;
		this.maxX = maxX;
		this.maxPrediction = maxPrediction;
		this.xSlider = xSlider;
		estimated = (modelKey == null);
	}
	
	public void setEstimated(boolean estimated) {
		this.estimated = estimated;
	}

//--------------------------------------------------------------------------------
	
	protected boolean initialise(Graphics g) {
		if (super.initialise(g)) {
			maxInterceptWidth = maxIntercept.stringWidth(g);
			maxSlopeWidth = maxSlope.stringWidth(g);
			maxXWidth = maxX.stringWidth(g);
			maxPredictionWidth = maxPrediction.stringWidth(g);
			
			FontMetrics fm = g.getFontMetrics();
			predictionSize = getValueSize(fm, maxPredictionWidth);
			
			int leftWidth = RegnImages.kMuYParamWidth + kYEqualsGap;
			int equationWidth = maxInterceptWidth + kPlusWidth + maxSlopeWidth
																		+ kTimesWidth + maxXWidth + kEqualsWidth;
			modelWidth = 2 * kLeftRightBorder + leftWidth + equationWidth + predictionSize.width;
			
			modelHeight = predictionSize.height + kTopExtra + kBottomExtra;
			baseline = getValueBaseline(g.getFontMetrics()) + kTopExtra;
			return true;
		}
		else
			return false;
	}
	
	public int paintModel(Graphics g) {
//		FontMetrics fm = g.getFontMetrics();
		int horizPos = kLeftRightBorder;
		
		Image muImage = estimated ? RegnImages.muYHat : RegnImages.muY;
		g.drawImage(muImage, horizPos, baseline - RegnImages.kMuYParamAscent, this);
		horizPos += RegnImages.kMuYParamWidth + kYEqualsGap;
		
		NumValue intercept = null;
		NumValue slope = null;
		
		if (modelKey == null) {
			FlagEnumeration fe = getSelection().getEnumeration();
			
			boolean gotSelection = false;
			int selectedIndex = -1;
			int i = 0;
			while (fe.hasMoreFlags()) {
				boolean nextSel = fe.nextFlag();
				if (nextSel) {
					if (gotSelection) {
						selectedIndex = -1;
						break;
					}
					gotSelection = true;
					selectedIndex = i;
				}
				i++;
			}
			if (selectedIndex >= 0) {
				NumVariable interceptVar = (NumVariable)getVariable(interceptKey);
				intercept = (NumValue)interceptVar.valueAt(selectedIndex);
				NumVariable slopeVar = (NumVariable)getVariable(slopeKey);
				slope = (NumValue)slopeVar.valueAt(selectedIndex);
			}
		}
		else {
			LinearModel model = (LinearModel)getVariable(modelKey);
			intercept = model.getIntercept();
			slope = model.getSlope();
		}
		
		NumValue x = xSlider.getNumValue();
		NumValue prediction = (intercept == null) ? null : new NumValue(intercept.toDouble()
															+ slope.toDouble() * x.toDouble(), maxPrediction.decimals);
		
		horizPos += maxInterceptWidth;
		if (intercept != null)
			intercept.drawLeft(g, horizPos, baseline);
		
		horizPos = drawPlus(g, horizPos, baseline);
		horizPos += maxSlopeWidth;
		if (slope != null)
			slope.drawLeft(g, horizPos, baseline);
		
		horizPos = drawTimes(g, horizPos, baseline);
		
		g.setColor(Color.red);
		horizPos += maxXWidth;
		x.drawLeft(g, horizPos, baseline);
		g.setColor(getForeground());
		
		horizPos = drawEquals(g, horizPos, baseline);
		
		if (prediction != null)
			drawParameter(g, prediction.toString(), maxPredictionWidth, horizPos, baseline);
		horizPos += predictionSize.width;
		
		return horizPos;
	}
	
	private int drawPlus(Graphics g, int horizPos, int baseline) {
		g.drawLine(horizPos + 5, baseline - 4, horizPos + 11, baseline - 4);
		g.drawLine(horizPos + 8, baseline - 7, horizPos + 8, baseline - 1);
		
		return horizPos + kPlusWidth;
	}
	
	private int drawTimes(Graphics g, int horizPos, int baseline) {
		g.drawLine(horizPos + 5, baseline - 7, horizPos + 11, baseline - 1);
		g.drawLine(horizPos + 5, baseline - 1, horizPos + 11, baseline - 7);
		
		return horizPos + kTimesWidth;
	}
	
	private int drawEquals(Graphics g, int horizPos, int baseline) {
		g.drawLine(horizPos + 5, baseline - 6, horizPos + 11, baseline - 6);
		g.drawLine(horizPos + 5, baseline - 3, horizPos + 11, baseline - 3);
		
		return horizPos + kEqualsWidth;
	}
	
}
