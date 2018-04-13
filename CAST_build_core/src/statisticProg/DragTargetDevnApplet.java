package statisticProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;

import statistic.*;


public class DragTargetDevnApplet extends DragCrossDevnApplet {
	private XButton meanButton, medianButton;
	
	private DragValAxis theHorizAxis;
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			NumValue target = new NumValue(getParameter(TARGET_PARAM));
			
			theHorizAxis = new DragValAxis(this);
			String labelInfo = getParameter(AXIS_INFO_PARAM);
			theHorizAxis.readNumLabels(labelInfo);
			try {
				theHorizAxis.setAxisVal(target);
			} catch (AxisException e) {
			}
		thePanel.add("Bottom", theHorizAxis);
		
		
			NumValue maxSummary = new NumValue(getParameter(MAX_SUMMARY_PARAM));
			theView = new DragCrossView(data, this, theHorizAxis, DragCrossView.ABS_DEVN, target.toDouble(),
																																				null, maxSummary.decimals);
			theHorizAxis.setView(theView);
			theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	protected XPanel buttonPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 4));
		
		medianButton = new XButton("Set to Median", this);
		thePanel.add(medianButton);
		meanButton = new XButton("Set to Mean", this);
		thePanel.add(meanButton);
		
		return thePanel;
	}
	
	protected XPanel summaryPanel(DataSet data) {
		XPanel thePanel = super.summaryPanel(data);
		DataView otherViews[] = new DataView[1];
		otherViews[0] = summaryValue;
		theHorizAxis.setOtherLinkedViews(otherViews);
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == meanButton || target == medianButton) {
			NumVariable y = (NumVariable)data.getVariable("y");
			NumValue[] sortedData = y.getSortedData();
			int nValues = sortedData.length;
			double summary = 0.0;
			if (target == meanButton) {
				for (int i=0 ; i<nValues ; i++)
					summary += sortedData[i].toDouble();
				summary /= nValues;
			}
			else {
				summary = sortedData[nValues / 2].toDouble();
				if (nValues % 2 == 0)
					summary = 0.5 * (summary + sortedData[(nValues - 1) / 2].toDouble());
					
			}
			
			try {
				theHorizAxis.setAxisVal(summary);
			} catch (AxisException e) {
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