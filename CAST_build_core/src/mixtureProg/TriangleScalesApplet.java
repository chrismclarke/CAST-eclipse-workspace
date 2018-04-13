package mixtureProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;

import mixture.*;


public class TriangleScalesApplet extends TriangleConstraintApplet {

	private XButton xScaleButton, yScaleButton, zScaleButton;
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = super.displayPanel(data);
		
		((TriangleConstraintView)theView).setDrawCorners(false);
		((TriangleConstraintView)theView).setAlwaysDrawAxesLabels(false);
		
		return thePanel;
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
		
			StringTokenizer st = new StringTokenizer(translate("Show * scale"), "*");
			String showString = st.nextToken();
			String scaleString = st.nextToken();
			
			xScaleButton = new XButton(showString + getParameter(X_VAR_NAME_PARAM) + scaleString, this);
		thePanel.add(xScaleButton);
		
			yScaleButton = new XButton(showString + getParameter(Y_VAR_NAME_PARAM) + scaleString, this);
		thePanel.add(yScaleButton);
		
			zScaleButton = new XButton(showString + getParameter(Z_VAR_NAME_PARAM) + scaleString, this);
		thePanel.add(zScaleButton);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == xScaleButton) {
			theView.animateRotateTo(0, 45);
			return true;
		}
		else if (target == yScaleButton) {
			theView.animateRotateTo(315, 360);
			return true;
		}
		else if (target == zScaleButton) {
			theView.animateRotateTo(270, 45);
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