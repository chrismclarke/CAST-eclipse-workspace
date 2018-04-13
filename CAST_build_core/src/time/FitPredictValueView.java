package time;

import java.awt.*;

import dataView.*;
import valueList.*;


public class FitPredictValueView extends OneValueView {
	static final private int NO_SELECTION = 0;
	static final private int PREDICTION = 1;
	static final private int FITTED = 2;
	
	private String actualKey;
	private String fittedValString, predictionString;
	private int selectionType = NO_SELECTION;
	private int fitLabelWidth, predictLabelWidth, maxLabelWidth;
	
	public FitPredictValueView(DataSet theData, String fitKey,
					String actualKey, XApplet applet, Value maxValue,
					String fittedValString, String predictionString) {
		super(theData, fitKey, applet, maxValue);
		this.actualKey = actualKey;
		this.fittedValString = fittedValString;
		this.predictionString = predictionString;
		setLabel("");
	}
	
	public FitPredictValueView(DataSet theData, String fitKey, String actualKey,
							XApplet applet, String fittedValString, String predictionString) {
		this(theData, fitKey, actualKey, applet, null, fittedValString, predictionString);
	}

//--------------------------------------------------------------------------------
	
	protected int getLabelWidth(Graphics g) {
		fitLabelWidth = g.getFontMetrics().stringWidth(fittedValString);
		predictLabelWidth = g.getFontMetrics().stringWidth(predictionString);
		maxLabelWidth = Math.max(fitLabelWidth, predictLabelWidth);
		return maxLabelWidth;
	}
	
	protected void drawLabel(Graphics g, int startHoriz, int baseLine) {
		int offset = maxLabelWidth - ((selectionType == PREDICTION) ? predictLabelWidth
																		: fitLabelWidth);
		super.drawLabel(g, startHoriz + offset, baseLine);
	}
	
	public void redrawValue() {
		super.redrawValue();
		adjustLabel();
	}
	

//--------------------------------------------------------------------------------
	
	private void adjustLabel() {
		int selectionIndex = getSelection().findSingleSetFlag();
		int newSelectionType = NO_SELECTION;
		if (selectionIndex >= 0) {
			NumVariable v = (NumVariable)getVariable(actualKey);
			newSelectionType = Double.isNaN(v.doubleValueAt(selectionIndex)) ? PREDICTION : FITTED;
		}
		if (selectionType != newSelectionType) {
			selectionType = newSelectionType;
			setLabel((selectionType == NO_SELECTION) ? ""
						: (selectionType == PREDICTION) ? predictionString
						: fittedValString);
		}
	}
	
	public void paintChildren(Graphics g) {
		adjustLabel();
		super.paintChildren(g);
	}
}
