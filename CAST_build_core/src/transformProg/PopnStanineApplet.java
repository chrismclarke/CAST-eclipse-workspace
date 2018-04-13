package transformProg;

import java.awt.*;

import dataView.*;
import axis.*;
import valueList.OneValueView;
import coreVariables.*;


public class PopnStanineApplet extends StanineApplet {
	static final private String Z_AXIS_INFO_PARAM = "zAxis";
	static final private String Z_TRANSFORM_PARAM = "zTransform";
	
	
	protected void addZScoreVariables(DataSet data) {
		String transformParam = getParameter(Z_TRANSFORM_PARAM);
		for (int i=0 ; i<noOfVariables ; i++) {
			NumVariable baseVariable = (NumVariable)data.getVariable("y" + i);
			data.addVariable("z" + i, new ScaledVariable("Z score", baseVariable, "y" + i, transformParam));
		}
	}
	
	protected HorizAxis createZAxis(DataSet data, HorizAxis xAxis) {
		HorizAxis zAxis = new HorizAxis(this);
		String labelInfo = getParameter(Z_AXIS_INFO_PARAM);
		zAxis.readNumLabels(labelInfo);
		zAxis.setAxisName(translate("z-score"));
		return zAxis;
	}
	
	protected XPanel valuePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 12, 0));
		
			yView = new OneValueView(data, "y0", this);
			yView.setLabel(getParameter(MEASUREMENT_PARAM));
		thePanel.add(yView);
		
		thePanel.add(super.valuePanel(data));
		return thePanel;
	}
}