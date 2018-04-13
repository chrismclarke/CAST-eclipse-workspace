package statisticProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;

import statistic.*;


public class DragTargetRmseApplet extends DragCrossRmseApplet {
	static final private Color kTargetColor = new Color(0x006600);	//	dark green
	
	private DragValAxis theHorizAxis;
	
	private XButton meanButton;
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			theHorizAxis = new DragValAxis(this);
			String labelInfo = getParameter(AXIS_INFO_PARAM);
			theHorizAxis.readNumLabels(labelInfo);
			theHorizAxis.setAxisName(data.getVariable("y").name);
			theHorizAxis.setValueColor(kTargetColor);
			try {
				theHorizAxis.setAxisVal(target);
			} catch (AxisException e) {
			}
		thePanel.add("Bottom", theHorizAxis);
		
			theView = new DragCrossView(data, this, theHorizAxis, DragCrossView.SQR_DEVN, target,
																					getParameter(TARGET_NAME_PARAM), maxSummary.decimals);
			theHorizAxis.setView(theView);
			theView.setKeepTargetToMean(keepTargetToMean);
			theView.setCrossSize(DataView.LARGE_CROSS);
			theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected LayoutManager getSummaryLayout() {
		return new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 5);
	}
	
	protected XPanel valuePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 0, ProportionLayout.HORIZONTAL,
																										ProportionLayout.REMAINDER));
		
			XPanel summaryPanel = new XPanel();
			summaryPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																														VerticalLayout.VERT_CENTER, 0));
			summaryPanel.add(super.valuePanel(data));
			
		thePanel.add(ProportionLayout.LEFT, summaryPanel);
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																														VerticalLayout.VERT_CENTER, 5));
			
				meanButton = new XButton("Set k = mean", this);
			rightPanel.add(meanButton);
			
		thePanel.add(ProportionLayout.RIGHT, rightPanel);
		
		DataView otherViews[] = new DataView[1];
		otherViews[0] = summaryValue;
		theHorizAxis.setOtherLinkedViews(otherViews);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == meanButton) {
			NumVariable y = (NumVariable)data.getVariable("y");
			
			ValueEnumeration ye = y.values();
			double sy = 0.0;
			while (ye.hasMoreValues())
				sy += ye.nextDouble();
			
			try {
				theHorizAxis.setAxisVal(sy / y.noOfValues());
			} catch (AxisException e) {
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