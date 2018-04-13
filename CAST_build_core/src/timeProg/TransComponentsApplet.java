package timeProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import valueList.*;
import coreVariables.*;

import transform.*;
import time.*;


public class TransComponentsApplet extends ComponentsApplet {
	static final private String LOG_AXIS_INFO_PARAM = "logAxis";
	static final private String MODEL_TYPE_PARAM = "modelType";
	
	private boolean multiplicative;
	
	private XNoValueSlider transformSlider;
	
	protected VertAxis rawVertAxis;
	protected Log10VertAxis logVertAxis;
	
	public void setupApplet() {
		multiplicative = getParameter(MODEL_TYPE_PARAM).equals("multiplicative");
		
		data = readData();
		
		setLayout(new BorderLayout(0, 20));
		
			String fitKey = multiplicative ? "fit" : "bottomComponents";
		add("Center", timeSeriesPanel(data, "y", fitKey));
		
			XPanel controlPanel = new XPanel();
			controlPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 5));
			
			controlPanel.add(componentCheckPanel());
			controlPanel.add(fitPredictPanel(data));
			
		add("South", controlPanel);
		
		if (multiplicative) {
			rawVertAxis.setPowerIndex(200);
			logVertAxis.setPowerIndex(200);
		}
	}
	
	protected DataSet readRawData() {
		DataSet data = super.readRawData();
		
		if (multiplicative) {
//			NumVariable yVar = (NumVariable)data.getVariable("y");
			data.addVariable("yLog", new LogVariable("Log y", data, "y", 3));
		}
		return data;
	}
	
	protected String getYSourceKey() {
		return multiplicative ? "yLog" : "y";
	}
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		if (multiplicative) {
			LogVariable transFit = new LogVariable("Fit", data, "bottomComponents", decimals);
			transFit.setInverse();
			data.addVariable("fit", transFit);
		}
		return data;
	}
	
	protected XPanel addTitle(XPanel normalPanel, DataSet data, String yKey) {
		String title = data.getVariable(yKey).name;
		
		XPanel superPanel = new XPanel();
		superPanel.setLayout(new BorderLayout());
			
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new BorderLayout());
			
				XLabel rawLabel = new XLabel(title, XLabel.LEFT, this);
			topPanel.add("West", rawLabel);
			
				XLabel logLabel = new XLabel(translate("log") + "(" + title + ")", XLabel.RIGHT, this);
			topPanel.add("East", logLabel);
			topPanel.add("Center", new XPanel());
			
		superPanel.add("North", topPanel);
		superPanel.add("Center", normalPanel);
		
		return superPanel;
	}
	
	private XPanel sliderPanel() {
		XPanel thePanel = new XPanel();
		if (multiplicative) {
			thePanel.setLayout(new ProportionLayout(0.8, 0, ProportionLayout.HORIZONTAL,
																																		ProportionLayout.TOTAL));
				XPanel leftPanel = new InsetPanel(0, 0, 0, 10);
				leftPanel.setLayout(new ProportionLayout(0.25, 0, ProportionLayout.HORIZONTAL,
																																		ProportionLayout.TOTAL));
				leftPanel.add("Left", new XPanel());
			
					transformSlider = new XNoValueSlider(translate("Original scale"), translate("Log scale"),
																						null, 0, 100, (multiplicative ? 100 : 0), this);
				leftPanel.add("Right", transformSlider);
			
			thePanel.add("Left", leftPanel);
			thePanel.add("Right", new XPanel());
		}
		return thePanel;
	}
	
	protected XPanel componentCheckPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 10));
		thePanel.add(coreCheckPanel());
		thePanel.add(sliderPanel());
		return thePanel;
	}
	
	protected XPanel fitPredictPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
		
		thePanel.add(new SeasonYearValueView(data, this, oneTimeAxis, "Quarter"));
		
		thePanel.add(new OneValueView(data, "y", this));
		
		thePanel.add(new FitPredictValueView(data, (multiplicative ? "fit" : "bottomComponents"),
																			"y", this, translate("Fitted value"), translate("Prediction")));
		
		return thePanel;
	}
	
	protected VertAxis getVertAxis() {
		rawVertAxis = super.getVertAxis();
		return rawVertAxis;
	}
	
	protected VertAxis getVertAxis2() {
		logVertAxis = new Log10VertAxis(this, Log10VertAxis.SHOW_LOGS);
		String labelInfo = getParameter(LOG_AXIS_INFO_PARAM);
		logVertAxis.readLogLabels(labelInfo);
		return logVertAxis;
	}

	
	private boolean localAction(Object target) {
		if (target == transformSlider) {
			int newTransformIndex = 300 - transformSlider.getValue();
			rawVertAxis.setPowerIndex(newTransformIndex);
			logVertAxis.setPowerIndex(newTransformIndex);
			data.transformedAxis(rawVertAxis);
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