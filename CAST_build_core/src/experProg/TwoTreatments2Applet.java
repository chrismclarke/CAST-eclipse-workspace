package experProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import coreGraphics.*;
import coreSummaries.*;


public class TwoTreatments2Applet extends TwoTreatmentsApplet {
	static final private String DIFF_AXIS_PARAM = "diffAxis";
	static final private String DIFF_NAME_PARAM = "diffName";
	
	private boolean doAnimation = true;
	
	private XCheckbox accumulateCheck, animateCheck;
	
	protected double getHorizLayoutPropn() {
		return 0.5;
	}
	
	protected SummaryDataSet getSummaryData(DataSet data) {
		SummaryDataSet summaryData = super.getSummaryData(data);
		summaryData.addVariable("diff", new DiffSummaryVariable("Difference", "response", "treat1", 9));
		
		return summaryData;
	}
	
	protected XPanel responsePlotPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.6, 10, ProportionLayout.HORIZONTAL,
																																	ProportionLayout.TOTAL));
		
		thePanel.add(ProportionLayout.LEFT, super.responsePlotPanel(data));
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new BorderLayout(0, 0));
				
			rightPanel.add("North", new XLabel(getParameter(DIFF_NAME_PARAM), XLabel.LEFT, this));
				
				XPanel diffPanel = new XPanel();
				diffPanel.setLayout(new AxisLayout());
				
					VertAxis vertAxis = new VertAxis(this);
					vertAxis.readNumLabels(getParameter(DIFF_AXIS_PARAM));
				diffPanel.add("Left", vertAxis);
				
					DotPlotView theView = new DotPlotView(summaryData, this, vertAxis, 1.0);
					theView.setActiveNumVariable("diff");
					theView.lockBackground(Color.white);
				diffPanel.add("Center", theView);
				
			rightPanel.add("Center", diffPanel);
				
				XPanel accumulatePanel = new InsetPanel(0, 20, 0, 0);
				accumulatePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 3));
				
					accumulateCheck = new XCheckbox(translate("Accumulate"), this);
				accumulatePanel.add(accumulateCheck);
				
					animateCheck = new XCheckbox(translate("Animate"), this);
					animateCheck.setState(true);
				accumulatePanel.add(animateCheck);
				
			rightPanel.add("South", accumulatePanel);
		
		thePanel.add(ProportionLayout.RIGHT, rightPanel);
		
		return thePanel;
	}
	
	protected void perpareForAnimation() {
		if (doAnimation)
			super.perpareForAnimation();
	}
	
	protected void startAnimation() {
		if (doAnimation)
			super.startAnimation();
	}
	
	private boolean localAction(Object target) {
		if (target == accumulateCheck) {
			summaryData.setAccumulate(accumulateCheck.getState());
			return true;
		}
		else if (target == animateCheck) {
			doAnimation = animateCheck.getState();
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