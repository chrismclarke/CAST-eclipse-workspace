package residProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import models.*;

import resid.*;
import regnView.*;


public class StudentResidApplet extends XApplet {
	static final protected String RESID_AXIS_PARAM = "residAxis";
	
	static final private int kDescriptionWidth = 540;
	
	private SimpleRegnDataSet data;
	
	private XLabel yNameLabel;
	
	private MultiVertAxis vertResidAxis;
	private VertAxis dataYAxis;
	private HorizAxis dataXAxis, residXAxis;
	private LSScatterView dataView, residView;
	
	private XTextArea dataDescription;
	
	private XChoice dataSetChoice;
	
	private XChoice residTypeChoice;
	private int currentResidType = 0;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout(10, 0));
		
		add("North", topPanel(data));
		add("Center", displayPanel(data));
	}
	
	private SimpleRegnDataSet readData() {
		SimpleRegnDataSet data = new SimpleRegnDataSet(this);
		data.addVariable("resid", new ResidValueVariable("Resid", data, "x", "y", "ls", 9));
		data.addVariable("stdResid", new StdResidValueVariable("Std resid", data, "x", "y",
																																									"ls", 9));
		data.addVariable("zero", new LinearModel("Zero", data, "x", LinearModel.kZero,
																										LinearModel.kZero, LinearModel.kZero));
		return data;
	}
	
	private XPanel topPanel(SimpleRegnDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(4, 7));
		
			XPanel dataChoicePanel = new XPanel();
			dataChoicePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
				
				XLabel dataLabel = new XLabel(translate("Data set") + ": ", XLabel.LEFT, this);
				dataLabel.setFont(getStandardBoldFont());
			dataChoicePanel.add(dataLabel);
			
				dataSetChoice = data.dataSetChoice(this);
			dataChoicePanel.add(dataSetChoice);
		
		thePanel.add("North", dataChoicePanel);
		
			dataDescription = new XTextArea(data.getDescriptionStrings(), 0, kDescriptionWidth, this);
			dataDescription.lockBackground(Color.white);
		
		thePanel.add("Center", dataDescription);
		
			XPanel headingPanel = new XPanel();
			headingPanel.setLayout(new ProportionLayout(0.5, 10));
				
				XPanel yNamePanel = new XPanel();
					yNamePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_BOTTOM, 0));
					yNameLabel = new XLabel(data.getYVarName(), XLabel.LEFT, this);
				yNamePanel.add(yNameLabel);
				
			headingPanel.add(ProportionLayout.LEFT, yNamePanel);
			
				XPanel rightPanel = new XPanel();
				rightPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
				
					residTypeChoice = new XChoice(this);
					residTypeChoice.addItem(translate("Ordinary residual"));
					residTypeChoice.addItem(translate("Standardised residual"));
				rightPanel.add(residTypeChoice);
				
			headingPanel.add(ProportionLayout.RIGHT, rightPanel);
		
		thePanel.add("South", headingPanel);
		
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
		
			dataXAxis = new HorizAxis(this);
			dataXAxis.readNumLabels(data.getXAxisInfo());
			dataXAxis.setAxisName(data.getXVarName());
		thePanel.add("Bottom", dataXAxis);
		
			dataYAxis = new VertAxis(this);
			dataYAxis.readNumLabels(data.getYAxisInfo());
		thePanel.add("Left", dataYAxis);
		
			dataView = new LSScatterView(data, this, dataXAxis, dataYAxis, "x", "y", "ls");
			dataView.lockBackground(Color.white);
		thePanel.add("Center", dataView);
		return thePanel;
	}
	
	private XPanel residPlot(SimpleRegnDataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			residXAxis = new HorizAxis(this);
			residXAxis.readNumLabels(data.getXAxisInfo());
			residXAxis.setAxisName(data.getXVarName());
		thePanel.add("Bottom", residXAxis);
		
			vertResidAxis = new MultiVertAxis(this, 2);
			vertResidAxis.readNumLabels(getParameter(RESID_AXIS_PARAM));
			vertResidAxis.readExtraNumLabels(stdResidScale(data, vertResidAxis));
			vertResidAxis.setChangeMinMax(true);
		thePanel.add("Left", vertResidAxis);
		
			residView = new LSScatterView(data, this, residXAxis, vertResidAxis, "x", "resid", "zero");
			residView.lockBackground(Color.white);
		thePanel.add("Center", residView);
		return thePanel;
	}
	
	private String stdResidScale(SimpleRegnDataSet data, MultiVertAxis residAxis) {
		LinearModel ls = (LinearModel)data.getVariable("ls");
		NumVariable xVar = (NumVariable)data.getVariable("x");
		int n = xVar.noOfValues();
		
		double sd = ls.evaluateSD().toDouble();
		
		double maxResid = residAxis.maxOnAxis;
		double maxStdResid = maxResid / sd / Math.sqrt(1.0 - 2.0 / n);
		
		String maxStdResidString = new NumValue(maxStdResid, 5).toString();
		String resultString;
		if (maxStdResid < 2.0)
			resultString = "-" + maxStdResidString + " " + maxStdResidString + " -1 0.5";
		else if (maxStdResid < 3.0)
			resultString = "-" + maxStdResidString + " " + maxStdResidString + " -2 1";
		else if (maxStdResid < 4.0)
			resultString = "-" + maxStdResidString + " " + maxStdResidString + " -3 1";
		else
			resultString = "-" + maxStdResidString + " " + maxStdResidString + " -4 2";
		
		return resultString;
	}
	
	private void changeDisplaysForNewData(SimpleRegnDataSet data, int dataSetIndex) {
		dataDescription.setText(dataSetIndex);
		
		dataXAxis.readNumLabels(data.getXAxisInfo());
		dataXAxis.setAxisName(data.getXVarName());
		dataXAxis.repaint();
		residXAxis.readNumLabels(data.getXAxisInfo());
		residXAxis.setAxisName(data.getXVarName());
		residXAxis.repaint();
		dataYAxis.readNumLabels(data.getYAxisInfo());
		dataYAxis.repaint();
		yNameLabel.setText(data.getYVarName());
		
		vertResidAxis.resetLabels();
			String residAxisParam = RESID_AXIS_PARAM;
			if (dataSetIndex > 0)
				residAxisParam += (dataSetIndex + 1);
		vertResidAxis.readNumLabels(getParameter(residAxisParam));
		vertResidAxis.readExtraNumLabels(stdResidScale(data, vertResidAxis));
		if (!vertResidAxis.setAlternateLabels(0))
			vertResidAxis.repaint();
		
		residTypeChoice.select(0);
		currentResidType = 0;
		residView.changeVariables("resid", "x");
	}

	
	private boolean localAction(Object target) {
		if (target == dataSetChoice) {
			int newDataIndex = dataSetChoice.getSelectedIndex();
			if (data.changeDataSet(newDataIndex)) {
				changeDisplaysForNewData(data, newDataIndex);
				data.variableChanged("y");
			}
			return true;
		}
		else if (target == residTypeChoice) {
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

