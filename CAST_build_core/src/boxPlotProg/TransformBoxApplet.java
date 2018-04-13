package boxPlotProg;

import java.awt.*;

import axis.*;
import dataView.*;


public class TransformBoxApplet extends RandomGroupedBoxApplet {
	static final private String POWER_PARAM = "power";
	
	protected void generateNumValues() {
		double power = Integer.parseInt(getParameter(POWER_PARAM));
		
		double value[] = new double[totalCount];
		int itemIndex = 0;
		for (int i=0 ; i<group.size() ; i++) {
			GroupInfo groupIInfo = (GroupInfo)(group.elementAt(i));
			double groupVal[] = groupIInfo.generator.generate();
			
			
			for (int j=0 ; j<groupIInfo.sampleSize ; j++)
				value[itemIndex++] = Math.pow(groupVal[j], power);
		}
		data.getNumVariable().setValues(value);
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
		
		thePanel.add(createDotBoxChoice());
		
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		TransformHorizAxis theHorizAxis = new TransformHorizAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		thePanel.add("Bottom", theHorizAxis);
		
		VertAxis theVertAxis = new VertAxis(this);
		CatVariable groupVariable = data.getCatVariable();
		theVertAxis.setCatLabels(groupVariable);
		thePanel.add("Left", theVertAxis);
		
		theView = createView(data, theHorizAxis, theVertAxis);
		theHorizAxis.setLinkedData(data, true);
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
}