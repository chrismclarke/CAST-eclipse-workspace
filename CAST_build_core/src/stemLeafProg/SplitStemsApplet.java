package stemLeafProg;

import java.awt.*;

import dataView.*;
import random.RandomNormal;
import utils.*;

import stemLeaf.*;


public class SplitStemsApplet extends XApplet {
	static final private String RANDOM_NORMAL_PARAM = "random";
	
	private SplitStemsView theStemAndLeaf;
	
	private XButton splitButton;
	private XChoice targetRepeatChoice;
	private XSlider animateSlider;
	
	public void setupApplet() {
		DataSet data = getData();
		
		setLayout(new BorderLayout(0, 5));
		add("Center", displayPanel(data));
		
//		ScrollImages.loadScroll(this);
		add("West", controlPanel());
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
		String randomInfo = getParameter(RANDOM_NORMAL_PARAM);
		if (randomInfo == null)
			data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		else {
			RandomNormal generator = new RandomNormal(randomInfo);
			double vals[] = generator.generate();
			data.addNumVariable("y", getParameter(VAR_NAME_PARAM), vals);
		}
		return data;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
			theStemAndLeaf = new SplitStemsView(data, this, getParameter(STEM_AXIS_PARAM));
		thePanel.add("Center", theStemAndLeaf);
			theStemAndLeaf.lockBackground(Color.white);
		
		return thePanel;
	}
	
	private XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(10, 0));
		
			XPanel controlPanel = new XPanel();
			controlPanel.setLayout(new GridLayout(2, 1));
				XPanel topPanel = new XPanel();
				topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 2));
					splitButton = new XButton(translate("Animate Splitting"), this);
				topPanel.add(splitButton);
			controlPanel.add(topPanel);
				XPanel bottomPanel = new XPanel();
				bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 2));
					targetRepeatChoice = new XChoice(this);
					targetRepeatChoice.addItem(translate("Split into") + " 2");
					targetRepeatChoice.addItem(translate("Split into") + " 5");
					targetRepeatChoice.select(0);								//	split into 2
				bottomPanel.add(targetRepeatChoice);
			controlPanel.add(bottomPanel);
		
		thePanel.add("North", controlPanel);
		
			XPanel sliderPanel = new XPanel();
			sliderPanel.setLayout(new CenterFillLayout(CenterFillLayout.FILL_VERT));
				animateSlider = new XNoValueSlider(translate("basic") + " ", translate("split") + " ", null, 0,
										SplitStemsView.kSplitIndex, 0, XSlider.VERTICAL_INVERSE, this);
				
			sliderPanel.add(animateSlider);
				
		thePanel.add("Center", sliderPanel);
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == animateSlider) {
			theStemAndLeaf.setFrame(animateSlider.getValue());
			return true;
		}
		else if (target == splitButton) {
			theStemAndLeaf.doSplittingAnimation(animateSlider);
			return true;
		}
		else if (target == targetRepeatChoice) {
			int targetRepeats = (targetRepeatChoice.getSelectedIndex() == 0) ? 2 : 5;
			theStemAndLeaf.setTargetRepeats(targetRepeats, animateSlider);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}