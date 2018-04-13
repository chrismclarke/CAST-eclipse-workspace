package residTwoProg;

import java.awt.*;

import axis.*;
import utils.*;
import dataView.*;
import models.*;
import valueList.*;
import graphics3D.*;

import multiRegn.*;
import resid.*;


public class DragXZOutlier2Applet extends DragXZOutlierApplet {
	static final private String FIT_AXIS_PARAM = "fitAxis";
	
	static final private int kParamDecimals[] = {9, 9, 9};
	
	static final private int kResidScatterWidth = 270;
	
	private StdResidPlotView residView;
	
	private XChoice residTypeChoice;
	private int currentResidType = 0;
	
	protected DataSet readData() {
		data = new MultiRegnDataSet(this);
		
		data.addVariable("fit", new FittedValueVariable("Fitted value", data, MultiRegnDataSet.xKeys,
																			"ls", 9));
		
		data.addVariable("delResid", new DeletedResidVariable("Deleted resid", data,
																																			MultiRegnDataSet.xKeys, "y", "ls", 9));
			MultipleRegnModel deletedLS = new MultipleRegnModel("Deleted LS", data, MultiRegnDataSet.xKeys);
			deletedLS.setLSParams("y", kParamDecimals, 9);
		data.addVariable("deletedLS", deletedLS);
		
		data.addVariable("stdResid", new StdResidValueVariable(translate("Std resid"), data,
																									MultiRegnDataSet.xKeys, "y", "ls", 2));
		
		data.addVariable("extStudentResid", new ExtStudentResidVariable(translate("Deleted resid"), data,
																							MultiRegnDataSet.xKeys, "y", "ls", "deletedLS", 2));
		
		return data;
	}
	
	protected void addRotateButtons(XPanel thePanel, Rotate3DView theView) {
		thePanel.add("South", RotateButton.createRotationPanel(theView, this,
																																		RotateButton.HORIZONTAL));
	}
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XPanel choicePanel = new XPanel();
			choicePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
				residTypeChoice = new XChoice(this);
				residTypeChoice.addItem(translate("Ordinary standardised residual"));
				residTypeChoice.addItem(translate("Externally studentised residual"));
			
			choicePanel.add(residTypeChoice);
		thePanel.add("North", residTypeChoice);
		
			XPanel innerPanel = new XPanel();
			innerPanel.setLayout(new FixedSizeLayout(kResidScatterWidth, 0));
			
				XPanel dataPanel = new XPanel();
				dataPanel.setLayout(new AxisLayout());
			
					HorizAxis fitAxis = new HorizAxis(this);
					fitAxis.readNumLabels(getParameter(FIT_AXIS_PARAM));
					fitAxis.setAxisName(translate("Fitted values"));
				dataPanel.add("Bottom", fitAxis);
				
					VertAxis vertResidAxis = new VertAxis(this);
					vertResidAxis.readNumLabels(getParameter(RESID_AXIS_INFO_PARAM));
				dataPanel.add("Left", vertResidAxis);
				
					residView = new StdResidPlotView(data, this, fitAxis, vertResidAxis, "fit", "stdResid");
					residView.lockBackground(Color.white);
				dataPanel.add("Center", residView);
				
			innerPanel.add(dataPanel);
			
		thePanel.add("Center", innerPanel);
		
			XPanel residValuePanel = new XPanel();
			residValuePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 3));
			
				NumValue maxResid = new NumValue(999, 2);
			residValuePanel.add(new OneValueView(data, "stdResid", this, maxResid));
			residValuePanel.add(new OneValueView(data, "extStudentResid", this, maxResid));
		
		thePanel.add("South", residValuePanel);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == residTypeChoice) {
			int newChoice = residTypeChoice.getSelectedIndex();
			if (newChoice != currentResidType) {
				currentResidType = newChoice;
				residView.changeVariables((newChoice == 0) ? "stdResid" : "extStudentResid", "fit");
			}
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