package dynamicProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;

import dynamic.*;


public class HistogramTimeApplet extends XApplet {
	static final private String FREQ_AXIS_INFO_PARAM = "freqAxis";
	static final private String X_AXIS_INFO_PARAM = "xAxis";
	static final private String CLASS_BOUNDARY_PARAM = "classBoundary";
	static final private String UNIT_CLASS_WIDTH_PARAM = "unitClassWidth";
	static final private String X_AXIS_NAME_PARAM = "xAxisName";
	static final private String DENSITY_AXIS_NAME_PARAM = "densityAxisName";
	
	static final private String YEARS_PARAM = "years";
	static final private String YEAR_LABELS_PARAM = "yearLabels";
	
	
	private DataSet data;
	
	private int startYear, endYear, yearStep;
	private YearSlider yearSlider;
	
	
	public void setupApplet() {
		data = readData();
		readYearInfo();
		
		setLayout(new BorderLayout(0, 0));
		
		add("Center", displayPanel(data));
		
		add("North", topPanel(data));
		
		add("South", controlPanel(data));
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
			NumSeriesVariable yVar = new NumSeriesVariable(getParameter(VAR_NAME_PARAM));
			yVar.readValues(getParameter(VALUES_PARAM));
		
		data.addVariable("y", yVar);
		
		return data;
	}
	
	private void readYearInfo() {
		StringTokenizer st = new StringTokenizer(getParameter(YEARS_PARAM));
		startYear = Integer.parseInt(st.nextToken());
		endYear = Integer.parseInt(st.nextToken());
		yearStep = Integer.parseInt(st.nextToken());
	}
	
	private XPanel topPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		XLabel axisLabel = new XLabel(getParameter(DENSITY_AXIS_NAME_PARAM), XLabel.LEFT, this);
		axisLabel.setFont(getStandardFont());
		thePanel.add(axisLabel);
		return thePanel;
	}
	
	protected HistogramView getHistogram(DataSet data, HorizAxis theHorizAxis, VertAxis theVertAxis,
												double[] classBoundary, double unitClassWidth, boolean[] groupWithNext) {
		return new HistogramView(data, this, "y", theHorizAxis, theVertAxis,
																					unitClassWidth, classBoundary, groupWithNext);
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			theHorizAxis.readNumLabels(getParameter(X_AXIS_INFO_PARAM));
			theHorizAxis.setAxisName(getParameter(X_AXIS_NAME_PARAM));
		thePanel.add("Bottom", theHorizAxis);
		
			VertAxis theVertAxis = new VertAxis(this);
			theVertAxis.readNumLabels(getParameter(FREQ_AXIS_INFO_PARAM));
		thePanel.add("Left", theVertAxis);
		
			StringTokenizer st = new StringTokenizer(getParameter(CLASS_BOUNDARY_PARAM));
			double classBoundary[] = new double[st.countTokens()];
			for (int i=0 ; i<classBoundary.length ; i++)
				classBoundary[i] = Double.parseDouble(st.nextToken());
				
			double unitClassWidth = Double.parseDouble(getParameter(UNIT_CLASS_WIDTH_PARAM));
			
			boolean groupWithNext[] = new boolean[classBoundary.length - 1];
			
			HistogramView theView = getHistogram(data, theHorizAxis, theVertAxis, classBoundary,
																								unitClassWidth, groupWithNext);
			theView.setCorrectHeights(true);
			theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 4));
		
			StringTokenizer st = new StringTokenizer(getParameter(YEAR_LABELS_PARAM));
			int startYearLabel = Integer.parseInt(st.nextToken());
			int labelStep = Integer.parseInt(st.nextToken());
		
			yearSlider = new YearSlider(translate("Year"), startYear, endYear, startYear, yearStep,
																															startYearLabel, labelStep, this);
			
		thePanel.add(yearSlider);
		
		return thePanel;
	}
	
	private void yearIndexChange() {
		double yearIndex = (yearSlider.getYear() - startYear) / (double)yearStep;
		
		NumSeriesVariable yVar = (NumSeriesVariable)data.getVariable("y");
		yVar.setSeriesIndex(yearIndex);
	}
	
	private boolean localAction(Object target) {
		if (target == yearSlider) {
			yearIndexChange();
			data.variableChanged("y");
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}