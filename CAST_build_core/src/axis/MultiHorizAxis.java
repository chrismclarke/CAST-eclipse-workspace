package axis;

import java.awt.*;
import java.util.*;

import dataView.*;


public class MultiHorizAxis extends HorizAxis {
	private AxisLabel alternateLabels[][];
	private double originalMin[];
	private double originalMax[];
	private int alternatesUsed = 0;
	private int currentAlternate = -1;
	private int startAlternate = 0;
	
	private boolean changeMinMax = false;
	
	public MultiHorizAxis(XApplet applet, int noOfAlternateLabels) {
		super(applet);
		setNoOfAlternates(noOfAlternateLabels);
		canStagger = false;
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
		StringTokenizer st = new StringTokenizer(labelInfo);
		int nExtraLabels = st.countTokens() - 4;
		
		String minString = st.nextToken();
		double tempMin = Double.parseDouble(minString);
			
		String maxString = st.nextToken();
		double tempMax = Double.parseDouble(maxString);
		
		String labelString = st.nextToken();
		NumValue labelMin = new NumValue(labelString);
				
		String stepString = st.nextToken();
		NumValue labelStep = new NumValue(stepString);
		
		int nCoreLabels = 0;
		double labelVal = labelMin.toDouble();
		double step = labelStep.toDouble();
		while (labelVal <= tempMax) {
			nCoreLabels++;
			labelVal += step;
		}
		AxisLabel[] altLabels = alternateLabels[alternatesUsed] = new AxisLabel[nCoreLabels + nExtraLabels];
			
		double axisRange = tempMax - tempMin;
		int decimals = Math.max(labelMin.decimals, labelStep.decimals);
		labelVal = labelMin.toDouble();
		for (int i=0 ; i<nCoreLabels ; i++) {
			AxisLabel theLabel = new AxisLabel(new NumValue(labelVal, decimals),
																				(labelVal - tempMin) / axisRange);
			labels.addElement(theLabel);		//		initially add to axis so that correct width is found
			altLabels[i] = theLabel;
			labelVal += step;
		}
		if (nExtraLabels > 0) {
			for (int i=0 ; i<nExtraLabels ; i++) {
				NumValue label = new NumValue(st.nextToken());
				AxisLabel theLabel = new AxisLabel(label, (label.toDouble() - tempMin) / axisRange);
				labels.addElement(theLabel);
				altLabels[nCoreLabels + i] = theLabel;
			}
			for (int i=nCoreLabels + nExtraLabels - 1 ; i>0 ; i--)		//	sort labels
				for (int j=0 ; j<i ; j++)
					if (((NumValue)altLabels[i].label).toDouble() < ((NumValue)altLabels[j].label).toDouble()) {
						AxisLabel temp = altLabels[i];
						altLabels[i] = altLabels[j];
						altLabels[j] = temp;
					}
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
		if (changeMinMax) {
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
			noOfCats = labelArray.length;
			for (int i=0 ; i<noOfCats ; i++)
				labels.addElement(labelArray[i]);
			
			if (changeMinMax) {
				minOnAxis = originalMin[index];
				maxOnAxis = originalMax[index];
				minPower = minOnAxis;				//		assumes power is 1.0
				maxPower = maxOnAxis;
				powerRange = maxOnAxis - minOnAxis;
			}
			return true;
		}
	}
	
	public int getAlternativeLabelIndex() {
		return (currentAlternate < 0) ? startAlternate : currentAlternate;
	}
	
	public boolean labelsArranged() {		//		if labelsArranged(), it is possible to call setAlternateLabels()
		return (currentAlternate >= 0);
	}
	
	public void corePaint(Graphics g) {
		if (currentAlternate < 0)
			setAlternateLabels(startAlternate);
		super.corePaint(g);
	}
}