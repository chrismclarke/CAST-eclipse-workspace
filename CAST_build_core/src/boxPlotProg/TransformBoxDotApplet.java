package boxPlotProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import coreVariables.*;
import random.RandomRectangular;

import boxPlot.*;


public class TransformBoxDotApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String RANDOM_NORMAL_PARAM = "random";
	static final private String OUTLIERS_PARAM = "showOutliers";
	
	private XNoValueSlider kurtosisSlider, skewnessSlider, spreadSlider, shiftSlider;
	
	private DataSet data;
	private SimpleBoxDotView theView;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout(0, 30));
		add("Center", createView(data));
		add("South", createControls(data));
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		String randomInfo = getParameter(RANDOM_NORMAL_PARAM);
		RandomRectangular generator = new RandomRectangular(randomInfo);
		double vals[] = generator.generate();
		data.addNumVariable("raw", "raw data", vals);
		
		data.addVariable("y", new ShapeChangeVariable(getParameter(VAR_NAME_PARAM), data, "raw"));
		
		return data;
	}
	
	private XPanel createView(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		thePanel.add("Bottom", theHorizAxis);
		
		theView = new SimpleBoxDotView(data, this, theHorizAxis);
		theView.setActiveNumVariable("y");
		String outliersString = getParameter(OUTLIERS_PARAM);
		if (outliersString != null)
			theView.setShowOutliers(outliersString.equals("true"));
		theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	private XPanel createControls(DataSet data) {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new ProportionLayout(0.5, 5, ProportionLayout.HORIZONTAL,
																							ProportionLayout.TOTAL));
		
		XPanel leftPanel = new XPanel();
		leftPanel.setLayout(new BorderLayout());
		
		shiftSlider = new XNoValueSlider(null, null, translate("Centre"), -50, 50, 0, this);
		leftPanel.add("North", shiftSlider);
		spreadSlider = new XNoValueSlider(null, null, translate("Spread"), 0, 100, 100, this);
		leftPanel.add("South", spreadSlider);
		
		controlPanel.add(ProportionLayout.LEFT, leftPanel);
		
		XPanel rightPanel = new XPanel();
		rightPanel.setLayout(new BorderLayout());
		
		skewnessSlider = new XNoValueSlider(null, null, translate("Skewness"), -50, 50, 0, this);
		rightPanel.add("North", skewnessSlider);
		kurtosisSlider = new XNoValueSlider(null, null, translate("Tails"), 0, 100, 0, this);
		rightPanel.add("South", kurtosisSlider);
		
		controlPanel.add(ProportionLayout.RIGHT, rightPanel);
		
		ShapeChangeVariable transVar = (ShapeChangeVariable)data.getVariable("y");
		transVar.setSliders(kurtosisSlider, skewnessSlider, spreadSlider, shiftSlider);
		
		return controlPanel;
	}
	
	private boolean localAction(Object target) {
		if (target == shiftSlider || target == spreadSlider
				|| target == skewnessSlider || target == kurtosisSlider) {
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