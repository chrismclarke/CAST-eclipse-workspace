package dotPlotProg;

import java.awt.*;

import axis.*;
import dataView.*;
import random.RandomNormal;
import utils.*;

import dotPlot.*;


public class JitterStackApplet extends XApplet {
	static final private String RANDOM_NORMAL_PARAM = "random";
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String SHOW_VAR_NAME_PARAM = "showVarName";
	
	private final static int kJitterSliderMax = 100;
	
	private StackingDotPlotView theDotPlot;
	
	private XChoice crossSizeChoice;
	private XSlider jitterSlider, stackSlider;
	private XButton newJitterButton;
	
	public void setupApplet() {
		DataSet data = getData();
		
		setLayout(new BorderLayout(0, 5));
		add("Center", displayPanel(data));
		
		add("South", controlPanel());
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
		String valueString = getParameter(VALUES_PARAM);
		if (valueString != null)
			data.addNumVariable("y", getParameter(VAR_NAME_PARAM), valueString);
		else {
			String randomInfo = getParameter(RANDOM_NORMAL_PARAM);
			RandomNormal generator = new RandomNormal(randomInfo);
			double vals[] = generator.generate();
			
			data.addNumVariable("y", getParameter(VAR_NAME_PARAM), vals);
		}
		return data;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			String labelInfo = getParameter(AXIS_INFO_PARAM);
			theHorizAxis.readNumLabels(labelInfo);
			String showNameString = getParameter(SHOW_VAR_NAME_PARAM);
			if (showNameString != null && showNameString.equals("true"))
				theHorizAxis.setAxisName(data.getVariable("y").name);
			
		thePanel.add("Bottom", theHorizAxis);
		
			theDotPlot = new StackingDotPlotView(data, this, theHorizAxis, 0.0);
			theDotPlot.setCrossSize(DataView.LARGE_CROSS);
		thePanel.add("Center", theDotPlot);
			theDotPlot.lockBackground(Color.white);
		
		return thePanel;
	}
	
	private XPanel sliderPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
			jitterSlider = new XNoValueSlider(translate("no jittering"), translate("max jittering"),
																															null, 0, kJitterSliderMax, 0, this);
		thePanel.add("North", jitterSlider);
		
			stackSlider = new XNoValueSlider(translate("jittered"), translate("stacked"), null, 0,
																				StackingDotPlotView.kStackedIndex, 0, this);
		thePanel.add("Center", stackSlider);
		
		return thePanel;
	}
	
	private XPanel buttonPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 20));
		
			newJitterButton = new XButton(translate("Redo jittering"), this);
		thePanel.add(newJitterButton);
		
			crossSizeChoice = new XChoice(this);
			crossSizeChoice.addItem(translate("Small cross"));
			crossSizeChoice.addItem(translate("Medium cross"));
			crossSizeChoice.addItem(translate("Big cross"));
			crossSizeChoice.select(2);								//	medium cross
		thePanel.add(crossSizeChoice);
		
		return thePanel;
	}
	
	private XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
		thePanel.add("Center", sliderPanel());
		thePanel.add("East", buttonPanel());
		
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
			setJitterAmount(((double)value) / kJitterSliderMax);
			return true;
		}
		else if (target == stackSlider) {
			theDotPlot.setFrame(stackSlider.getValue());
			return true;
		}
		else if (target == crossSizeChoice) {
			theDotPlot.setCrossSize(crossSizeChoice.getSelectedIndex() + 1, stackSlider);
			repaint();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}