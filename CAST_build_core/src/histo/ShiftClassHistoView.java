package histo;

import dataView.*;
import axis.*;



public class ShiftClassHistoView extends VariableClassHistoView {
//	static public final String SHIFT_CLASS_HISTO = "shiftClassHisto";
	
	static public final boolean RIGHT = true;
	static public final boolean LEFT = false;
	static public final boolean WIDER = true;
	static public final boolean NARROWER = false;
	
	private int maxGroupSize;
	private int groupSize;
	
	public ShiftClassHistoView(DataSet theData, XApplet applet, NumCatAxis valAxis,
							DensityAxis densityAxis, double coreClass0Start, double coreClassWidth, int maxGroupSize,
							int startGroupSize) {
		super(theData, applet, valAxis, densityAxis, coreClass0Start, coreClassWidth, null);
		this.maxGroupSize = maxGroupSize;
		groupSize = startGroupSize;
	}
	
	public boolean isMinClassWidth() {
		return groupSize == 1;
	}
	
	public boolean isMaxClassWidth() {
		return groupSize == maxGroupSize;
	}
	
	public double findClass1Width() {
		initialise();
		return (coreClassStart[1] - coreClassStart[0]) * groupSize;
	}
	
	public double findClass0Start() {
		initialise();
		if (groupID[groupSize - 1] == 0)		//		first class is full-size
			return coreClassStart[0];
		else {
			for (int i=0 ; i<groupSize ; i++)	//		return start of 2nd class
				if (groupID[i] == 1)
					return coreClassStart[i];
		}
		return 0;	//		should never be reached
	}
	
	protected int[] initialGrouping() {
		int noOfSourceGroups = noOfCoreClasses();
		int localGroupID[] = new int[noOfSourceGroups];
		for (int i=0 ; i<noOfSourceGroups ; i++)
			localGroupID[i] = i / groupSize;
		return localGroupID;
	}
	
	public void changeWidth(boolean widerNotNarrower) {
		if (groupID == null)
			return;
		if (widerNotNarrower)
			if (groupSize == maxGroupSize)
				return;
			else
				groupSize++;
		else
			if (groupSize == 1)
				return;
			else
				groupSize--;
		
		for (int i=0 ; i<noOfCoreClasses() ; i++)
			groupID[i] = i / groupSize;
		
		classStart = null;
		classCount = null;
		checkedMaxDensity = false;
		repaint();
	}
	
	public void shiftGroups(boolean rightNotLeft) {
		if (groupID == null)
			return;
		if (rightNotLeft) {
			int classShift = (groupID[groupSize - 1] == 0) ? 1 : 0;
			for (int i=groupID.length - 1 ; i>0 ; i--)
				groupID[i] = groupID[i-1] + classShift;
			groupID[0] = 0;
		}
		else {
			int classShift = (groupID[1] == 1) ? 1 : 0;
			for (int i=0 ; i<groupID.length-1 ; i++)
				groupID[i] = groupID[i+1] - classShift;
			groupID[groupID.length - 1] = groupID[groupID.length - 2];
			if (groupID[groupID.length - groupSize - 1] == groupID[groupID.length - 2])
				groupID[groupID.length - 1] ++;
		}
		
		classStart = null;
		classCount = null;
		checkedMaxDensity = false;
		repaint();
	}
}