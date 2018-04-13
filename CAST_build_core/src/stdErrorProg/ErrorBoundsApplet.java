package stdErrorProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;

import stdError.*;


public class ErrorBoundsApplet extends ErrorSampleApplet {
	static final protected String MAX_ERROR_BOUND_PARAM = "maxErrorBound";
	
	static final private int kMinForBounds = 100;
	
	private XCheckbox show95BoundsCheck;
	
	protected Stacked2SdBoundsView errorBoundsView;
	
	private XPanel boundsPanel;
	private CardLayout boundsPanelLayout;
	
	protected Stacked2SdBoundsView getStackedBoundsView(SummaryDataSet summaryData,
																														HorizAxis theHorizAxis) {
		return new StackedErrorBoundsView(summaryData, this, theHorizAxis, "est", target.toDouble());
	}
	
	protected XPanel summaryPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = getAxis(summaryData, "error", ERROR_AXIS_INFO_PARAM);
			theHorizAxis.setForeground(Color.red);
		thePanel.add("Bottom", theHorizAxis);
		
			errorBoundsView = getStackedBoundsView(summaryData, theHorizAxis);
			errorBoundsView.setActiveNumVariable("error");
			errorBoundsView.lockBackground(Color.white);
			errorBoundsView.setForeground(Color.red);
		thePanel.add("Center", errorBoundsView);
		
		return thePanel;
	}
	
	protected XPanel errorSummaryPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 20));
		
			XPanel checkPanel = new XPanel();
			checkPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
				show95BoundsCheck = new XCheckbox(translate("Show 95% bounds for error"), this);
				show95BoundsCheck.disable();
			checkPanel.add(show95BoundsCheck);
			
		thePanel.add(checkPanel);
		
			boundsPanel = new XPanel();
			boundsPanelLayout = new CardLayout();
			boundsPanel.setLayout(boundsPanelLayout);
			
			boundsPanel.add("blank", new XPanel());
			
			boundsPanel.add("bounds", boundsValuePanel(summaryData));
			
		thePanel.add(boundsPanel);
		
		return thePanel;
	}
	
	protected XPanel boundsValuePanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 5));
		
			ErrorBoundsView boundsView = new ErrorBoundsView(summaryData, this, errorBoundsView,
																								new NumValue(getParameter(MAX_ERROR_BOUND_PARAM)));
		
			boundsView.setFont(getBigBoldFont());
			boundsView.setForeground(Color.blue);
		thePanel.add(boundsView);
		
		thePanel.lockBackground(Color.white);
		return thePanel;
	}
	
	protected void doTakeSample() {
		summaryData.takeSample();
		
		int noOfSamples = ((NumVariable)summaryData.getVariable("error")).noOfValues();
		if (noOfSamples >= kMinForBounds)
			show95BoundsCheck.enable();
	}
	
	protected void doChangeAccumulate() {
		summaryData.setAccumulate(accumulateCheck.getState());
		if (show95BoundsCheck != null) {
			show95BoundsCheck.setState(false);
			show95BoundsCheck.disable();
			boundsPanelLayout.show(boundsPanel, "blank");
			errorBoundsView.setShowBounds(false);
			errorBoundsView.repaint();
		}
	}

	
	private boolean localAction(Object target) {
		if (target == show95BoundsCheck) {
			boolean showBounds = show95BoundsCheck.getState();
			boundsPanelLayout.show(boundsPanel, showBounds ? "bounds" : "blank");
			errorBoundsView.setShowBounds(showBounds);
			errorBoundsView.repaint();
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