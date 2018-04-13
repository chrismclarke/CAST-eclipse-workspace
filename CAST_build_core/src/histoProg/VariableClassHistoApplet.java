package histoProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import histo.*;


public class VariableClassHistoApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String CLASS_INFO_PARAM = "classInfo";
	static final protected String INIT_GROUPING_PARAM = "groupingInfo";
	static final protected String HIGHLIGHT_PARAM = "highlight";
	
	protected VariableClassHistoView theHisto;
	protected DensityAxis theDensityAxis;
	private XLabel densityAxisNameLabel;
	
	public void setupApplet() {
		DataSet data = getData();
		
		setLayout(new BorderLayout());
		densityAxisNameLabel = new XLabel("", XLabel.LEFT, this);
		densityAxisNameLabel.setFont(getSmallBoldFont());
		add("North", densityAxisNameLabel);
		add("Center", createHisto(data));
		add("South", createControls(data));
		densityAxisNameLabel.setText(theDensityAxis.getAxisName());
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		return data;
	}
	
	protected int initialDensityAxisLabel() {
		return DensityAxis.COUNT_LABELS;
	}
	
	protected int initialHistoLines() {
		return HistoView.VERT_BARS;
	}
	
	protected HighlightLimits getHighlightLimits() {
		String hiliteString = getParameter(HIGHLIGHT_PARAM);
		read: if (hiliteString != null) {
			StringTokenizer theHilites = new StringTokenizer(hiliteString);
			try {
				if (!theHilites.hasMoreTokens())
					break read;
				double min = Double.parseDouble(theHilites.nextToken());
				if (!theHilites.hasMoreTokens())
					break read;
				double max = Double.parseDouble(theHilites.nextToken());
				if (min < max)
					return new HighlightLimits(min, max);
			} catch (NumberFormatException e) {
				System.err.println("Bad hiliteLimit");
			}
		}
		return null;
	}
	
	protected VariableClassHistoView createHistoView(DataSet data, HorizAxis theHorizAxis,
										DensityAxis densityAxis, double class0Start, double classWidth) {
		return new VariableClassHistoView(data, this, theHorizAxis, densityAxis, class0Start, classWidth,
																																	getHighlightLimits());
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
		
		theDensityAxis = new DensityAxis(initialDensityAxisLabel(), maxDensity,
														classWidth, data.getNumVariable().noOfValues(), this);
		histoPanel.add("Left", theDensityAxis);
		
		theHisto = createHistoView(data, theHorizAxis, theDensityAxis, class0Start, classWidth);
		String groupingInfo = getParameter(INIT_GROUPING_PARAM);
		theHisto.setGrouping(groupingInfo);
		theHisto.setBarType(initialHistoLines());
		histoPanel.add("Center", theHisto);
		theHisto.lockBackground(Color.white);
		
		return histoPanel;
	}
	
	protected XPanel createControls(DataSet data) {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 2));
		
		return controlPanel;
	}
}