package timeProg;

import java.awt.*;

import dataView.*;
import utils.*;
import coreVariables.*;


public class SeasonalAdjustApplet extends SeasonalEffectApplet {
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		data.addVariable("adjusted", new SumDiffVariable("Seasonally adj", data, "y", "effect", SumDiffVariable.DIFF));
		
		return data;
	}
	
	protected String[] getLineKeys() {
		String keys[] = {"adjusted"};
		return keys;
	}
	
	protected XPanel valuePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		XLabel heading = new XLabel(translate("Subtracting mean seasonal effect"), XLabel.LEFT, this);
		heading.setFont(getStandardBoldFont());
		thePanel.add(heading);
		return thePanel;
	}
}