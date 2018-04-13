package graphics;

import dataView.*;
import axis.*;


public class GroupedHorizAxis extends MultiHorizAxis {
	static final private double kGroupGapPropn = 0.2;
	
	private int noOfGroups;
	private int correctNoOfCats;			//	noOfCats changes to the no of labels when it displays grouped
	
	public GroupedHorizAxis(XApplet applet) {
		super(applet, 2);
		alwayDrawLabels = true;			//	otherwise some labels are missed when axis is grouped
	}
	
	private double groupAxisPropn() {
		return 1.0 / ((1 + kGroupGapPropn) * noOfGroups - 0.2);
	}
	
	private double axisStart(int group) {
		double axisPropn = groupAxisPropn();
		return group * (1 + kGroupGapPropn) * axisPropn;
	}
	
	private double groupCatPropn(int catIndex, int group, double axisPropn) {
		double axisStart = axisStart(group);
		double propn = axisStart + (catIndex + 0.5) / correctNoOfCats * axisPropn;
		return propn;
	}
	
	public void setupGroupedAxis(int noOfGroups, Value[] shortLabel) {
		this.noOfGroups = noOfGroups;
		correctNoOfCats = noOfCats;
		AxisLabel newLabels[] = new AxisLabel[correctNoOfCats * noOfGroups];
		
		double axisPropn = groupAxisPropn();
		for (int i=0 ; i<noOfGroups ; i++)
			for (int j=0 ; j<correctNoOfCats ; j++) {
				double pos = groupCatPropn(j, i, axisPropn);
				newLabels[i * correctNoOfCats + j] = new AxisLabel(shortLabel[j], pos);
			}
		setExtraLabels(newLabels);
	}
	
	public int catValToGroupPosition(int catIndex, int group) {
		double axisPropn = groupAxisPropn();
		double propn = groupCatPropn(catIndex, group, axisPropn);
		return (int)Math.round((axisLength - 1) * propn);
	}
	
	public int catValToPosition(int catIndex) {
		return (int)Math.round((axisLength - 1) * (catIndex + 0.5) / correctNoOfCats);
	}

}