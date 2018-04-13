package statisticProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import random.RandomRectangular;
import coreVariables.*;
import coreGraphics.*;

import statistic.*;


public class CenterSpreadApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String RANDOM_NORMAL_PARAM = "random";
	static final private String CENTER_WORDING_PARAM = "centerWording";
	static final private String SPREAD_WORDING_PARAM = "spreadWording";
	
	static final private String kVarKeys[] = {"y0", "y1"};
	
	static final private double kMinSpread = 0.15;
	
	static final protected Color kCenterColor = new Color(0x990000);
	static final protected Color kSpreadColor = new Color(0x000099);
	
	protected XNoValueSlider spreadSlider, centerSlider, spread2Slider, center2Slider;
	
	private DataSet data;
	
	private HorizAxis axis;
	protected DotPlotView theView;
	
	private XChoice sliderChoice;
	private int currentSliderChoice = 0;
	
	private XCheckbox wordingCheck;
	
	private XPanel sliderPanel;
	private CardLayout sliderPanelLayout;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout(0, 20));
		add("Center", createView(data));
		add("South", controlPanel(data));
		
		updateScaling();
	}
	
	private void setSliderText(XSlider slider, String title, String min, String max) {
		slider.setTitle(title, this);
		slider.setMinValue(min, 0);
		slider.setMaxValue(max, 100);
	}
	
	private void updateSliderWording() {
		String centerTitle = translate("Centre");
		String centerMin = translate("low");
		String centerMax = translate("high");
		String spreadTitle = translate("Spread");
		String spreadMin = translate("low");
		String spreadMax = translate("high");
		
		if (!wordingCheck.getState()) {
			StringTokenizer st = new StringTokenizer(getParameter(CENTER_WORDING_PARAM), "*");
			centerTitle = st.nextToken();
			centerMin = st.nextToken();
			centerMax = st.nextToken();
			
			st = new StringTokenizer(getParameter(SPREAD_WORDING_PARAM), "*");
			spreadTitle = st.nextToken();
			spreadMin = st.nextToken();
			spreadMax = st.nextToken();
		}
		
		setSliderText(centerSlider, centerTitle, centerMin, centerMax);
		setSliderText(center2Slider, centerTitle, centerMin, centerMax);
		setSliderText(spreadSlider, spreadTitle, spreadMin, spreadMax);
		setSliderText(spread2Slider, spreadTitle, spreadMin, spreadMax);
		
		centerSlider.invalidate();
		center2Slider.invalidate();
		spreadSlider.invalidate();
		spread2Slider.invalidate();
	}
	
	private void updateScaling() {
		double minOnAxis = axis.minOnAxis;
		double maxOnAxis = axis.maxOnAxis;
		double axisRange = maxOnAxis - minOnAxis;
		
		ScaledVariable baseVar = (ScaledVariable)data.getVariable("y0");
		double stdMin = 0.4;
		double stdRange = 0.2;
		baseVar.setScale(minOnAxis + stdMin * axisRange, stdRange * axisRange, 9);
		
		ScaledVariable transVar = (ScaledVariable)data.getVariable("y1");
		int centerSliderVal = (currentSliderChoice == 2) ? center2Slider.getValue() : centerSlider.getValue();
		int centerSliderMax = (currentSliderChoice == 2) ? center2Slider.getMaxValue() : centerSlider.getMaxValue();
		int spreadSliderVal = (currentSliderChoice == 2) ? spread2Slider.getValue() : spreadSlider.getValue();
		int spreadSliderMax = (currentSliderChoice == 2) ? spread2Slider.getMaxValue() : spreadSlider.getMaxValue();
		double stdCentre = 0.25 + 0.5 * centerSliderVal / (double)centerSliderMax;
		stdRange = 0.5 * (kMinSpread + (1.0 - kMinSpread) * spreadSliderVal / (double)spreadSliderMax);
		stdMin = stdCentre - stdRange / 2;
		
		transVar.setScale(minOnAxis + stdMin * axisRange, stdRange * axisRange, 9);
		data.variableChanged("y1");
//		theView.repaint();
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
		String randomInfo = getParameter(RANDOM_NORMAL_PARAM);
		RandomRectangular generator = new RandomRectangular(randomInfo);
		for (int i=0 ; i<2 ; i++) {
			String rawKey = "raw" + i;
			NumVariable rawVar = new NumVariable("raw data" + i);
			rawVar.setValues(generator.generate());
			data.addVariable(rawKey, rawVar);
			
			String yKey = "y" + i;
			data.addVariable(yKey, new ScaledVariable("scaled data" + i, rawVar, rawKey, 0.0, 1.0, 9));
		}
		
		StackedKeyVariable groupVar = new StackedKeyVariable(getParameter(CAT_NAME_PARAM), data, kVarKeys);
		groupVar.readLabels(getParameter(CAT_LABELS_PARAM));
		data.addVariable("group", groupVar);
		
		NumVariable yVar = new StackedNumVariable(getParameter(VAR_NAME_PARAM), data, kVarKeys);
		data.addVariable("y", yVar);
		
		return data;
	}
	
	private XPanel createView(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		axis = new HorizAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		axis.readNumLabels(labelInfo);
		axis.setAxisName(data.getVariable("y").name);
		thePanel.add("Bottom", axis);
		
		theView = getDataView(data, axis);
		theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	protected DotPlotView getDataView(DataSet data, HorizAxis axis) {
		return new TwoGroupDotView(data, this, axis, "y", "group");
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(20, 0));
		
			XPanel menuPanel = new XPanel();
			menuPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 20));
			
				CatVariable groupVar = (CatVariable)data.getVariable("group");
				StringTokenizer st = new StringTokenizer(translate("Adjust *'s"), "*");
				sliderChoice = new XChoice(st.nextToken() + groupVar.getLabel(1) + st.nextToken(), XChoice.VERTICAL_CENTER, this);
				sliderChoice.addItem(translate("Centre only"));
				sliderChoice.addItem(translate("Spread only"));
				sliderChoice.addItem(translate("Centre and spread"));
				
			menuPanel.add(sliderChoice);
			
				wordingCheck = new XCheckbox(translate("Generic wording"), this);
			menuPanel.add(wordingCheck);
			
		thePanel.add("West", menuPanel);
		
			sliderPanel = new XPanel();
			sliderPanelLayout = new CardLayout();
			sliderPanel.setLayout(sliderPanelLayout);
			
				XPanel centerPanel = new XPanel();
				centerPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
					
					centerSlider = new XNoValueSlider("", "", "", 0, 100, 50, this);
					centerSlider.setForeground(kCenterColor);
				centerPanel.add("Center", centerSlider);
				
			sliderPanel.add("center", centerPanel);
			
				XPanel spreadPanel = new XPanel();
				spreadPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
					
					spreadSlider = new XNoValueSlider("", "", "", 0, 100, 35, this);
					spreadSlider.setForeground(kSpreadColor);
				spreadPanel.add("Center", spreadSlider);
				
			sliderPanel.add("spread", spreadPanel);
			
				XPanel centerSpreadPanel = new XPanel();
				centerSpreadPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 12));
					
					center2Slider = new XNoValueSlider("", "", "", 0, 100, 50, this);
					center2Slider.setForeground(kCenterColor);
				centerSpreadPanel.add("Center", center2Slider);
				
					spread2Slider = new XNoValueSlider("", "", "", 0, 100, 35, this);
					spread2Slider.setForeground(kSpreadColor);
				centerSpreadPanel.add("Center", spread2Slider);
				
			sliderPanel.add("centerSpread", centerSpreadPanel);
			
			sliderPanelLayout.show(sliderPanel, "center");
			updateSliderWording();
			
		thePanel.add("Center", sliderPanel);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == centerSlider || target == spreadSlider
				|| target == center2Slider || target == spread2Slider) {
			updateScaling();
			return true;
		}
		else if (target == sliderChoice) {
			int newChoice = sliderChoice.getSelectedIndex();
			if (newChoice != currentSliderChoice) {

				if (currentSliderChoice == 2) {
					centerSlider.setValue(center2Slider.getValue());
					spreadSlider.setValue(spread2Slider.getValue());
				}
				else if (newChoice == 2) {
					center2Slider.setValue(centerSlider.getValue());
					spread2Slider.setValue(spreadSlider.getValue());
				}

				sliderPanelLayout.show(sliderPanel, (newChoice == 0) ? "center"
						: (newChoice == 1) ? "spread" : "centerSpread");

				currentSliderChoice = newChoice;

				if (currentSliderChoice == 0) {
					spreadSlider.setValue(35);
					updateScaling();
				}
				else if (currentSliderChoice == 1) {
					centerSlider.setValue(50);
					updateScaling();
				}
			}
			return true;
		}
		else if (target == wordingCheck) {
			updateSliderWording();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}