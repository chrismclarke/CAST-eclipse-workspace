package exerciseGroups;

import java.awt.*;

import dataView.*;
import valueList.*;
import exercise2.*;


public class GroupsSummaryPanel extends XPanel {
	static final private Color kGreenColor = new Color(0x006600);
	
	static final private NumValue kMaxCount = new NumValue(9999, 0);
	
	static final private LabelValue	groupLabel = new LabelValue("Group");
	static final private LabelValue	countLabel = new LabelValue("Count");
	static final private LabelValue	meanLabel = new LabelValue("Mean");
	static final private LabelValue	sdLabel = new LabelValue("St devn");
	
	private GroupSummaryView groupNames[], groupCounts[], groupMeans[], groupSDs[];
	
	public GroupsSummaryPanel(DataSet data, String yKey, String groupKey, ExerciseApplet applet) {
		CatVariable x = (CatVariable)data.getVariable(groupKey);
		int nGroups = x.noOfCategories();
		groupNames = new GroupSummaryView[nGroups];
		groupCounts = new GroupSummaryView[nGroups];
		groupMeans = new GroupSummaryView[nGroups];
		groupSDs = new GroupSummaryView[nGroups];
		
		GridBagLayout gbl = new GridBagLayout();
		setLayout(gbl);
		
    GridBagConstraints cons = new GridBagConstraints();
		cons.anchor = GridBagConstraints.CENTER;
		cons.fill = GridBagConstraints.NONE;
		cons.gridheight = cons.gridwidth = 1;
		cons.insets = new Insets(3,0,3,0);
		cons.ipadx = cons.ipady = 0;
		cons.weightx = cons.weighty = 1.0;
		cons.gridy = 0;
		
		cons.gridx = 0;
		FixedValueView nameHeading = new FixedValueView(null, groupLabel, groupLabel, applet);
		nameHeading.unboxValue();
		add(nameHeading);
		gbl.setConstraints(nameHeading, cons);
		
		cons.gridx ++;
		FixedValueView countHeading = new FixedValueView(null, countLabel, countLabel, applet);
		countHeading.unboxValue();
		add(countHeading);
		gbl.setConstraints(countHeading, cons);
		
		cons.gridx ++;
		FixedValueView meanHeading = new FixedValueView(null, meanLabel, meanLabel, applet);
		meanHeading.setForeground(Color.blue);
		meanHeading.unboxValue();
		add(meanHeading);
		gbl.setConstraints(meanHeading, cons);
		
		cons.gridx ++;
		FixedValueView sdHeading = new FixedValueView(null, sdLabel, sdLabel, applet);
		sdHeading.setForeground(kGreenColor);
		sdHeading.unboxValue();
		add(sdHeading);
		gbl.setConstraints(sdHeading, cons);
		
		cons.gridy ++;
		cons.ipady = 4;
			
		for (int groupIndex=0 ; groupIndex<nGroups ; groupIndex++) {
			cons.gridx = 0;
			groupNames[groupIndex] = new GroupSummaryView(data, groupKey, yKey, groupIndex,
																										GroupSummaryView.NAME_VALUE, applet);
			groupNames[groupIndex].unboxValue();
			add(groupNames[groupIndex]);
			gbl.setConstraints(groupNames[groupIndex], cons);
			
			cons.gridx ++;
			groupCounts[groupIndex] = new GroupSummaryView(data, groupKey, yKey, groupIndex,
																										GroupSummaryView.COUNT_VALUE, applet);
			add(groupCounts[groupIndex]);
			gbl.setConstraints(groupCounts[groupIndex], cons);
			
			cons.gridx ++;
			groupMeans[groupIndex] = new GroupSummaryView(data, groupKey, yKey, groupIndex,
																										GroupSummaryView.MEAN_VALUE, applet);
			groupMeans[groupIndex].setForeground(Color.blue);
			add(groupMeans[groupIndex]);
			gbl.setConstraints(groupMeans[groupIndex], cons);
			
			cons.gridx ++;
			groupSDs[groupIndex] = new GroupSummaryView(data, groupKey, yKey, groupIndex,
																										GroupSummaryView.SD_VALUE, applet);
			groupSDs[groupIndex].setForeground(kGreenColor);
			add(groupSDs[groupIndex]);
			gbl.setConstraints(groupSDs[groupIndex], cons);
			
			cons.gridy ++;
		}
	}
	
	public void updateForNewData(Value maxGroupName, Value maxMean, Value maxSd) {
		for (int i=0 ; i<groupNames.length ; i++) {
			groupNames[i].updateGroupInfo(maxGroupName);
			groupCounts[i].updateGroupInfo(kMaxCount);
			groupMeans[i].updateGroupInfo(maxMean);
			groupSDs[i].updateGroupInfo(maxSd);
		}
	}
	
	public double getMean(int groupIndex) {
		return Double.parseDouble(groupMeans[groupIndex].getValueString());
	}
	
	public double getSd(int groupIndex) {
		return Double.parseDouble(groupSDs[groupIndex].getValueString());
	}
	
}