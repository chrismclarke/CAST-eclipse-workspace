package propnVennProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import propnVenn.*;


public class AreaContinIconApplet extends AreaContin2Applet {
	
	static final private Rectangle staticPoints[]
											= {new Rectangle(0, 0, 6, 3), new Rectangle(8, 0, 4, 4),
												new Rectangle(0, 4, 8, 4), new Rectangle(10, 6, 2, 2)};
	static final private GridIterator dynStartPoints[]
										= {new GridIterator(6, 0, 2, 3, true), new GridIterator(8, 4, 4, 2, true),
											new GridIterator(0, 3, 8, 2, true), new GridIterator(8, 6, 2, 2, true)};
	static final private GridIterator dynEndPoints[]
										= {new GridIterator(0, 3, 6, 1, true), new GridIterator(6, 0, 2, 4, true),
											new GridIterator(8, 4, 2, 4, true), new GridIterator(10, 4, 2, 2, true)};
	
	static final private int gridWidth = 12;
	static final private int gridHeight = 8;
	
	private XCheckbox shadeAreaCheck;
	
	public void setupApplet() {
		super.setupApplet();
		
		ItemImages.loadApples(this);
	}
	
	protected AreaContinCoreView getPropnVenn(DataSet data, VertAxis vertAxis,
																	HorizAxis horizAxis, boolean yMarginal) {
		return new AreaContinIconView(data, this, vertAxis, horizAxis, "y", "x", gridWidth, gridHeight, staticPoints,
																			dynStartPoints, dynEndPoints, ItemImages.apples);
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
			XChoice marginChoice = new XChoice(this);
			String xName = data.getVariable("x").name;
			String yName = data.getVariable("y").name;
			
			marginChoice.addItem(translate("Group by") + " " + yName);
			marginChoice.addItem(translate("Group by") + " " + xName);
			
		thePanel.add(new PickMarginPanel(xAxisLabel, yAxisLabel, theView, marginChoice));
			
			shadeAreaCheck = new XCheckbox(translate("Hide Icons"), this);
		thePanel.add(shadeAreaCheck);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == shadeAreaCheck) {
			((AreaContinIconView)theView).setShadeAreas(shadeAreaCheck.getState());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}