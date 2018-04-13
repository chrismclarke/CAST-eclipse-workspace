package ssqProg;

import java.awt.*;

import dataView.*;
import utils.*;
import valueList.*;
import formula.*;


public class MultipleTestApplet extends XApplet {
	static final public String FIXED_PROB_TYPE_PARAM = "fixedProbType";		//	individual or overall
	static final public String RESULT_DECIMALS_PARAM = "resultDecimals";
	
	static final private NumValue kPValue[] = {new NumValue("0.05"), new NumValue("0.01"), new NumValue("0.001")};
	static final private int kMaxTests = 50;
	
	static final private Color kResultBackgroundColor = new Color(0xEDF2FF);
	static final private Color kProbHeadingColor = new Color(0x990000);
	
	private int resultDecimals;
	private boolean fixedIndividualTests;
	
	private IntegerSlider noOfTestsSlider;
	private XChoice alpha0Choice;
	private int currentAlpha0Index = 0;
	
	private FixedValueView indepRejectView, depRejectView;
	
	public void setupApplet() {
		fixedIndividualTests = getParameter(FIXED_PROB_TYPE_PARAM).equals("individual");
		resultDecimals = Integer.parseInt(getParameter(RESULT_DECIMALS_PARAM));
		
		setLayout(new BorderLayout(0, 20));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 10));
	
			topPanel.add(noOfTestsPanel());
			topPanel.add(alphaPanel());
			
		add("North", topPanel);
			
		add("Center", resultPanel());
		
		setMultipleCoverage();
	}
	
	private XPanel noOfTestsPanel() {
		XPanel thePanel = new InsetPanel(100, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
		
			noOfTestsSlider = new IntegerSlider(translate("Number of tests"), 1,
																		kMaxTests, 1, XSlider.HORIZONTAL, this, true);
			noOfTestsSlider.setFont(getStandardBoldFont());
		
		thePanel.add("Center", noOfTestsSlider);
		
		return thePanel;
	}
	
	private XPanel alphaPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			String probString = translate("Probability of rejecting") + MText.expandText(" H#sub0# ")
											+ (fixedIndividualTests ? translate("in each test") : translate("in at least one test"))
											+ " =";
			alpha0Choice = new XChoice(probString, XChoice.HORIZONTAL, this);
			for (int i=0 ; i<kPValue.length ; i++)
				alpha0Choice.addItem(kPValue[i].toString());
		
		thePanel.add(alpha0Choice);
		
		return thePanel;
	}
	
	private String pRejectString() {
		return "P(" + translate(fixedIndividualTests ? "any rejected" : "reject") + ")";
	}
	
	private XPanel indepPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 3));
			
			XLabel l = new XLabel(translate("Independent tests"), XLabel.LEFT, this);
			l.setFont(getStandardBoldFont());
		thePanel.add(l);
		
			indepRejectView = new FixedValueView(pRejectString() + " =", new NumValue(1.0, resultDecimals), null, this);
		thePanel.add(indepRejectView);
		
		return thePanel;
	}
	
	private XPanel depPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 3));
			
			XLabel l = new XLabel(translate("Dependent tests"), XLabel.LEFT, this);
			l.setFont(getStandardBoldFont());
		thePanel.add(l);
		
			String pString = MText.expandText(pRejectString() + " #le#");
			depRejectView = new FixedValueView(pString, new NumValue(1.0, resultDecimals), null, this);
		thePanel.add(depRejectView);
		
		return thePanel;
	}
	
	private XPanel resultPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 15));
		
			String probString = fixedIndividualTests
							? (translate("Probability of rejecting") + MText.expandText(" H#sub0# ") + translate("in at least one test"))
							: (translate("Required probability of rejecting") + MText.expandText(" H#sub0# ") + translate("in individual tests"));
			XLabel probLabel = new XLabel(probString, XLabel.CENTER, this);
			probLabel.setFont(getBigBoldFont());
			probLabel.setForeground(kProbHeadingColor);
			
		thePanel.add(probLabel);
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new ProportionLayout(0.5, 30));
			
			mainPanel.add(ProportionLayout.LEFT, indepPanel());
			mainPanel.add(ProportionLayout.RIGHT, depPanel());
		
		thePanel.add(mainPanel);
		
		thePanel.lockBackground(kResultBackgroundColor);
		return thePanel;
	}
	
	private void setMultipleCoverage() {
		double alpha0 = kPValue[currentAlpha0Index].toDouble();
		int noOfTests = noOfTestsSlider.getValue();
		
		double indepPReject, depPReject;
		if (fixedIndividualTests) {
			indepPReject = 1.0 - Math.pow((1 - alpha0), noOfTests);
			depPReject = Math.min(noOfTests * alpha0, 1.0);
		}
		else {
			indepPReject = 1.0 - Math.pow((1 - alpha0), 1.0 / noOfTests);
			depPReject = alpha0 / noOfTests;
		}
		
		indepRejectView.setValue(new NumValue(indepPReject, resultDecimals));
		depRejectView.setValue(new NumValue(depPReject, resultDecimals));
	}

	
	private boolean localAction(Object target) {
		if (target == noOfTestsSlider) {
			setMultipleCoverage();
			return true;
		}
		else if (target == alpha0Choice) {
			int newChoice = alpha0Choice.getSelectedIndex();
			if (newChoice != currentAlpha0Index) {
				currentAlpha0Index = newChoice;
				setMultipleCoverage();
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}