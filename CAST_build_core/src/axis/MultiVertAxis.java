package axis;

import java.awt.*;
import java.util.*;

import dataView.*;


public class MultiVertAxis extends VertAxis {
	private AxisLabel alternateLabels[][];
	private double originalMin[];
	private double originalMax[];
	private int alternatesUsed = 0;
	private int currentAlternate = -1;
	private int startAlternate = 0;
	
	private boolean changeMinMax = false;
	
	public MultiVertAxis(XApplet applet, int noOfAlternateLabels) {
		super(applet);
		alternateLabels = new AxisLabel[noOfAlternateLabels][];
		originalMin = new double[noOfAlternateLabels];
		originalMax = new double[noOfAlternateLabels];
	}
	
	public int getNoOfAlternates() {
		return alternateLabels.length;
	}
	
	public void setNoOfAlternates(int noOfAlternateLabels) {
		alternateLabels = new AxisLabel[noOfAlternateLabels][];
		originalMin = new double[noOfAlternateLabels];
		originalMax = new double[noOfAlternateLabels];
		resetLabels();
	}
	
	public Vector getLabels() {		//	must not be called until axis is layed out
		if (currentAlternate < 0)
			setAlternateLabels(startAlternate);
		return super.getLabels();
	}
	
	public AxisLabel[] getLabels(int index) {
		return alternateLabels[index];
	}
	
	public void resetLabels() {
		currentAlternate = -1;
		alternatesUsed = 0;
	}
	
	public void setChangeMinMax(boolean changeMinMax) {
		this.changeMinMax = changeMinMax;
	}
	
	private void saveCurrentLabels() {
		alternateLabels[alternatesUsed] = new AxisLabel[labels.size()];
		for (int i=0 ; i<labels.size() ; i++)
			alternateLabels[alternatesUsed][i] = (AxisLabel)labels.elementAt(i);
		originalMin[alternatesUsed] = minOnAxis;
		originalMax[alternatesUsed] = maxOnAxis;
		alternatesUsed++;
	}
	
	public void readNumLabels(String labelInfo) {
		super.readNumLabels(labelInfo);
		alternatesUsed = 0;
		saveCurrentLabels();
	}
	
	public void setExtraLabels(AxisLabel[] extraLabels) {
		alternateLabels[alternatesUsed] = extraLabels;
		for (int i=0 ; i<extraLabels.length ; i++)
			labels.addElement(extraLabels[i]);
		
		alternatesUsed++;
	}
	
	public void readExtraNumLabels(String labelInfo) {
		StringTokenizer theLabels = new StringTokenizer(labelInfo);
		
		String minString = theLabels.nextToken();
		double tempMin = Double.parseDouble(minString);
			
		String maxString = theLabels.nextToken();
		double tempMax = Double.parseDouble(maxString);
		
		String labelString = theLabels.nextToken();
		NumValue labelMin = new NumValue(labelString);
				
		String stepString = theLabels.nextToken();
		NumValue labelStep = new NumValue(stepString);
		
		int noOfLabels = 0;
		double labelVal = labelMin.toDouble();
		double step = labelStep.toDouble();
		while (labelVal <= tempMax) {
			noOfLabels++;
			labelVal += step;
		}
		alternateLabels[alternatesUsed] = new AxisLabel[noOfLabels];
			
		double axisRange = tempMax - tempMin;
		int decimals = Math.max(labelMin.decimals, labelStep.decimals);
		labelVal = labelMin.toDouble();
		for (int i=0 ; i<noOfLabels ; i++) {
			AxisLabel theLabel = new AxisLabel(new NumValue(labelVal, decimals),
																				(labelVal - tempMin) / axisRange);
			labels.addElement(theLabel);
			alternateLabels[alternatesUsed][i] = theLabel;
			labelVal += step;
		}
		
		originalMin[alternatesUsed] = tempMin;
		originalMax[alternatesUsed] = tempMax;
		
		alternatesUsed++;
	}
	
	public void setCatLabels(CatVariable variable) {
		super.setCatLabels(variable);
		alternatesUsed = 0;
		saveCurrentLabels();
	}
	
	public void readExtraCatLabels(CatVariable variable) {
		int noOfCats = variable.noOfCategories();
		alternateLabels[alternatesUsed] = new AxisLabel[noOfCats];
		
		for (int index=0 ; index<noOfCats ; index++) {
			Value catLabel = variable.getLabel(index);
			AxisLabel theLabel = new AxisLabel(catLabel, (index + 0.5) / noOfCats);
			labels.addElement(theLabel);		//	initially add to axis so that correct width is found
			alternateLabels[alternatesUsed][index] = theLabel;
		}
		
		alternatesUsed++;
	}
	
	public void setStartAlternate(int startAlternate) {
		this.startAlternate = startAlternate;
		if (changeMinMax) {			//	change min and max here in case they are used
														//	by DataView before axis is first displayed
			minOnAxis = originalMin[startAlternate];
			maxOnAxis = originalMax[startAlternate];
			minPower = minOnAxis;				//		assumes power is 1.0
			maxPower = maxOnAxis;
			powerRange = maxOnAxis - minOnAxis;
		}
	}
	
	public boolean setAlternateLabels(int index) {
		if (index == currentAlternate)
			return false;
		else {
			currentAlternate = index;
			labels.removeAllElements();
			AxisLabel labelArray[] = alternateLabels[index];
			for (int i=0 ; i<labelArray.length ; i++)
				labels.addElement(labelArray[i]);
			
			if (changeMinMax) {
				minOnAxis = originalMin[index];
				maxOnAxis = originalMax[index];
				minPower = minOnAxis;				//		assumes power is 1.0
				maxPower = maxOnAxis;
				powerRange = maxOnAxis - minOnAxis;
			}
			repaint();
			return true;
		}
	}
	
	public AxisLabel getAxisLabel(int alternate, int index) {
		return alternateLabels[alternate][index];
	}
	
	public void corePaint(Graphics g) {
		if (currentAlternate < 0)
			setAlternateLabels(startAlternate);
		super.corePaint(g);
	}
}