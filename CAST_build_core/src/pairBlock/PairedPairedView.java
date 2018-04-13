package pairBlock;

import java.awt.*;

import dataView.*;
import axis.*;


public class PairedPairedView extends TwoGroupPairedView {
	static final private Color kSelLinkColor = new Color(0xFF9999);
	
	public PairedPairedView(DataSet theData, XApplet applet,
												String preKey, String postKey, String groupKey, NumCatAxis theAxis,
												NumCatAxis groupAxis, double initialJittering) {
		super(theData, applet, preKey, postKey, groupKey, theAxis, groupAxis, initialJittering);
		groupVariable = (CatVariable)theData.getVariable(groupKey);
	}
	
	protected Color getJoiningColor(int i) {
		CatVariable groupVar = getCatVariable();
		if (groupVar.getItemCategory(i) == 0)
			return kSelLinkColor;
		else
			return super.getJoiningColor(i);
	}
	
	protected double getMean(int i) {
		NumVariable y1Var = getNumVariable();
		NumVariable y2Var = (NumVariable)getVariable(postKey);
		CatVariable groupVar = getCatVariable();
		int n = 0;
		double sy = 0.0;
		
		ValueEnumeration y1e = y1Var.values();
		ValueEnumeration y2e = y2Var.values();
		ValueEnumeration ge = groupVar.values();
		while (y1e.hasMoreValues() && y2e.hasMoreValues() && ge.hasMoreValues()) {
			double y1 = y1e.nextDouble();
			double y2 = y2e.nextDouble();
			int g = groupVar.labelIndex(ge.nextValue());
			if (g == 0) {
				n ++;
				sy += (i == 0) ? y1 : y2;
			}
		}
		return sy / n;
	}
	
	protected void fiddleColor(Graphics g, int index) {
		int group = groupVariable.getItemCategory(index);
		inSample = (group == 0);
	}
	
	protected void doHilite(Graphics g, int index, Point thePoint) {
		int group = groupVariable.getItemCategory(index);
		if (group != 0)
			return;
			
		if (thePoint != null)
			drawCrossBackground(g, thePoint);
		
		NumVariable postVariable = (NumVariable)getVariable(postKey);
		int oldGroupCentre = groupCentre;
		groupCentre = groupAxis.catValToPosition(1);
		Point p2 = getScreenPoint(index, (NumValue)postVariable.valueAt(index), null);
		groupCentre = oldGroupCentre;
		if (p2 != null)
			drawCrossBackground(g, p2);
		
		if (thePoint != null && p2 != null)
			g.drawLine(thePoint.x, thePoint.y, p2.x, p2.y);
	}
	
	protected PositionInfo getPosition(int x, int y) {
		IndexPosInfo indexPos = (IndexPosInfo)super.getPosition(x, y);
		if (indexPos == null)
			return null;
		
		CatVariable groupVar = getCatVariable();
		int index = indexPos.itemIndex;
		if (groupVar.getItemCategory(index) == 0)
			return indexPos;
		else
			return null;
	}
}
	
