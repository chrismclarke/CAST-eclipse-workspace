package histoProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import histo.*;


public class VariableClass2Applet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String CLASS_INFO_PARAM = "classInfo";
	static final protected String INIT_GROUPING_PARAM = "groupingInfo";
	static final protected String HIGHLIGHT_PARAM = "highlight";
	static final private String DENSITY_AXIS_INFO_PARAM = "densityAxis";
	static final private String COUNT_AXIS_INFO_PARAM = "countAxis";
	static final private String PROPN_AXIS_INFO_PARAM = "propnAxis";
	static final private String LINES_PARAM = "lines";
	static final private String AXIS_PARAM = "axisType";
	
	protected VariableClassHistoView theHisto;
	protected DensityAxis2 theDensityAxis;
	protected XLabel densityAxisNameLabel;
	
	public void setupApplet() {
		DataSet data = getData();
		
		setLayout(new BorderLayout());
			densityAxisNameLabel = new XLabel("", XLabel.LEFT, this);
			densityAxisNameLabel.setFont(getStandardBoldFont());
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
		String axisString = getParameter(AXIS_PARAM);
		int labelType =
				(axisString == null || axisString.equals("none")) ? DensityAxis2.NO_LABELS
				: axisString.equals("density") ? DensityAxis2.DENSITY_LABELS
				: axisString.equals("frequency") ? DensityAxis2.COUNT_LABELS
				: DensityAxis2.REL_FREQ_LABELS;
		return labelType;
	}
	
	protected int initialHistoLines() {
		String linesString = getParameter(LINES_PARAM);
		int linesType =
				(linesString == null || linesString.equals("none")) ? HistoView.NO_BARS
				: linesString.equals("vert") ? HistoView.VERT_BARS
				: HistoView.BOTH_BARS;
		return linesType;
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
										DensityAxis2 densityAxis, double class0Start, double classWidth) {
		return new VariableClassHistoView(data, this, theHorizAxis, densityAxis, class0Start, classWidth,
																																		getHighlightLimits());
	}
	
	private XPanel createHisto(DataSet data) {
		XPanel histoPanel = new XPanel();
		histoPanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		theHorizAxis.setAxisName(data.getVariable("y").name);
		histoPanel.add("Bottom", theHorizAxis);
		
		String densityAxisInfo = getParameter(DENSITY_AXIS_INFO_PARAM);
		String countAxisInfo = getParameter(COUNT_AXIS_INFO_PARAM);
		String propnAxisInfo = getParameter(PROPN_AXIS_INFO_PARAM);
		theDensityAxis = new DensityAxis2(initialDensityAxisLabel(), densityAxisInfo,
																		countAxisInfo, propnAxisInfo, this);
		histoPanel.add("Left", theDensityAxis);
		
		String classInfo = getParameter(CLASS_INFO_PARAM);
		StringTokenizer theParams = new StringTokenizer(classInfo);
		double class0Start = Double.parseDouble(theParams.nextToken());
		double classWidth = Double.parseDouble(theParams.nextToken());
		
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