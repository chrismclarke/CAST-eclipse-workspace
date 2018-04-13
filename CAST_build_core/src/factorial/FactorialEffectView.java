package factorial;

import java.awt.*;

import dataView.*;
import axis.*;

import twoFactor.*;


public class FactorialEffectView extends CoreEffectDiagramView {
//	static final public String FACTORIAL_EFFECT = "factorialEffect";
	
	static final private int kKeySpacing = 2;
	static final private int kKeyLabelOffset = 10;
	
	static final private Color[] kLineColors = {Color.blue, Color.red, new Color(0x009900)};
	
	public FactorialEffectView(DataSet theData, XApplet applet, String yKey, String[] xKey, String modelKey,
									HorizAxis horizAxis, VertAxis yAxis, int horizIndex) {
		super(theData, applet, yKey, xKey, modelKey, horizAxis, yAxis, horizIndex);
	}
	
	public void paintView(Graphics g) {
		MultiFactorModel model = (MultiFactorModel)getVariable(modelKey);
		
		int[][] heirTerms = model.getCurrentModel();
		boolean[] hasInteraction = new boolean[xKey.length];		//	all false
		for (int i=0 ; i<heirTerms.length ; i++) {
			boolean relevantTerm = false;
			for (int j=0 ; j<heirTerms[i].length ; j++)
				if (heirTerms[i][j] == horizIndex) {
					relevantTerm = true;
					break;
				}
			if (relevantTerm)
				for (int j=0 ; j<heirTerms[i].length ; j++)
					hasInteraction[heirTerms[i][j]] = true;
		}
		
		CatVariable horizVar = (CatVariable)getVariable(xKey[horizIndex]);
		int nHoriz = horizVar.noOfCategories();
		double mean[] = new double[nHoriz];
		int cellIndex[] = new int[xKey.length];
		for (int i=0 ; i<cellIndex.length ; i++)
			cellIndex[i] = -1;
		
		if (!hasInteraction[horizIndex]) {		//	No terms involve horiz variable
			double overallMean = model.evaluateMean(cellIndex);
			for (int i=0 ; i<nHoriz ; i++)
				mean[i] = overallMean;
			
			drawMeans(g, mean, Color.gray);
		}
		else {
			int keyIndex = recursiveDrawMeans(g, model, hasInteraction, cellIndex, mean, Color.gray, "", 0);
			if (keyIndex >= 0)
				drawKey(g, keyIndex);
		}
	}
	
	private void drawKey(Graphics g, int keyIndex) {
		CatVariable keyVar = (CatVariable)getVariable(xKey[keyIndex]);
		int nKeyCats = keyVar.noOfCategories();
		
		FontMetrics fm = g.getFontMetrics();
		int ascent = fm.getAscent();
		int descent = fm.getDescent();
		int baseline = ascent + kKeySpacing;
		g.setColor(Color.gray);
		g.drawString(keyVar.name, 2, baseline);
		for (int i=nKeyCats-1 ; i>=0 ; i--) {
			baseline += ascent + descent + kKeySpacing;
			g.setColor(kLineColors[Math.min(i, kLineColors.length - 1)]);
			keyVar.getLabel(i).drawRight(g, kKeyLabelOffset, baseline);
		}
	}
	
	private int recursiveDrawMeans(Graphics g, MultiFactorModel model,
													boolean[] hasInteraction, int[] cellIndex, double[] mean,
													Color lineColor, String lineLabel, int fromIndex) {
													//	returns index of variable for key
		if (fromIndex == hasInteraction.length) {
			for (int i=0 ; i<mean.length ; i++) {
				cellIndex[horizIndex] = i;
				mean[i] = model.evaluateMean(cellIndex);
			}
			drawMeans(g, mean, lineColor);
			if (lineLabel != null)
				drawLineLabel(g, mean[mean.length - 1], lineColor, lineLabel);
			return -1;
		}
		else if (fromIndex == horizIndex || !hasInteraction[fromIndex])
			return recursiveDrawMeans(g, model, hasInteraction, cellIndex, mean, lineColor,
																														lineLabel, fromIndex + 1);
		else {
			boolean lastInteraction = true;
			for (int i=fromIndex+1 ; i<hasInteraction.length ; i++)
				if (i != horizIndex && hasInteraction[i])
					lastInteraction = false;
			CatVariable interactVar = (CatVariable)getVariable(xKey[fromIndex]);
			int nInteract = interactVar.noOfCategories();
			int lowerInteractIndex = -1;
			for (int i=0 ; i<nInteract ; i++) {
				cellIndex[fromIndex] = i;
				String label;
				if (lastInteraction)
					label = lineLabel;
				else {
					label = interactVar.getLabel(i).toString();
					if (lineLabel.length() > 0)
						label = lineLabel + " & " + label;
				}
				Color c = kLineColors[Math.min(i, kLineColors.length - 1)];
				lowerInteractIndex = recursiveDrawMeans(g, model, hasInteraction, cellIndex,
																													mean, c, label, fromIndex + 1);
			}
			
			return lastInteraction ? fromIndex : lowerInteractIndex;
		}
	}
}