package groupedDotPlotProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import valueList.OneValueView;

import dotPlot.*;


public class GroupingDotPlotApplet extends XApplet {
	static final protected String AXIS_INFO_PARAM = "horizAxis";
	static final protected String ANIMATE_BUTTON_PARAM = "hasAnimateButton";
	
	protected GroupingDotPlotView theDotPlot;
	
	private CatKey theKey;
	private XChoice jitterOrStack;
	
	private XCheckbox colourCheck;
	
	private XButton groupButton;
	protected XSlider animateSlider;
	
	public void setupApplet() {
		DataSet data = getData();
		
		setLayout(new BorderLayout());
		add("Center", dotPlotPanel(data));
		
		add("East", infoPanel(data));
		
		LabelVariable lv = (LabelVariable)data.getVariable("label");
		if (lv != null)
			add("North", labelPanel(data));
		
		add("South", controlPanel());
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		data.addCatVariable("group", getParameter(CAT_NAME_PARAM),
								getParameter(CAT_VALUES_PARAM), getParameter(CAT_LABELS_PARAM));
		
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		
		String labelVarName = getParameter(LABEL_NAME_PARAM);
		if (labelVarName != null)
			data.addLabelVariable("label", labelVarName, getParameter(LABELS_PARAM));
		
		return data;
	}
	
	protected XPanel infoPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 25));
		
			colourCheck = new XCheckbox(translate("Colour groups"), this);
			colourCheck.setState(false);
		thePanel.add(colourCheck);
		
			jitterOrStack = new XChoice(this);
			jitterOrStack.addItem(translate("stacked"));
			jitterOrStack.addItem(translate("jittered"));
			jitterOrStack.select(0);
			theDotPlot.stackCrosses(true);
		thePanel.add(jitterOrStack);
		
			XPanel keyPanel = new XPanel();
			keyPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
			
				theKey = new CatKey(data, "group", this, CatKey.VERT);
				theKey.show(false);		//	private version of show() since hidden component is not laid out
			keyPanel.add(theKey);
		thePanel.add(keyPanel);
			
		return thePanel;
	}
	
	protected XPanel labelPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		thePanel.add(new OneValueView(data, "label", this));
		return thePanel;
	}
	
	protected XPanel dotPlotPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			String labelInfo = getParameter(AXIS_INFO_PARAM);
			theHorizAxis.readNumLabels(labelInfo);
			theHorizAxis.setAxisName(data.getVariable("y").name);
		thePanel.add("Bottom", theHorizAxis);
		
			VertAxis theVertAxis = new VertAxis(this);
			CatVariable groupVariable = data.getCatVariable();
			theVertAxis.setCatLabels(groupVariable);
		thePanel.add("Left", theVertAxis);
		
			theDotPlot = new GroupingDotPlotView(data, this, theHorizAxis, theVertAxis);
			theDotPlot.setRetainLastSelection(true);
			theDotPlot.stackCrosses(true);
			theDotPlot.lockBackground(Color.white);
		thePanel.add("Center", theDotPlot);
		return thePanel;
	}
	
	protected XPanel controlPanel() {
		String animateString = getParameter(ANIMATE_BUTTON_PARAM);
		boolean hasAnimateButton =  animateString == null || animateString.equals("true");
		
		XPanel thePanel = new InsetPanel(hasAnimateButton ? 70 : 100, 0);
		
		thePanel.setLayout(new BorderLayout(10, 0));
		
		if (hasAnimateButton) {
			XPanel buttonPanel = new XPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 2));
				groupButton = new XButton(translate("Animate Grouping"), this);
			buttonPanel.add(groupButton);
			
			thePanel.add("West", buttonPanel);
		}
		
			animateSlider = new XNoValueSlider(translate("combined"), translate("grouped"), null, 0, GroupingDotPlotView.kEndFrame,
																						0, this);
		thePanel.add("Center", animateSlider);
		return thePanel;
	}
	
	public void showGroups(boolean showNotHide) {
		theDotPlot.setColorCrosses(showNotHide);
		theKey.show(showNotHide);
	}
	
	private boolean localAction(Object target) {
		if (target == jitterOrStack) {
			theDotPlot.stackCrosses(jitterOrStack.getSelectedIndex() == 0);
			return true;
		}
		else if (target == colourCheck) {
			theDotPlot.setColorCrosses(colourCheck.getState());
			theKey.show(colourCheck.getState());
			return true;
		}
		else if (target == animateSlider) {
			int newFrame = animateSlider.getValue();
			theDotPlot.setJitter(1 - newFrame / (animateSlider.getMaxValue() * 2.0));
			theDotPlot.setFrame(newFrame);
			return true;
		}
		else if (target == groupButton) {
			theDotPlot.doGroupingAnimation(animateSlider);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}