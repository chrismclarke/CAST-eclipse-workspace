package transformProg;

import java.awt.*;

import dataView.*;
import axis.*;
import valueList.OneValueView;

import transform.*;


public class StanineApplet extends ZScoreApplet {
	protected OneValueView stanineView;
	
	protected DataSet getData() {
		DataSet data = super.getData();
		
		data.addVariable("stanine", new StanineVariable(translate("Stanine"), data, "z0"));
		
		return data;
	}
	
	protected XPanel valuePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 12, 0));
		
		if (hasLabels)
			thePanel.add(new OneValueView(data, "label", this));
		
			zView = new OneValueView(data, "z0", this);
			zView.setForeground(Color.blue);
		thePanel.add(zView);
		
			stanineView = new OneValueView(data, "stanine", this);
		thePanel.add(stanineView);
		return thePanel;
	}
	
	protected DataView coreView(DataSet data, NumCatAxis theHorizAxis) {
		return new StanineDotPlotView(data, this, theHorizAxis, zAxis);
	}
	
	protected void changeVariable(int i) {
		StanineVariable stanine = (StanineVariable)data.getVariable("stanine");
		stanine.setBaseVariable("z" + i);
		super.changeVariable(i);
	}
}