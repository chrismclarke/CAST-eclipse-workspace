package dotPlotProg;

import java.awt.*;

import axis.*;
import dataView.*;
import coreGraphics.*;
import random.RandomNormal;

import utils.*;


public class DotJitterApplet extends XApplet {
	static final private String RANDOM_NORMAL_PARAM = "random";
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String HAS_CONTROLS_PARAM = "hasControls";
	
	private final static int kSliderMax = 100;
	
	private DotPlotView theDotPlot;
	private XButton newJitterButton;
	private XSlider jitterSlider;
	
	public void setupApplet() {
		DataSet data = getData();
		
		setLayout(new BorderLayout(0, 5));
		add("Center", dotPlotPanel(data));
		
		String controlsString = getParameter(HAS_CONTROLS_PARAM);
		if (controlsString != null && controlsString.equals("true"))
			add("South", controlPanel());
	}
	
	private DataSet getData() {
		String variableName = getParameter(VAR_NAME_PARAM);
		DataSet data = new DataSet();
		
		String randomInfo = getParameter(RANDOM_NORMAL_PARAM);
		if (randomInfo != null) {
			RandomNormal generator = new RandomNormal(randomInfo);
			double vals[] = generator.generate();
			
			data.addNumVariable("y", variableName, vals);
		}
		else
			data.addNumVariable("y", variableName, getParameter(VALUES_PARAM));
		
		return data;
	}
	
	private XPanel dotPlotPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			String labelInfo = getParameter(AXIS_INFO_PARAM);
			theHorizAxis.readNumLabels(labelInfo);
			theHorizAxis.setAxisName(getParameter(VAR_NAME_PARAM));
		thePanel.add("Bottom", theHorizAxis);
		
			theDotPlot = new DotPlotView(data, this, theHorizAxis);
		thePanel.add("Center", theDotPlot);
			theDotPlot.lockBackground(Color.white);
		
		return thePanel;
	}
	
	private XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(10, 0));
		
			XPanel buttonPanel = new XPanel();
				newJitterButton = new XButton(translate("Redo jittering"), this);
			buttonPanel.add(newJitterButton);
		thePanel.add("East", buttonPanel);
		
			jitterSlider = new XNoValueSlider(translate("none"), translate("max"), translate("Jittering"),
																																					0, kSliderMax, 0, this);
		thePanel.add("Center", jitterSlider);
		return thePanel;
	}
	
	public void setNewJittering() {
		theDotPlot.newRandomJittering();
	}
	
	public void setJitterAmount(double jitter) {
		theDotPlot.setJitter(jitter);
	}
	
	private boolean localAction(Object target) {
		if (target == newJitterButton) {
			setNewJittering();
			return true;
		}
		else if (target == jitterSlider) {
			int value = jitterSlider.getValue();
			setJitterAmount(((double)value) / kSliderMax);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}