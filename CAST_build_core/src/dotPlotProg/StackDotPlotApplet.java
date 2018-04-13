package dotPlotProg;

import java.awt.*;

import axis.*;
import dataView.*;
import random.RandomNormal;
import utils.*;

import dotPlot.StackingDotPlotView;


public class StackDotPlotApplet extends XApplet {
	static final private String RANDOM_NORMAL_PARAM = "random";
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String SHOW_VAR_NAME_PARAM = "showVarName";
	
	private StackingDotPlotView theDotPlot;
	
	private XButton stackButton;
	private XChoice crossSizeChoice;
	private XSlider animateSlider;
	
	public void setupApplet() {
		DataSet data = getData();
		
		setLayout(new BorderLayout(0, 5));
		add("Center", displayPanel(data));
		
//		ScrollImages.loadScroll(this);
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
		
			theDotPlot = new StackingDotPlotView(data, this, theHorizAxis);
			theDotPlot.setCrossSize(DataView.LARGE_CROSS);
		thePanel.add("Center", theDotPlot);
			theDotPlot.lockBackground(Color.white);
		
		return thePanel;
	}
	
	private XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(10, 0));
		
			XPanel controlPanel = new XPanel();
			controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
				stackButton = new XButton(translate("Animate Stacking"), this);
			controlPanel.add(stackButton);
		
				crossSizeChoice = new XChoice(this);
				crossSizeChoice.addItem(translate("Small cross"));
				crossSizeChoice.addItem(translate("Medium cross"));
				crossSizeChoice.addItem(translate("Big cross"));
			controlPanel.add(crossSizeChoice);
		
		thePanel.add("North", controlPanel);
			crossSizeChoice.select(2);								//	big cross
		
			animateSlider = new XNoValueSlider(translate("jittered"), translate("stacked"), null, 0,
																StackingDotPlotView.kStackedIndex, 0, this);
//			animateSlider.setSnapToExtremes();
		thePanel.add("Center", animateSlider);
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == animateSlider) {
			theDotPlot.setFrame(animateSlider.getValue());
			return true;
		}
		else if (target == stackButton) {
			theDotPlot.doStackingAnimation(animateSlider);
			return true;
		}
		else if (target == crossSizeChoice) {
			theDotPlot.setCrossSize(crossSizeChoice.getSelectedIndex() + 1, animateSlider);
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