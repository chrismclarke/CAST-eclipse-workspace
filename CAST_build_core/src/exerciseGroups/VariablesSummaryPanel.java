package exerciseGroups;

import java.awt.*;

import dataView.*;
import valueList.*;
import exercise2.*;


public class VariablesSummaryPanel extends XPanel {
	static final private Color kGreenColor = new Color(0x006600);
	
	static final private LabelValue	varLabel = new LabelValue("Variable");
	static final private LabelValue	meanLabel = new LabelValue("Mean");
	static final private LabelValue	sdLabel = new LabelValue("St devn");
	
	private GroupSummaryView varNames[], varMeans[], varSds[];
	
	public VariablesSummaryPanel(DataSet data, String[] varKey, ExerciseApplet applet) {
		int nGroups = varKey.length;
		varNames = new GroupSummaryView[nGroups];
		varMeans = new GroupSummaryView[nGroups];
		varSds = new GroupSummaryView[nGroups];
		
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
		FixedValueView nameHeading = new FixedValueView(null, varLabel, varLabel, applet);
		nameHeading.unboxValue();
		add(nameHeading);
		gbl.setConstraints(nameHeading, cons);
		
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
			
		for (int varIndex=0 ; varIndex<nGroups ; varIndex++) {
			cons.gridx = 0;
			varNames[varIndex] = new GroupSummaryView(data, varKey[varIndex],
																										GroupSummaryView.NAME_VALUE, applet);
			varNames[varIndex].unboxValue();
			add(varNames[varIndex]);
			gbl.setConstraints(varNames[varIndex], cons);
			
			cons.gridx ++;
			varMeans[varIndex] = new GroupSummaryView(data, varKey[varIndex],
																										GroupSummaryView.MEAN_VALUE, applet);
			varMeans[varIndex].setForeground(Color.blue);
			add(varMeans[varIndex]);
			gbl.setConstraints(varMeans[varIndex], cons);
			
			cons.gridx ++;
			varSds[varIndex] = new GroupSummaryView(data, varKey[varIndex],
																										GroupSummaryView.SD_VALUE, applet);
			varSds[varIndex].setForeground(kGreenColor);
			add(varSds[varIndex]);
			gbl.setConstraints(varSds[varIndex], cons);
			
			cons.gridy ++;
		}
	}
	
	public void updateForNewData(Value maxGroupName, Value maxMean, Value maxSd) {
		for (int i=0 ; i<varNames.length ; i++) {
			varNames[i].updateGroupInfo(maxGroupName);
			varMeans[i].updateGroupInfo(maxMean);
			varSds[i].updateGroupInfo(maxSd);
		}
	}
	
	public NumValue getMean(int varIndex) {
		return new NumValue(varMeans[varIndex].getValueString());
	}
	
	public NumValue getSd(int varIndex) {
		return new NumValue(varSds[varIndex].getValueString());
	}
	
}