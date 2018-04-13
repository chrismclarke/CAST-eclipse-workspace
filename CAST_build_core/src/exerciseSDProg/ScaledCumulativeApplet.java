package exerciseSDProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import exercise2.*;

import exerciseSD.*;


public class ScaledCumulativeApplet extends ScaledMeanSdApplet {
//	static final private int CUT_OFF_INDEX = 8;
	
	static final private Color kTemplateBackground = new Color(0x9BD8F2);
	
	private ScaleXYTemplatePanel scaleXYTemplate, scaleYXTemplate;
	
	private ResultValuePanel propnResultPanel;
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
		super.registerParameterTypes();
		registerParameter("cutoff", "const");
	}
	
	private NumValue getCutOff() {
		return getNumValueParam("cutoff");
	}
	
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 5));
		
		thePanel.add("North", createConvertionFormula());
		
			XPanel centerPanel = new XPanel();
			centerPanel.setLayout(new BorderLayout(0, 12));
			
			centerPanel.add("North", createTemplatePanel());
		
				XPanel displayPanel = new XPanel();
				displayPanel.setLayout(new AxisLayout());
				
					theAxis = new HorizAxis(this);
				displayPanel.add("Bottom", theAxis);
				
					theView = new StackCumulativeView(data, this, theAxis, "y", getMaxResult().decimals);
					theView.setCrossSize(DataView.LARGE_CROSS);
					theView.lockBackground(Color.white);
					theView.setFont(getBigBoldFont());
					registerStatusItem("drag", (StackCumulativeView)theView);
				displayPanel.add("Center", theView);
				
			centerPanel.add("Center", displayPanel);
		
		thePanel.add("Center", centerPanel);
		
			propnResultPanel = new ResultValuePanel(this, translate("Proportion") + " =", 6);
			registerStatusItem("propn", propnResultPanel);
		thePanel.add("South", propnResultPanel);
		
		return thePanel;
	}
	
	private XPanel createTemplatePanel() {
		XPanel thePanel = new InsetPanel(0, 12, 0, 0);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			XPanel templatePanel = new InsetPanel(0, 4, 0, 0);
			templatePanel.setLayout(new BorderLayout(0, 5));
			
				scaleXYTemplate = new ScaleXYTemplatePanel(this, ScaleXYTemplatePanel.X_TO_Y, getMaxResult());
				registerStatusItem("XYTemplate", scaleXYTemplate);
			templatePanel.add("North", scaleXYTemplate);
		
				scaleYXTemplate = new ScaleXYTemplatePanel(this, ScaleXYTemplatePanel.Y_TO_X, getMaxResult());
				registerStatusItem("YXTemplate", scaleYXTemplate);
			templatePanel.add("South", scaleYXTemplate);
		
			templatePanel.lockBackground(kTemplateBackground);
		thePanel.add(templatePanel);
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		propnResultPanel.clear();
		
		scaleXYTemplate.setXYValues(kZeroValue, kZeroValue, kZeroValue);
		
		scaleYXTemplate.setYXValues(kZeroValue, kZeroValue, kZeroValue);
		
		setupConvertionFormula();
		
		theAxis.readNumLabels(getAxisInfo());
		theAxis.setAxisName(getVarName());
		theAxis.invalidate();
		
		((StackCumulativeView)theView).setDecimals(getMaxResult().decimals);
		((StackCumulativeView)theView).setCutoff((theAxis.minOnAxis + theAxis.maxOnAxis) / 2);
	}
	
	protected boolean rejectSample(double axisMin, double axisMax) {
		double sourceCutoff = getSourceCutoff();
		double gap = (axisMax - axisMin) / 200;
		double lowLimit = sourceCutoff - gap;
		double highLimit = sourceCutoff + gap;
		
		NumVariable yVar = (NumVariable)data.getVariable("y");
		ValueEnumeration ye = yVar.values();
		while (ye.hasMoreValues()) {
			double y = ye.nextDouble();
			if (y > lowLimit && y < highLimit)
				return true;
		}
		
		return false;
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Evaluate the proportion and type it in the box above.\n(Use one of the two blue templates to find the transformed cut-off value. Then drag the vertical red line in the dot plot to find the proportion of lower values.)");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("The template shows how to transform the cutoff value.");
				double sourceCutoff = getSourceCutoff();
				int nUnder = ((StackCumulativeView)theView).countUnder(sourceCutoff);
				int nTotal = ((NumVariable)data.getVariable("y")).noOfValues();
				messagePanel.insertText(" The proportion is " + nUnder + " divided by " + nTotal + ".");
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertRedText("You have given the correct proportion.");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Error!\n");
				messagePanel.insertRedText("You have not typed a value for the proportion.");
				break;
			case ANS_INVALID:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertRedText("Proportions must be between zero and one.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertRedText("Translate the cutoff with the formula  ");
				if (getDirection() == ScaleXYTemplatePanel.X_TO_Y)
					messagePanel.insertFormula(yToXFormula("value"));
				else
					messagePanel.insertFormula(xToYFormula("value"));
				messagePanel.insertRedText("\nThen find the proportion of values less than this.");
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 120;
	}
	
//-----------------------------------------------------------
	
	private double getSourceCutoff() {
		double destCutoff = getCutOff().toDouble();
		double intercept = getIntercept().toDouble();
		double slope = getSlope().toDouble();
		return (getDirection() == ScaleXYTemplatePanel.X_TO_Y)
												? (destCutoff - intercept) / slope : intercept + slope * destCutoff;
	}
	
	protected int assessAnswer() {
		int resultDecimals = getDecimals();
		double slop = Math.pow(10.01, -resultDecimals);
		
		if (propnResultPanel.isClear())
			return ANS_INCOMPLETE;
		else {
			double attempt = propnResultPanel.getAttempt().toDouble();
			if (attempt < 0.0 || attempt > 1)
				return ANS_INVALID;
			else {
				double sourceCutoff = getSourceCutoff();
				
				int nUnder = ((StackCumulativeView)theView).countUnder(sourceCutoff);
				int nTotal = ((NumVariable)data.getVariable("y")).noOfValues();
				double correct = nUnder / (double)nTotal;
				return (Math.abs(attempt - correct) < slop) ? ANS_CORRECT : ANS_WRONG;
			}
		}
	}
	
	protected void giveFeedback() {
	}
	
	protected void showCorrectWorking() {
		NumValue destCutoffVal = getCutOff();
		NumValue interceptVal = getIntercept();
		NumValue slopeVal = getSlope();
		
		double sourceCutoff = getSourceCutoff();
//		NumValue sourceCutoffVal = new NumValue(sourceCutoff, getDecimals());
		
		((StackCumulativeView)theView).setCutoff(sourceCutoff);
		theView.repaint();
		
		if (getDirection() == ScaleXYTemplatePanel.X_TO_Y) {
			scaleXYTemplate.setXYValues(kNanValue, kNanValue, kNanValue);
			scaleYXTemplate.setYXValues(destCutoffVal, interceptVal, slopeVal);
		}
		else {
			scaleXYTemplate.setXYValues(destCutoffVal, interceptVal, slopeVal);
			scaleYXTemplate.setYXValues(kNanValue, kNanValue, kNanValue);
		}
		
		int nUnder = ((StackCumulativeView)theView).countUnder(sourceCutoff);
		int nTotal = ((NumVariable)data.getVariable("y")).noOfValues();
		double correct = nUnder / (double)nTotal;
		propnResultPanel.showAnswer(new NumValue(correct, getDecimals()));
	}
	
	protected double getMark() {
		return (assessAnswer()== ANS_CORRECT) ? 1 : 0;
	}
}