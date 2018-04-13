package loessProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import coreGraphics.*;

import loess.*;


public class TransformScatterApplet extends ScatterApplet {
	static final protected String TRANSFORM_X_AXIS_PARAM = "transXAxis";
	static final protected String MAX_X_TRANSFORMED_PARAM = "maxXTransformed";
	static final protected String LOESS_POINTS_PARAM = "loessPoints";
	static final private String DO_LOESS_PARAM = "doLoess";
	
	protected ScatterLoessView theView;
	
	private XCheckbox loessCheck;
	private boolean doLoess = true;
	
	public void setupApplet() {
		String doLoessString = getParameter(DO_LOESS_PARAM);
		if (doLoessString != null && doLoessString.equals("false"))
			doLoess = false;
		super.setupApplet();
	}
	
	protected HorizAxis createHorizAxis(DataSet data) {
		String transAxisString = getParameter(TRANSFORM_X_AXIS_PARAM);
		
		DualTransHorizAxis axis = new DualTransHorizAxis(this, transAxisString);
		axis.setLinkedData(data, true);
		String maxTransformedString = getParameter(MAX_X_TRANSFORMED_PARAM);
		if (maxTransformedString != null)
			axis.setMaxTransformed(maxTransformedString);
		
		String labelInfo = getParameter(X_AXIS_INFO_PARAM);
		axis.readNumLabels(labelInfo);
		axis.setAxisName(getParameter(X_VAR_NAME_PARAM));
		
		return axis;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis) {
		if (doLoess) {
			int loessWindowWidth = Integer.parseInt(getParameter(LOESS_POINTS_PARAM));
			LoessSmoothVariable loessVar = new LoessSmoothVariable("Loess smooth", data, "x", "y");
			data.addVariable("loess", loessVar);
			loessVar.setAxes(theHorizAxis, theVertAxis);
			loessVar.initialise(loessWindowWidth);
		}
		
		theView = new ScatterLoessView(data, this, theHorizAxis, theVertAxis, "x", "y", "loess");
		return theView;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		if (doLoess) {
			loessCheck = new XCheckbox(translate("Show lowess smooth"), this);
			thePanel.add(loessCheck);
		}
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (doLoess && target == loessCheck) {
			theView.setShowLoess(loessCheck.getState());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}