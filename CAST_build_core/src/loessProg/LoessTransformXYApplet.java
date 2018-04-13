package loessProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;

import loess.*;


public class LoessTransformXYApplet extends TransformScatterApplet {
	static final protected String TRANSFORM_Y_AXIS_PARAM = "transYAxis";
	static final protected String MAX_Y_TRANSFORMED_PARAM = "maxYTransformed";
	
	private HorizAxis theHorizAxis;
	private DualTransVertAxis theVertAxis;
	
	private XButton resetButton;
	
	protected HorizAxis createHorizAxis(DataSet data) {
		theHorizAxis = super.createHorizAxis(data);
		theHorizAxis.setAxisName(getParameter(X_VAR_NAME_PARAM));
		return theHorizAxis;
	}
	
	protected VertAxis createVertAxis(DataSet data) {
		String transAxisString = getParameter(TRANSFORM_Y_AXIS_PARAM);
		
		theVertAxis = new DualTransVertAxis(this, transAxisString);
		String maxTransformedString = getParameter(MAX_Y_TRANSFORMED_PARAM);
		if (maxTransformedString != null)
			theVertAxis.setMaxTransformed(maxTransformedString);
//		theVertAxis = new TransformVertAxis(this);
		
		theVertAxis.setLinkedData(data, true);
			
		String labelInfo = getParameter(Y_AXIS_INFO_PARAM);
		theVertAxis.readNumLabels(labelInfo);
		theVertAxis.setAxisName(getParameter(Y_VAR_NAME_PARAM));
		return theVertAxis;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis,
																					VertAxis theVertAxis) {
		ScatterLoessView theView = (ScatterLoessView)super.createDataView(data,
																				theHorizAxis, theVertAxis);
		theView.setShowLoess(true);
		return theView;
	}
	
	protected XPanel controlPanel(DataSet data) {
		return null;
	}
	
	protected XPanel rightPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			resetButton = new XButton(translate("Reset"), this);
		thePanel.add(resetButton);
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == resetButton) {
			theHorizAxis.setPower(1.0);
			theVertAxis.setPower(1.0);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
}