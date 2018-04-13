package residProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import models.*;

import resid.*;


public class DragStdResidApplet extends XApplet {
	static final protected String RESID_AXIS_PARAM = "residAxis";
	
	private StdResidPlotView residView;
	
	private XChoice residTypeChoice;
	private int currentResidType = 0;
	
	public void setupApplet() {
		SimpleRegnDataSet data = readData();
		
		setLayout(new BorderLayout(10, 0));
		
		add("North", topPanel(data));
		add("Center", displayPanel(data));
	}
	
	protected SimpleRegnDataSet readData() {
		SimpleRegnDataSet data = new SimpleRegnDataSet(this);
		
		data.addVariable("delResid", new DeletedResidVariable("Deleted resid", data,
																																			"x", "y", "ls", 9));
			LinearModel deletedLS = new LinearModel("Deleted LS", data, "x");
			deletedLS.setLSParams("y", 9, 9, 9);
		data.addVariable("deletedLS", deletedLS);
		
		data.addVariable("stdResid", new StdResidValueVariable("Std resid", data, "x", "y",
																																						"ls", 9));
		
		data.addVariable("extStudentResid", new ExtStudentResidVariable("Deleted resid", data,
																													"x", "y", "ls", "deletedLS", 9));
		
		return data;
	}
	
	protected XPanel topPanel(SimpleRegnDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 10));
		
		thePanel.add(ProportionLayout.LEFT, new XLabel(data.getYVarName(), XLabel.LEFT, this));
			
			XPanel choicePanel = new XPanel();
			choicePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
				residTypeChoice = new XChoice(this);
				residTypeChoice.addItem(translate("Ordinary standardised residual"));
				residTypeChoice.addItem(translate("Externally studentised residual"));
			
			choicePanel.add(residTypeChoice);
		thePanel.add(ProportionLayout.RIGHT, choicePanel);
		
		return thePanel;
	}
	
	private XPanel displayPanel(SimpleRegnDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 10));
		
		thePanel.add(ProportionLayout.LEFT, dataPlot(data));
		thePanel.add(ProportionLayout.RIGHT, residPlot(data));
		return thePanel;
	}
	
	private XPanel dataPlot(SimpleRegnDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis dataXAxis = new HorizAxis(this);
			dataXAxis.readNumLabels(data.getXAxisInfo());
			dataXAxis.setAxisName(data.getXVarName());
		thePanel.add("Bottom", dataXAxis);
		
			VertAxis dataYAxis = new VertAxis(this);
			dataYAxis.readNumLabels(data.getYAxisInfo());
		thePanel.add("Left", dataYAxis);
		
			DragLeverageView dataView = new DragLeverageView(data, this, dataXAxis, dataYAxis, "x", "y", "ls");
			dataView.lockBackground(Color.white);
			dataView.setAdjustLS(true);
		thePanel.add("Center", dataView);
		return thePanel;
	}
	
	private XPanel residPlot(SimpleRegnDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis residXAxis = new HorizAxis(this);
			residXAxis.readNumLabels(data.getXAxisInfo());
			residXAxis.setAxisName(data.getXVarName());
		thePanel.add("Bottom", residXAxis);
		
			VertAxis vertResidAxis = new VertAxis(this);
			vertResidAxis.readNumLabels(getParameter(RESID_AXIS_PARAM));
		thePanel.add("Left", vertResidAxis);
		
			residView = new StdResidPlotView(data, this, residXAxis, vertResidAxis, "x", "stdResid");
			residView.lockBackground(Color.white);
		thePanel.add("Center", residView);
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == residTypeChoice) {
			int newChoice = residTypeChoice.getSelectedIndex();
			if (newChoice != currentResidType) {
				currentResidType = newChoice;
				residView.changeVariables((newChoice == 0) ? "stdResid" : "extStudentResid", "x");
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

