package statisticProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;

import statistic.*;


public class DragCrossRmseApplet extends XApplet {
	static final protected String AXIS_INFO_PARAM = "horizAxis";
	static final private String TARGET_PARAM = "target";
	static final protected String TARGET_NAME_PARAM = "targetName";
	static final protected String MAX_SUMMARY_PARAM = "maxSummary";
	
	static final protected Color kSummaryColor = Color.red;
	
	protected DataSet data;
	
	protected DragCrossView theView;
	protected SsqDeviationsView summaryValue;
	
	protected double target;
	protected boolean keepTargetToMean = true;
	protected NumValue maxSummary;
	
	public void setupApplet() {
		data = getData();
		readTarget();
		
		setLayout(new BorderLayout());
		add("Center", displayPanel(data));
		add("South", valuePanel(data));
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		return data;
	}
	
	private void readTarget() {
		target = 0.0;
		String targetString = getParameter(TARGET_PARAM);
		if (targetString != null) {
			target = Double.parseDouble(targetString);
			keepTargetToMean = false;
		}
		
		maxSummary = new NumValue(getParameter(MAX_SUMMARY_PARAM));
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			String labelInfo = getParameter(AXIS_INFO_PARAM);
			theHorizAxis.readNumLabels(labelInfo);
			theHorizAxis.setAxisName(data.getVariable("y").name);
			
		thePanel.add("Bottom", theHorizAxis);
		
			theView = new DragCrossView(data, this, theHorizAxis, DragCrossView.SQR_DEVN, target,
																	getParameter(TARGET_NAME_PARAM), maxSummary.decimals);
			theView.setKeepTargetToMean(keepTargetToMean);
			theView.setCrossSize(DataView.LARGE_CROSS);
			theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected LayoutManager getSummaryLayout() {
		return new FlowLayout(FlowLayout.CENTER, 20, 0);
	}
	
	protected XPanel valuePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(getSummaryLayout());
			
			XPanel labelPanel = new XPanel();
			labelPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				String labelString = keepTargetToMean ? (translate("Standard deviation") + ":")
																												: (translate("Root mean square error") + ":");
				XLabel label = new XLabel(labelString, XLabel.LEFT, this);
				label.setFont(getStandardBoldFont());
//				label.setForeground(kSummaryColor);
			labelPanel.add(label);
		thePanel.add(labelPanel);	
			
			NumValue maxValue = new NumValue(getParameter(MAX_SUMMARY_PARAM));
			String summaryImage = keepTargetToMean ? "xEquals/sdEqnRed.png" : "xEquals/rmseEqnRed.png";
			summaryValue = new SsqDeviationsView(data, this, summaryImage, 37, "y", maxValue, theView);
			summaryValue.setForeground(kSummaryColor);
		thePanel.add(summaryValue);
		
		return thePanel;
	}
}