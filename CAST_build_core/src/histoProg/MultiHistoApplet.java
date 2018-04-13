package histoProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import histo.*;
import dotPlot.StackingDotPlotView;


public class MultiHistoApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String GROUP_NAME_PARAM = "groupNames";
	static final private String CLASS_INFO_PARAM = "classInfo";
	
	static final private int HISTO = 0;
	static final private int JITTERED = 1;
//	static final private int STACKED = 2;
	
	private String groupName[];
	private DataSet data[];
	private int noOfGroups;
	private PlotInfo displayInfo[];
	private XChoice displayChoice;
	private int displayType = HISTO;
	
	public void setupApplet() {
		readData();
		
		setLayout(new BorderLayout());
		
		add("Center", createDataPanel());
		add("South", createControlPanel());
	}
	
	private void readData() {
		String groupNames = getParameter(GROUP_NAME_PARAM);
		LabelEnumeration le = new LabelEnumeration(groupNames);
		noOfGroups = 0;
		while (le.hasMoreElements()) {
			noOfGroups++;
			le.nextElement();
		}
		
		groupName = new String[noOfGroups];
		data = new DataSet[noOfGroups];
		
		le = new LabelEnumeration(groupNames);
		for (int i=0 ; i<noOfGroups ; i++) {
			groupName[i] = (String)le.nextElement();
			data[i] = new DataSet();
			data[i].addNumVariable("y", groupName[i], getParameter(groupName[i]));
		}
	}
	
	private XPanel createControlPanel() {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		displayChoice = new XChoice(this);
		displayChoice.addItem("Histograms");
		displayChoice.addItem("Jittered dot plots");
		displayChoice.addItem("Stacked dot plots");
		displayChoice.select(0);
		
		controlPanel.add(displayChoice);
		
		return controlPanel;
	}
	
	private XPanel createDataPanel() {
		XPanel dataPanel = new XPanel();
		MultiHistoLayout theLayout = new MultiHistoLayout(5);
		dataPanel.setLayout(theLayout);
		
		displayInfo = new PlotInfo[noOfGroups];
		String groupAxisParam = axisMinMax();
		String classInfo = getParameter(CLASS_INFO_PARAM);
		StringTokenizer theParams = new StringTokenizer(classInfo);
		double class0Start = Double.parseDouble(theParams.nextToken());
		double classWidth = Double.parseDouble(theParams.nextToken());
		double maxDensity = Double.parseDouble(theParams.nextToken());
		
		for (int i=0 ; i<noOfGroups ; i++) {
			HorizAxis theGroupAxis = new HorizAxis(this);
			theGroupAxis.readNumLabels(groupAxisParam);
			theGroupAxis.setAxisName(groupName[i]);
			dataPanel.add(theGroupAxis);
			
			DensityAxis theDensityAxis = new DensityAxis(DensityAxis.NO_LABELS, maxDensity,
									classWidth, data[i].getNumVariable().noOfValues(), this);
			dataPanel.add("Left", theDensityAxis);
			
			HistoView theHisto = new HistoView(data[i], this, theGroupAxis, theDensityAxis, class0Start,
																																											classWidth);
			theHisto.setBarType(HistoView.VERT_BARS);
			theHisto.lockBackground(Color.white);
			dataPanel.add(theHisto);
			
			StackingDotPlotView theDotPlot = new StackingDotPlotView(data[i], this, theGroupAxis);
			theDotPlot.lockBackground(Color.white);
			theDotPlot.setViewBorder(new Insets(5, 5, 3, 5));
			dataPanel.add(theDotPlot);
			
			displayInfo[i] = new PlotInfo(theGroupAxis, theDensityAxis, theHisto, theDotPlot,
																							PlotInfo.SHOW_HISTO);
		}
		
		HorizAxis mainAxis = new HorizAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		mainAxis.readNumLabels(labelInfo);
		dataPanel.add(mainAxis);
		
		theLayout.setPlotInfo(mainAxis, displayInfo);
		
		return dataPanel;
	}
	
	private String axisMinMax() {
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		StringTokenizer st = new StringTokenizer(labelInfo);
		return st.nextToken() + " " + st.nextToken();		//		first 2 values are min and max
	}
	
	private boolean localAction(Object target) {
		if (target == displayChoice) {
			int newType = displayChoice.getSelectedIndex();
			if (newType != displayType) {
				for (int i=0 ; i<displayInfo.length ; i++) {
					if (newType == HISTO)
						displayInfo[i].changeDisplayType(PlotInfo.SHOW_HISTO);
					else {
						if (newType == JITTERED)
							displayInfo[i].theDotPlot.reset();
						else
							displayInfo[i].theDotPlot.setFinalFrame();
						if (displayType == HISTO)
							displayInfo[i].changeDisplayType(PlotInfo.SHOW_DOTS);
					}
				}
				displayType = newType;
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