package boxPlotProg;

import java.awt.*;

import dataView.*;
import utils.*;
import qnUtils.*;
import imageGroups.TickCrossImages;
import valueList.ScrollValueList;


import stemLeaf.StemAndLeafView;


public class CheckQuartilesApplet extends XApplet {
	static final private String MEDIAN_PARAM = "median";
	static final private String LOW_QUART_PARAM = "lowQuartile";
	static final private String HIGH_QUART_PARAM = "highQuartile";
	
	AnswerEditPanel medianEdit;
	AnswerEditPanel lowQuartileEdit;
	AnswerEditPanel highQuartileEdit;
	XButton checkButton;
	XButton answerButton;
	XButton resetButton;
	
	public void setupApplet() {
		TickCrossImages.loadCrossAndTick(this);
		
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		
		controlPanel.add(editPanel());
		controlPanel.add(buttonPanel());
		
		if (getParameter(STEM_AXIS_PARAM) == null) {
//			ScrollImages.loadScroll(this);
			setLayout(new BorderLayout());
			ScrollValueList theList = new ScrollValueList(data, this, ScrollValueList.HEADING);
			theList.addVariableToList("y", ScrollValueList.RANK);
			theList.addVariableToList("y", ScrollValueList.RAW_VALUE);
			theList.sortByVariable("y", ScrollValueList.SMALL_FIRST);
			add("West", theList);
			add("Center", controlPanel);
		}
		else {
			setLayout(new ProportionLayout(0.35, 0));
			StemAndLeafView theView = new StemAndLeafView(data, this, getParameter(STEM_AXIS_PARAM));
			theView.lockBackground(Color.white);
			add(ProportionLayout.LEFT, theView);
			add(ProportionLayout.RIGHT, controlPanel);
		}
	}
	
	private XPanel editPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.RIGHT, VerticalLayout.VERT_CENTER, 5));
			medianEdit = new AnswerEditPanel(getParameter(MEDIAN_PARAM), this);
		thePanel.add(medianEdit);
			highQuartileEdit = new AnswerEditPanel(getParameter(LOW_QUART_PARAM), this);
		thePanel.add(highQuartileEdit);
			lowQuartileEdit = new AnswerEditPanel(getParameter(HIGH_QUART_PARAM), this);
		thePanel.add(lowQuartileEdit);
		return thePanel;
	}
	
	private XPanel buttonPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 8));
		checkButton = new XButton("Check", this);
		thePanel.add(checkButton);
		answerButton = new XButton("Tell Me", this);
		thePanel.add(answerButton);
		resetButton = new XButton(translate("Reset"), this);
		thePanel.add(resetButton);
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == checkButton) {
			medianEdit.checkAnswer();
			lowQuartileEdit.checkAnswer();
			highQuartileEdit.checkAnswer();
			return true;
		}
		else if (target == answerButton) {
			medianEdit.setCorrectAnswer();
			lowQuartileEdit.setCorrectAnswer();
			highQuartileEdit.setCorrectAnswer();
			return true;
		}
		else if (target == resetButton) {
			medianEdit.reset();
			lowQuartileEdit.reset();
			highQuartileEdit.reset();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}