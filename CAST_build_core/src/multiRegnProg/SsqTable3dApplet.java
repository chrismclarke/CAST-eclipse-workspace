package multiRegnProg;

import java.awt.*;

import dataView.*;

import multiRegn.*;


public class SsqTable3dApplet extends ResidSsq3dApplet {
	
	protected XPanel ssqPanel(DataSet data, NumValue maxSsq) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			theTable = new MultiAnovaTableView(data, this, "model", "y", explanKey, explanName, maxSsq);
		thePanel.add("Center", theTable);
		
		return thePanel;
	}
}