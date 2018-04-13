package transformProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import valueList.OneValueView;
import coreVariables.*;

import transform.*;


public class Log10Applet extends XApplet {
	static final private String DATASETS_PARAM = "dataSets";		//	assumes all datasets use same axes
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String LOG_AXIS_INFO_PARAM = "logAxis";
	static final private String LOG_NAME_PARAM = "logName";
	static final private String LOG_DECIMALS_PARAM = "logDecimals";
	static final private String JITTER_PARAM = "jittering";
	
	private NumVariable rawVar;
	private LogVariable logVar;
	private DataSet data;
	
	private HorizAxis standardAxis;
	private Log10HorizAxis unevenAxis, logAxis;
	
	private XNoValueSlider transformSlider;
	
	private XChoice dataSetChoice;
	private int currentDataSetIndex = 0;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout());
		add("North", topPanel(data));
		add("Center", createView(data));
		add("South", sliderPanel(data));
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
		rawVar = new NumVariable(getParameter(VAR_NAME_PARAM));
		rawVar.readValues(getParameter(VALUES_PARAM));
		data.addVariable("y", rawVar);
		
		int logDecimals = Integer.parseInt(getParameter(LOG_DECIMALS_PARAM));
		logVar = new LogVariable(getParameter(LOG_NAME_PARAM), data, "y", logDecimals);
		data.addVariable("log", logVar);
		
		String labelName = getParameter(LABEL_NAME_PARAM);
		if (labelName != null)
			data.addLabelVariable("label", labelName, getParameter(LABELS_PARAM));
		return data;
	}
	
	private XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
		
		CoreVariable label = data.getVariable("label");
		if (label != null)
			thePanel.add(new OneValueView(data, "label", this));
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
			
			topPanel.add(new OneValueView(data, "y", this));
			
			topPanel.add(new OneValueView(data, "log", this));
		thePanel.add(topPanel);
		
		String dataSetsString = getParameter(DATASETS_PARAM);
		if (dataSetsString != null) {
			StringTokenizer st = new StringTokenizer(dataSetsString, "#");
			dataSetChoice = new XChoice(this);
			while (st.hasMoreTokens())
				dataSetChoice.addItem(st.nextToken());
			
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new BorderLayout(0, 10));
				
				XPanel choicePanel = new XPanel();
				choicePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
				choicePanel.add(dataSetChoice);
				
			mainPanel.add("North", choicePanel);
				
			mainPanel.add("Center", thePanel);
			return mainPanel;
		}
		else
			return thePanel;
	}
	
	private XPanel sliderPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			transformSlider = new XNoValueSlider(translate("Original scale"), translate("Log scale"), null,
												0, 100, 0, this);
		
		thePanel.add("Center", transformSlider);
		
		return thePanel;
	}
	
	private XPanel createView(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		standardAxis = new HorizAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		standardAxis.readNumLabels(labelInfo);
		thePanel.add("Bottom", standardAxis);
		
		unevenAxis = new Log10HorizAxis(this, Log10HorizAxis.SHOW_RAW);
		unevenAxis.setAxisName(rawVar.name);
		labelInfo = getParameter(LOG_AXIS_INFO_PARAM);
		unevenAxis.readLogLabels(labelInfo);
		thePanel.add("Bottom", unevenAxis);
		
		logAxis = new Log10HorizAxis(this, Log10HorizAxis.SHOW_LOGS);
		logAxis.setAxisName(logVar.name);
		labelInfo = getParameter(LOG_AXIS_INFO_PARAM);
		logAxis.readLogLabels(labelInfo);
		thePanel.add("Top", logAxis);
		
		DataView theView = coreView(data, standardAxis);
		theView.setActiveNumVariable("y");
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		
		return thePanel;
	}
	
	protected DataView coreView(DataSet data, NumCatAxis theHorizAxis) {
		String jitterString = getParameter(JITTER_PARAM);
		double jittering = (jitterString == null) ? 1.0
																: Double.parseDouble(jitterString);
		return new TwinAxisDotPlotView(data, this, theHorizAxis, jittering);
	}

	
	private boolean localAction(Object target) {
		if (target == transformSlider) {
			int newTransformIndex = 300 - transformSlider.getValue();
			standardAxis.setPowerIndex(newTransformIndex);
			logAxis.setPowerIndex(newTransformIndex);
			unevenAxis.setPowerIndex(newTransformIndex);
			data.transformedAxis(standardAxis);
			return true;
		}
		else if (target == dataSetChoice) {
			int newChoice = dataSetChoice.getSelectedIndex();
			if (newChoice != currentDataSetIndex) {
				currentDataSetIndex = newChoice;
				
				NumVariable yVar = (NumVariable)data.getVariable("y");
				String suffix = (newChoice == 0) ? "" : String.valueOf(newChoice + 1);
				yVar.readValues(getParameter(VALUES_PARAM + suffix));
				
				LabelVariable labelVar = (LabelVariable)data.getVariable("label");
				if (labelVar != null)
					labelVar.readValues(getParameter(LABELS_PARAM + suffix));
				
				data.variableChanged("y");
				data.variableChanged("label");
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