package testProg;

import java.awt.*;

import axis.*;
import dataView.*;
import test.*;


public class DragPValueApplet extends XApplet {
	public void setupApplet() {
		DataSet data = new DataSet();
//		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		
		setLayout(new BorderLayout());
		
		add("North", new TwoLineLabel("Distribution of p-value", "(assuming null hypothesis)", this));
		add("Center", displayPanel(data));
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		DragValAxis theHorizAxis = new DragPValueAxis(this);
		theHorizAxis.readNumLabels("-0.1 1.1 0 1");
		thePanel.add("Bottom", theHorizAxis);
		
		VertAxis theVertAxis = new VertAxis(this);
		theVertAxis.readNumLabels("0 1.5 0 1");
		thePanel.add("Left", theVertAxis);
		
		RectangularView theView = new RectangularView(data, this, theHorizAxis, theVertAxis);
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
}