package exper2;

import java.awt.*;

import dataView.*;
import axis.*;


public class NestedFactorAxis extends HorizAxis {
	
	static final private Color kSeparatorColor = new Color(0x999999);
	
	private int nBlocks;
	private CatVariableInterface factorVar;
	private Color factorVarColor;
	
	public NestedFactorAxis(XApplet applet) {
		super(applet);
		canStagger = false;
	}
	
	public void setCatLabels(CatVariableInterface blockVar, CatVariableInterface factorVar, Color factorVarColor) {
		this.factorVar = factorVar;
		this.factorVarColor = factorVarColor;
		labels.removeAllElements();
		
		nBlocks = blockVar.noOfCategories();
		int nLevels = factorVar.noOfCategories();
		int blocksPerLevel = nBlocks / nLevels;
		
		for (int i=0 ; i<nBlocks ; i++) {
			int factorIndex = i / blocksPerLevel;
			Value catLabel = blockVar.getLabel(i);
			AxisLabel nextAxisLabel = new AxisLabel(catLabel, (i + factorIndex + 1) / (double)(nBlocks + nLevels));
			labels.addElement(nextAxisLabel);
		}
		resetLabelSizes();
		repaint();
	}
	
	public int catValToPosition(int blockIndex) {
		int nLevels = factorVar.noOfCategories();
		int blocksPerLevel = nBlocks / nLevels;
		int factorIndex = blockIndex / blocksPerLevel;
		return (axisLength - 1) * (blockIndex + factorIndex + 1) / (nBlocks + nLevels);
	}
	
	public void findAxisWidth() {
		LabelValue tempName = axisName;
		axisName = null;
		super.findAxisWidth();
		axisName = tempName;
		
		axisWidth += (ascent + descent);
//		axisWidth = axisWidth * 2;
	}
	
	public int getPositionBefore(int blockIndex) {
		if (blockIndex == nBlocks)
			return axisLength - 1;
		else
			return (catValToPosition(blockIndex) + catValToPosition(blockIndex - 1)) / 2;
	}
	
	public void corePaint(Graphics g) {
		LabelValue tempName = axisName;
		axisName = null;
		super.corePaint(g);
		axisName = tempName;		//		ignore name
		
		int nLevels = factorVar.noOfCategories();
		int blocksPerLevel = nBlocks / nLevels;
		int baseline = getSize().height - descent;
		int startPos = 0;
		
		for (int i=0 ; i<nLevels ; i++) {
			int endPos = (i == nLevels - 1) ? (axisLength - 1) : getPositionBefore((i + 1) * blocksPerLevel);
			int midPos = (startPos + endPos) / 2;
			if (i > 0) {
				g.setColor(kSeparatorColor);
				g.drawLine(startPos, 1, startPos, getSize().height);
			}
			g.setColor((factorVarColor != null) ? factorVarColor : getForeground());
			Value catLabel = factorVar.getLabel(i);
			catLabel.drawCentred(g, midPos, baseline);
			
			startPos = endPos;
		}
	}
	
/*
	public void corePaint(Graphics g) {
		LabelValue tempName = axisName;
		axisName = null;
		super.corePaint(g);
		axisName = tempName;		//		ignore name
		
		int nLevels = factorVar.noOfCategories();
		int baseline = getSize().height - descent;
		for (int i=0 ; i<nLevels ; i++) {
			int startPos = (axisLength - 1) * i / nLevels;
			int endPos = (axisLength - 1) * (i + 1) / nLevels;
			int midPos = (startPos + endPos) / 2;
			if (i > 0) {
				g.setColor(kSeparatorColor);
				g.drawLine(startPos, 1, startPos, getSize().height);
			}
			g.setColor((factorVarColor != null) ? factorVarColor : getForeground());
			Value catLabel = factorVar.getLabel(i);
			catLabel.drawCentred(g, midPos, baseline);
		}
	}
*/
}