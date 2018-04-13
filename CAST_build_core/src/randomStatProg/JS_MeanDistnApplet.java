package randomStatProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import distn.*;
import coreGraphics.*;


public class JS_MeanDistnApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String MEAN_NAME_PARAM = "meanName";
	
	static final protected Color kPopnColor = new Color(0x990000);
	static final private Color kSampColor = new Color(0x0000BB);
	static final protected Color kSummaryColor = Color.black;
	
	static final private Color kNormalColor = new Color(0xE2BBE6);
	
	protected DataSet data;
	protected SummaryDataSet summaryData;
	
	public void setupApplet() {
		data = getData();
		summaryData = getSummaryData(data);
		setTheoryParameters(data, summaryData);
		
		setLayout(new ProportionLayout(0.7, 12, ProportionLayout.VERTICAL,
																												ProportionLayout.TOTAL));
			XPanel topTwoPanel = new XPanel();
			topTwoPanel.setLayout(new ProportionLayout(0.5, 12, ProportionLayout.VERTICAL,
																												ProportionLayout.TOTAL));
			
			topTwoPanel.add(ProportionLayout.TOP, dataPanel(data, "y", kSampColor));
		
			topTwoPanel.add(ProportionLayout.BOTTOM, bestPopnDistnPanel(data, "bestModel", kPopnColor));
		
		add(ProportionLayout.TOP, topTwoPanel);
			
		add(ProportionLayout.BOTTOM, bestMeanDistnPanel(summaryData, "bestTheory", kSummaryColor));
	}
	
	private void setTheoryParameters(DataSet data, SummaryDataSet summaryData) {
		NumVariable yVar = (NumVariable)data.getVariable("y");
		ValueEnumeration ye = yVar.values();
		double sy = 0.0;
		double syy = 0.0;
		int n = 0;
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			sy += y;
			syy += y * y;
			n ++;
		}
		
		double mean = sy / n;
		double sd = Math.sqrt((syy - sy * mean) / (n - 1));
		double se = sd / Math.sqrt(n);
		
		NormalDistnVariable bestPopnDistn = (NormalDistnVariable)data.getVariable("bestModel");
		bestPopnDistn.setMean(mean);
		bestPopnDistn.setSD(sd);
		
		NormalDistnVariable bestMeanDistn = (NormalDistnVariable)summaryData.getVariable("bestTheory");
		bestMeanDistn.setMean(mean);
		bestMeanDistn.setSD(se);
		
		System.out.println("n = " + n);
		System.out.println("xBar = " + mean);
		System.out.println("s = " + sd);
		System.out.println("se = " + se);
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		
			NormalDistnVariable popnDistn = new NormalDistnVariable(getParameter(VAR_NAME_PARAM));
		data.addVariable("bestModel", popnDistn);
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "y");
		
			NormalDistnVariable bestTheoryDistn = new NormalDistnVariable(getParameter(MEAN_NAME_PARAM));
		summaryData.addVariable("bestTheory", bestTheoryDistn);
		
		return summaryData;
	}
	
	private HorizAxis getAxis(DataSet data, String variableKey) {
		HorizAxis theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		CoreVariable v = data.getVariable(variableKey);
		theHorizAxis.setAxisName(v.name);
		return theHorizAxis;
	}
	
	private XPanel bestPopnDistnPanel(DataSet data, String modelKey, Color c) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = getAxis(data, modelKey);
			horizAxis.setForeground(c);
		thePanel.add("Bottom", horizAxis);
		
			SimpleDistnView view = new SimpleDistnView(data, this, horizAxis, modelKey);
			view.setDensityScaling(0.9);
			view.setDensityColor(kNormalColor);
			view.lockBackground(Color.white);
			view.setForeground(c);
		thePanel.add("Center", view);
		
		return thePanel;
	}
	
	private XPanel dataPanel(DataSet data, String dataKey, Color c) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = getAxis(data, dataKey);
			horizAxis.setForeground(c);
		thePanel.add("Bottom", horizAxis);
		
			StackedDotPlotView view = new StackedDotPlotView(data, this, horizAxis);
			view.setActiveNumVariable(dataKey);
			view.lockBackground(Color.white);
			view.setForeground(c);
		thePanel.add("Center", view);
		
		return thePanel;
	}
	
	private XPanel bestMeanDistnPanel(SummaryDataSet summaryData, String modelKey, Color c) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = getAxis(summaryData, modelKey);
			horizAxis.setForeground(c);
		thePanel.add("Bottom", horizAxis);
		
			SimpleDistnView view = new SimpleDistnView(summaryData, this, horizAxis, modelKey);
			view.setDensityScaling(0.9);
			view.lockBackground(Color.white);
			view.setForeground(c);
		thePanel.add("Center", view);
		
		return thePanel;
	}
}