package sampling;

import java.awt.*;

import dataView.*;
import axis.*;
import histo.ClassPosInfo;


public class HistoDragView extends DataView {
	static final public boolean SHOW_VALUES = true;
	static final public boolean NO_SHOW_VALUES = false;
	
	static final private Color kSelectedOddColor = new Color(0x0000FF);
	static final private Color kSelectedEvenColor = new Color(0x3366FF);
	static final private Color kStandardOddColor = new Color(0x999999);
	static final private Color kStandardEvenColor = new Color(0xCCCCCC);
	
	static final private int kDragSlop = 150;
	
	protected HorizAxis horizAxis;
	protected VertAxis probAxis;
	private String yKey;
	private double class0Start, classWidth;
	private int classCount[];
	private boolean classSelected[];
	
	private boolean showValues;
	private int startDragClass = 0;
	
	public HistoDragView(DataSet theData, XApplet applet,
					HorizAxis horizAxis, VertAxis probAxis, String yKey, double class0Start,
					double classWidth, boolean showValues) {
		super(theData, applet, new Insets(0, 0, 0, 0));
		this.horizAxis = horizAxis;
		this.probAxis = probAxis;
		this.yKey = yKey;
		if (yKey != null) {
			this.class0Start = class0Start;
			this.classWidth = classWidth;
			int noOfClasses = (int)Math.round((horizAxis.maxOnAxis - class0Start + classWidth / 1000.0) / classWidth - 0.5);
			classCount = new int[noOfClasses];
			classSelected = new boolean[noOfClasses];
			this.showValues = showValues;
		}
	}
	
	public void paintView(Graphics g) {
		if (yKey == null)
			return;
		
		NumVariable y = (NumVariable)getVariable(yKey);
		double densityFactor = 1.0 / y.noOfValues() / classWidth;
		
		for (int i=0 ; i<classCount.length ; i++) {
			classSelected[i] = true;
			classCount[i] = 0;
		}
		
		ValueEnumeration e = y.values();
		FlagEnumeration fe = getSelection().getEnumeration();
		while (e.hasMoreValues()) {
			double nextVal = e.nextDouble();
			boolean nextSel = fe.nextFlag();
			int index = (int) Math.round(Math.floor((nextVal - class0Start) / classWidth));
			if (index >= 0 && index < classCount.length) {
				classCount[index] ++;
				if (!nextSel)
					classSelected[index] = false;
			}
		}
		
		int startPos = 0;
		try {
			startPos = horizAxis.numValToPosition(class0Start);
		} catch (AxisException ex) {
		}
		
		int maxCount = 0;
		for (int i=0 ; i<classCount.length ; i++)
			if (classCount[i] > maxCount)
				maxCount = classCount[i];
		
		int heightForCount[] = new int[maxCount + 1];
		for (int i=0 ; i<heightForCount.length ; i++)
			heightForCount[i] = probAxis.numValToRawPosition(i * densityFactor);
		
		Point topLeft = null;
		Point bottomRight = null;
		double classEnd = class0Start + classWidth;
		for (int i=0 ; i<classCount.length ; i++)
			try {
				int endPos = horizAxis.numValToPosition(classEnd);
				topLeft = translateToScreen(startPos, heightForCount[classCount[i]], topLeft);
				bottomRight = translateToScreen(endPos, 0, bottomRight);
				g.setColor(classSelected[i] ? kSelectedOddColor : kStandardOddColor);
				g.fillRect(topLeft.x, topLeft.y + 1, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y);
				
				if (showValues) {
					g.setColor(classSelected[i] ? kSelectedEvenColor : kStandardEvenColor);
					for (int j=(i&1) ; j<classCount[i] ; j+=2) {
						topLeft = translateToScreen(startPos, heightForCount[j+1], topLeft);
						bottomRight = translateToScreen(endPos, heightForCount[j], bottomRight);
						g.fillRect(topLeft.x, topLeft.y + 1, bottomRight.x - topLeft.x, bottomRight.y - topLeft.y);
					}
				}
				startPos = endPos;
				classEnd += classWidth;
			} catch (AxisException ex) {
			}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return true;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		if (x < -kDragSlop || y < -kDragSlop || x >= getSize().width + kDragSlop || y >= getSize().height + kDragSlop)
			return null;
		
		Point hitPos = translateFromScreen(x, y, null);
		double hitVal = 0.0;
		try {
			hitVal = horizAxis.positionToNumVal(hitPos.x);
		} catch (AxisException e) {
			return null;
		}
		
		if (hitVal <= class0Start)
			return null;
		
		int index = (int) Math.round(Math.floor((hitVal - class0Start) / classWidth));
		return new ClassPosInfo(index);
	}
	
	protected PositionInfo getInitialPosition(int x, int y) {
		return getPosition(x, y);
	}
	
	protected void invertAtPosition(PositionInfo posInfo) {
//		NumVariable variable = getNumVariable();
//		boolean selectedValues[] = new boolean[variable.noOfValues()];
		
		int hitClass = ((ClassPosInfo)posInfo).classIndex;
		int minClass = Math.min(startDragClass, hitClass);
		int maxClass = Math.max(startDragClass, hitClass);
		
		getData().setSelection(yKey, class0Start + minClass * classWidth,
															class0Start + (maxClass + 1) * classWidth);
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo == null)
			getData().clearSelection();
		else {
			startDragClass = ((ClassPosInfo)startInfo).classIndex;
			invertAtPosition(startInfo);
		}
		return true;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos != null)
			invertAtPosition(toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
																		//		leave classes highlighted
	}
}
	
