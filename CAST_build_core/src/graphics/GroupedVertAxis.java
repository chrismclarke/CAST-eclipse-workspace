package graphics;

import dataView.*;
import axis.*;


public class GroupedVertAxis extends MultiVertAxis {
	
	private int noOfGroups;
	
	public GroupedVertAxis(XApplet applet) {
		super(applet, 2);
	}
	
	public void setupGroupedAxis(int noOfGroups, NumValue startLabel, NumValue step, double maxLabel) {
		this.noOfGroups = noOfGroups;
		int nLabelsInGroup = 0;
		double label = startLabel.toDouble();
		while (label <= maxLabel) {
			nLabelsInGroup ++;
			label += step.toDouble();
		}
		
		AxisLabel newLabels[] = new AxisLabel[nLabelsInGroup * noOfGroups];
		for (int i=0 ; i<noOfGroups ; i++) {
			double lowP = i / (double)noOfGroups;
			double highP = (i+1) / (double)noOfGroups;
			label = startLabel.toDouble();
			for (int j=0 ; j<nLabelsInGroup ; j++) {
				double overallPos = (label - minOnAxis) / (maxOnAxis - minOnAxis);
				double posInGroup = lowP + overallPos * (highP - lowP);
				newLabels[i * nLabelsInGroup + j] = new AxisLabel(new NumValue(label, step.decimals), posInGroup);
				label += step.toDouble();
			}
		}
		setExtraLabels(newLabels);
	}
	
	public int groupAxisPosition(int group) {
		return (axisLength - 1) * group / noOfGroups;
	}
	
	public int numValToGroupPosition(double theValue, int group) {
//		int axisPos = groupAxisPosition(group);
		int groupAxisLength = (axisLength - 1) / noOfGroups;
		
		double realPosition = groupAxisLength * (theValue - minOnAxis) / (maxOnAxis - minOnAxis);
		return groupAxisPosition(group) + (int)Math.round(realPosition);
	}

}