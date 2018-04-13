package histo;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;



public class VariableClassHistoView extends HistoView {
//	static public final String VAR_CLASS_HISTO = "varClassHisto";
	
	protected double coreClassStart[];
	private int coreClassCount[];
	protected int groupID[];
	private String initialGrouping;
	private HighlightLimits hilite;
	private boolean showDotPlot = false;
	
	public VariableClassHistoView(DataSet theData, XApplet applet, NumCatAxis valAxis,
											HistoDensityInfo densityAxis, double coreClass0Start, double coreClassWidth,
											HighlightLimits hilite) {
		super(theData, applet, valAxis, densityAxis, coreClass0Start, coreClassWidth);
		this.hilite = hilite;
	}
	
	public void setShowDotPlot(boolean showDotPlot) {
		this.showDotPlot = showDotPlot;
	}
	
	protected int noOfCoreClasses() {
		if (coreClassCount == null)
			return 0;
		else
			return coreClassCount.length;
	}
	
	public void setGrouping(String initialGrouping) {
		this.initialGrouping = initialGrouping;
		groupID = null;
		classStart = null;
		classCount = null;
		checkedMaxDensity = false;
		repaint();
	}
	
	protected int[] initialGrouping() {
		int noOfSourceGroups = noOfCoreClasses();
		int localGroupID[] = new int[noOfSourceGroups];
		StringTokenizer theCounts = new StringTokenizer(initialGrouping);
		int sourceGroup = 0;
		int destGroup = 0;
		while (theCounts.hasMoreTokens())
			try {
				int count = Integer.parseInt(theCounts.nextToken());
				if (count <= 0)
					continue;
				if (sourceGroup + count >= noOfSourceGroups)
					count = noOfSourceGroups - sourceGroup;
				for (int i=0 ; i<count ; i++) {
					localGroupID[sourceGroup] = destGroup;
					sourceGroup++;
				}
				destGroup++;
			} catch (NumberFormatException e) {
				System.err.println("Bad group count");
			}
		if (sourceGroup < noOfSourceGroups) {
			for (int i=0 ; i<noOfSourceGroups - sourceGroup ; i++) {
				localGroupID[sourceGroup] = destGroup;
				sourceGroup++;
			}
			destGroup++;
		}
		
		initialGrouping = null;
		return localGroupID;
	}
	
	private void doClassGrouping() {
		int noOfSourceGroups = noOfCoreClasses();
		int noOfDestGroups = groupID[groupID.length - 1] + 1;
		classStart = new double[noOfDestGroups + 1];
		classCount = new int[noOfDestGroups];
		int destGroup = -1;
		for (int sourceGroup=0 ; sourceGroup<noOfSourceGroups ; sourceGroup++) {
			if (destGroup < groupID[sourceGroup]) {
				destGroup++;
				classStart[destGroup] = coreClassStart[sourceGroup];
			}
			classCount[destGroup] += coreClassCount[sourceGroup];
		}
		classStart[classStart.length - 1] = coreClassStart[coreClassStart.length - 1];
	}
	
	protected void initialise() {
		if (coreClassStart == null)
			coreClassStart = initialiseClasses();
		if (coreClassCount == null)
			coreClassCount = countClasses(coreClassStart);
		if (groupID == null)
			groupID = initialGrouping();
		if (classStart == null || classCount == null)
			doClassGrouping();
		
		if (!checkedMaxDensity)
			checkMaxDensity();
	}
	
	protected Color getHistoColor(int classIndex) {
		if (hilite != null && hilite.classHilited(classStart[classIndex], classStart[classIndex + 1]))
			return Color.yellow;
		else
			return Color.lightGray;
	}
	
	protected Color getHiliteColor() {
		return new Color(0xCC3366);
	}
	
	public void paintView(Graphics g) {
		super.paintView(g);
		
		if (showDotPlot) {
			int lowStackPos = axis.numValToRawPosition(coreClassStart[0]);
			int minStackWidth = 999;
			for (int i=0 ; i<coreClassCount.length ; i++) {
				int highStackPos = axis.numValToRawPosition(coreClassStart[i + 1]);
				minStackWidth = Math.min(minStackWidth, highStackPos - lowStackPos);
				lowStackPos = highStackPos;
			}
//			System.out.println("minStackWidth = " + minStackWidth);
			int halfCrossSize = (minStackWidth - 1) / 2;
			Point p = null;
			lowStackPos = axis.numValToRawPosition(coreClassStart[0]);
			for (int i=0 ; i<coreClassCount.length ; i++) {
				int highStackPos = axis.numValToRawPosition(coreClassStart[i + 1]);
				
				int yMiddle = halfCrossSize + 1;
				int xMiddle = (lowStackPos + highStackPos) / 2;
				for (int j=0 ; j<coreClassCount[i] ; j++) {
					p = translateToScreen(xMiddle, yMiddle, p);
					g.drawLine(p.x - halfCrossSize, p.y - halfCrossSize, p.x + halfCrossSize,
																								p.y + halfCrossSize);
					g.drawLine(p.x - halfCrossSize, p.y + halfCrossSize, p.x + halfCrossSize,
																								p.y - halfCrossSize);
					yMiddle += 2 * halfCrossSize + 2;
				}
				
				lowStackPos = highStackPos;
			}
		}
	}

//-----------------------------------------------------------------------------------
	
	protected void doChangeVariable(Graphics g, String key) {
		coreClassStart = null;
		coreClassCount = null;
		groupID = null;
		super.doChangeVariable(g, key);
	}
	
}