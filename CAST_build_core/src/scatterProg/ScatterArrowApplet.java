package scatterProg;

import java.awt.*;

import axis.*;
import dataView.*;
import valueList.*;
import coreGraphics.*;
import utils.*;

import scatter.*;


public class ScatterArrowApplet extends ScatterApplet {
	static final protected String X_UNITS_PARAM = "xUnits";
	static final protected String Y_UNITS_PARAM = "yUnits";
	static final protected String JOIN_POINTS_PARAM = "joinPoints";
	
	private ScatterArrowView theView;
	private XCheckbox joinLinesCheck;
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		theView = new ScatterArrowView(data, this, theHorizAxis, theVertAxis, "x", "y");
		theView.setRetainLastSelection(true);
		String joinString = getParameter(JOIN_POINTS_PARAM);
		theView.setJoinPoints(joinString != null && joinString.equals("true"));
		return theView;
	}
	
	protected XPanel topPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 0, 0, 5);
		thePanel.setLayout(new BorderLayout(0, 12));
		
			if (getParameter(LABEL_NAME_PARAM) != null) {
				XPanel topPanel = new XPanel();
				topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
					OneValueView labelView = new OneValueView(data, "label", this);
					labelView.addEqualsSign();
					labelView.setFont(getBigFont());
				topPanel.add(labelView);
				
				thePanel.add("North", topPanel);
			}
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			
				OneValueView vertAxisLabel = new OneValueView(data, "y", this);
				String yUnitsString = getParameter(Y_UNITS_PARAM);
				if (yUnitsString != null)
					vertAxisLabel.setUnitsString(yUnitsString);
				vertAxisLabel.addEqualsSign();
				vertAxisLabel.setForeground(ScatterArrowView.kVertAxisColor);
				vertAxisLabel.setFont(getBigFont());
			bottomPanel.add(vertAxisLabel);
		
		thePanel.add("Center", bottomPanel);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel namePanel = new XPanel();
		namePanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		
			OneValueView horizAxisLabel = new OneValueView(data, "x", this);
			String xUnitsString = getParameter(X_UNITS_PARAM);
			if (xUnitsString != null)
				horizAxisLabel.setUnitsString(xUnitsString);
			horizAxisLabel.addEqualsSign();
			horizAxisLabel.setForeground(ScatterArrowView.kHorizAxisColor);
			horizAxisLabel.setFont(getBigFont());
		namePanel.add(horizAxisLabel);
		
		String joinString = getParameter(JOIN_POINTS_PARAM);
		if (joinString != null && joinString.equals("check")) {
			XPanel thePanel = new XPanel();
			thePanel.setLayout(new BorderLayout(0, 5));
			
			thePanel.add("Center", namePanel);
			
				XPanel checkPanel = new XPanel();
				checkPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
					joinLinesCheck = new XCheckbox(translate("Join crosses"), this);
				checkPanel.add(joinLinesCheck);
				
			thePanel.add("South", checkPanel);
			return thePanel;
		}
		else
			return namePanel;
	}
	
	protected HorizAxis createHorizAxis(DataSet data) {
		HorizAxis axis = super.createHorizAxis(data);
		axis.setForeground(ScatterArrowView.kHorizAxisColor);
		return axis;
	}
	
	protected VertAxis createVertAxis(DataSet data) {
		VertAxis axis = super.createVertAxis(data);
		axis.setForeground(ScatterArrowView.kVertAxisColor);
		return axis;
	}

	
	private boolean localAction(Object target) {
		if (target == joinLinesCheck) {
			theView.setJoinPoints(joinLinesCheck.getState());
			theView.repaint();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}