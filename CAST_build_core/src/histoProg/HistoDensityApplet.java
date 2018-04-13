package histoProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import histo.*;
import valueList.OneValueView;


public class HistoDensityApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String CLASS_INFO_PARAM = "classInfo";
	
	private HistoView theHisto;
	private DensityAxis theDensityAxis;
	private XChoice axisTypeChoice;
	private XLabel densityAxisNameLabel;
	
	public void setupApplet() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		
		setLayout(new BorderLayout());
		densityAxisNameLabel = new XLabel("", XLabel.LEFT, this);
		densityAxisNameLabel.setFont(getSmallBoldFont());
		add("North", densityAxisNameLabel);
		add("Center", createHisto(data));
		add("South", createControls(data));
		densityAxisNameLabel.setText(theDensityAxis.getAxisName());
	}
	
	private XPanel createHisto(DataSet data) {
		XPanel histoPanel = new XPanel();
		histoPanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		histoPanel.add("Bottom", theHorizAxis);
		
		String classInfo = getParameter(CLASS_INFO_PARAM);
		StringTokenizer theParams = new StringTokenizer(classInfo);
		double class0Start = Double.parseDouble(theParams.nextToken());
		double classWidth = Double.parseDouble(theParams.nextToken());
		double maxDensity = Double.parseDouble(theParams.nextToken());
		
		theDensityAxis = new DensityAxis(DensityAxis.COUNT_LABELS, maxDensity, classWidth,
																	data.getNumVariable().noOfValues(), this);
		histoPanel.add("Left", theDensityAxis);
		
		theHisto = new HistoView(data, this, theHorizAxis, theDensityAxis, class0Start, classWidth);
		theHisto.setBarType(HistoView.VERT_BARS);
		histoPanel.add("Center", theHisto);
		theHisto.lockBackground(Color.white);
		
		return histoPanel;
	}
	
	private XPanel createControls(DataSet data) {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		
		axisTypeChoice = new XChoice(this);
		axisTypeChoice.addItem(translate("Frequency"));
		axisTypeChoice.addItem("Relative Freq");
		axisTypeChoice.addItem(translate("Density"));
		axisTypeChoice.addItem("No labels");
		axisTypeChoice.select(0);
		controlPanel.add(axisTypeChoice);
		
		OneValueView theValue = new OneValueView(data, "y",this);
		controlPanel.add(theValue);
		
		return controlPanel;
	}
	
	private boolean localAction(Object target) {
		if (target == axisTypeChoice) {
			theDensityAxis.changeLabelType(axisTypeChoice.getSelectedIndex());
			densityAxisNameLabel.setText(theDensityAxis.getAxisName());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}