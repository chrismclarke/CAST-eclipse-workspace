package statisticProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import imageUtils.*;

import statistic.*;


public class DragCrossDevnApplet extends XApplet {
	static final protected String AXIS_INFO_PARAM = "horizAxis";
	static final protected String TARGET_PARAM = "target";
	static final private String TARGET_NAME_PARAM = "targetName";
	static final protected String MAX_SUMMARY_PARAM = "maxSummary";
	
	protected DataSet data;
	protected DragCrossView theView;
	protected DeviationsView summaryValue;
	
	private XChoice devnTypeChoice;
	private int currentDevnType;
	private XButton resetButton;
	private ImageSwapCanvas summaryImage;
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		return data;
	}
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout());
		add("Center", displayPanel(data));
		add("South", controlPanel(data));
		add("East", summaryPanel(data));
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			String labelInfo = getParameter(AXIS_INFO_PARAM);
			theHorizAxis.readNumLabels(labelInfo);
		thePanel.add("Bottom", theHorizAxis);
		
			double target = Double.parseDouble(getParameter(TARGET_PARAM));
			NumValue maxSummary = new NumValue(getParameter(MAX_SUMMARY_PARAM));
		
			theView = new DragCrossView(data, this, theHorizAxis, DragCrossView.ABS_DEVN, target,
																		getParameter(TARGET_NAME_PARAM), maxSummary.decimals);
			theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
		devnTypeChoice = new XChoice(this);
		devnTypeChoice.addItem("Mean absolute error");
		devnTypeChoice.addItem("Mean square error");
		devnTypeChoice.addItem("Root mean sqr error");
		devnTypeChoice.select(0);
		currentDevnType = 0;
		controlPanel.add(devnTypeChoice);
		
		controlPanel.add(buttonPanel(data));
		
		return controlPanel;
	}
	
	protected XPanel buttonPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 4));
		
		resetButton = new XButton(translate("Reset"), this);
		thePanel.add(resetButton);
		
		return thePanel;
	}
	
	protected XPanel summaryPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 4));
		
		
		String[] pictName = new String[3];
		pictName[0] = "statistics/meanAbsDevn.gif";
		pictName[1] = "statistics/meanSqrDevn.gif";
		pictName[2] = "statistics/rootMeanSqrDevn.gif";
		summaryImage = new ImageSwapCanvas(pictName, this, 91, 51);
		thePanel.add(summaryImage);
		
			NumValue maxValue = new NumValue(getParameter(MAX_SUMMARY_PARAM));
			summaryValue = new DeviationsView(data, this, "y", maxValue, theView);
			
		thePanel.add(summaryValue);
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == resetButton) {
			NumVariable y = (NumVariable)data.getVariable("y");
			y.readValues(getParameter(VALUES_PARAM));
			data.variableChanged("y");
			return true;
		}
		else if (target == devnTypeChoice) {
			if (devnTypeChoice.getSelectedIndex() != currentDevnType) {
				currentDevnType = devnTypeChoice.getSelectedIndex();
				theView.setDevnType(currentDevnType == 0 ? DragCrossView.ABS_DEVN
																		: DragCrossView.SQR_DEVN);
				summaryImage.showVersion(currentDevnType);
				
				summaryValue.setSummaryType(currentDevnType);
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