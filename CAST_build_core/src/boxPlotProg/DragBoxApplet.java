package boxPlotProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import boxPlot.*;


public class DragBoxApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	
	DragBoxView theView;
	XCheckbox hintsCheck;
	
	public void setupApplet() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		
		setLayout(new BorderLayout());
		add("Center", dataPanel(data));
		
		add("South", controlPanel(data));
	}
	
	protected XPanel dataPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		thePanel.add("Bottom", theHorizAxis);
		
		theView = createView(data, theHorizAxis);
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
	
	protected DragBoxView createView(DataSet data, HorizAxis theHorizAxis) {
		return new DragBoxView(data, this, theHorizAxis);
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 3));
		hintsCheck = new XCheckbox("Show Errors", this);
		thePanel.add(hintsCheck);
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == hintsCheck) {
			theView.showHints(hintsCheck.getState());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}