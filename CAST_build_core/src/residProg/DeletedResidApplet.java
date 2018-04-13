package residProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import models.*;

import resid.*;


public class DeletedResidApplet extends XApplet {
	static final protected String RESID_AXIS_PARAM = "residAxis";
	static final protected String SD_DECIMALS_PARAM = "sdDecimals";
	
	protected HiliteOneResidualView residView;
	
	public void setupApplet() {
		SimpleRegnDataSet data = readData();
		
		setLayout(new BorderLayout(10, 0));
		
		add("North", topPanel(data));
		add("Center", displayPanel(data));
		add("South", bottomPanel(data));
	}
	
	protected SimpleRegnDataSet readData() {
		SimpleRegnDataSet data = new SimpleRegnDataSet(this);
		
		data.addVariable("delResid", new DeletedResidVariable("Deleted resid", data,
																																			"x", "y", "ls", 9));
			LinearModel deletedLS = new LinearModel("Deleted LS", data, "x");
			deletedLS.setLSParams("y", 9, 9, 9);
		data.addVariable("deletedLS", deletedLS);
		
		String sdDecimalsString = getParameter(SD_DECIMALS_PARAM);
		if (sdDecimalsString != null) {
			int sdDecimals = Integer.parseInt(sdDecimalsString);
			LinearModel ls = (LinearModel)data.getVariable("ls");
			ls.evaluateSD().decimals = sdDecimals;
			deletedLS.evaluateSD().decimals = sdDecimals;
		}
		
		return data;
	}
	
	protected XPanel topPanel(SimpleRegnDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 10));
		
		thePanel.add(ProportionLayout.LEFT, new XLabel(data.getYVarName(), XLabel.LEFT, this));
		thePanel.add(ProportionLayout.RIGHT, new XLabel(translate("Deleted residual"), XLabel.LEFT, this));
		
		return thePanel;
	}
	
	protected XPanel bottomPanel(SimpleRegnDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		return thePanel;
	}
	
	private XPanel displayPanel(SimpleRegnDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 10));
		
		thePanel.add(ProportionLayout.LEFT, dataPlot(data));
		thePanel.add(ProportionLayout.RIGHT, residPlot(data));
		return thePanel;
	}
	
	protected DeletedResidView getDataView(SimpleRegnDataSet data, HorizAxis dataXAxis, VertAxis dataYAxis) {
		return new DeletedResidView(data, this, dataXAxis, dataYAxis, "x", "y", "ls", "deletedLS");
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
		
			DeletedResidView dataView = getDataView(data, dataXAxis, dataYAxis);
			dataView.lockBackground(Color.white);
		thePanel.add("Center", dataView);
		return thePanel;
	}
	
	protected String getResidKey() {
		return "delResid";
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
		
			residView = new HiliteOneResidualView(data, this, residXAxis, vertResidAxis, "x", getResidKey(), null);
			residView.lockBackground(Color.white);
		thePanel.add("Center", residView);
		return thePanel;
	}
}

