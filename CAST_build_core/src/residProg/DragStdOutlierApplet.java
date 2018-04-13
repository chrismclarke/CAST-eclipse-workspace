package residProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;

import resid.*;


public class DragStdOutlierApplet extends DragOutlierApplet {
	static final private String kStdResidScale = "-3 3 -3 1";
	
	private MultiVertAxis vertResidAxis;
	private HiliteOneResidualView residView;
	
	private XChoice residTypeChoice;
	private int currentResidType = 0;
	
	protected DataSet readData() {
		DataSet data = super.readData();
		data.addVariable("stdResid", new StdResidValueVariable("Std resid", data, "x", "y",
																																						"lsLine", 9));
		
		return data;
	}
	
	protected XPanel choicePanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		return thePanel;
	}
	
	protected XPanel createPlotPanel(DataSet data, boolean axisLabelValues, String xKey,
										String yKey, String labelKey, String xAxisInfo, String yAxisInfo,
										int plotIndex) {
		if (plotIndex == 0) {
			XPanel thePanel = super.createPlotPanel(data, axisLabelValues, xKey, yKey, labelKey,
																										xAxisInfo, yAxisInfo, plotIndex);
			dragView.setAdjustLS(true);
			return thePanel;
		}
		else {
			XPanel thePanel = new XPanel();
			thePanel.setLayout(new BorderLayout());
			
				XPanel plotPanel = new XPanel();
				plotPanel.setLayout(new AxisLayout());
				
					HorizAxis residXAxis = new HorizAxis(this);
					residXAxis.readNumLabels(getParameter(X_AXIS_INFO_PARAM));
						NumVariable xVar = (NumVariable)data.getVariable("x");
					residXAxis.setAxisName(xVar.name);
				plotPanel.add("Bottom", residXAxis);
				
					vertResidAxis = new MultiVertAxis(this, 2);
					vertResidAxis.readNumLabels(getParameter(RESID_AXIS_INFO_PARAM));
					vertResidAxis.readExtraNumLabels(kStdResidScale);
					vertResidAxis.setChangeMinMax(true);
				plotPanel.add("Left", vertResidAxis);
				
					residView = new HiliteOneResidualView(data, this, residXAxis, vertResidAxis, "x", "resid", null);
					residView.setAllowSelectPoint(false);
					residView.lockBackground(Color.white);
				plotPanel.add("Center", residView);
			
			thePanel.add("Center", plotPanel);
			
				XPanel topPanel = new XPanel();
				topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
				
						residTypeChoice = new XChoice(this);
						residTypeChoice.addItem(translate("Ordinary residual"));
						residTypeChoice.addItem(translate("Standardised residual"));
				topPanel.add(residTypeChoice);
			
			thePanel.add("North", topPanel);
			
			return thePanel;
		}
	}

	
	private boolean localAction(Object target) {
		if (target == residTypeChoice) {
			int newChoice = residTypeChoice.getSelectedIndex();
			if (newChoice != currentResidType) {
				currentResidType = newChoice;
				vertResidAxis.setAlternateLabels(newChoice);
				residView.changeVariables(newChoice == 0 ? "resid" : "stdResid", "x");
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}