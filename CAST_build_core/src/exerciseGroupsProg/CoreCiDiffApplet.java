package exerciseGroupsProg;

import dataView.*;
import exercise2.*;

import expression.*;


abstract public class CoreCiDiffApplet extends CoreCiApplet {
	static final protected String USE_SE_TEMPLATES_OPTION = "seTemplates";
	
	protected ExpressionResultPanel seExpression = null;
	
	protected void registerParameterTypes() {
		super.registerParameterTypes();
		registerParameter("varName", "string");
		registerParameter("groupVarName", "string");
		registerParameter("group1Name", "string");
		registerParameter("n1", "int");
		registerParameter("group2Name", "string");
		registerParameter("n2", "int");
	}
	
//	--------------------------------------------------
	
	protected String getVarName() {
		return getStringParam("varName");
	}
	
	protected String getGroupVarName() {
		return getStringParam("groupVarName");
	}
	
	protected String getGroup1Name() {
		return getStringParam("group1Name");
	}
	
	protected int getN1() {
		return getIntParam("n1");
	}
	
	protected String getGroup2Name() {
		return getStringParam("group2Name");
	}
	
	protected int getN2() {
		return getIntParam("n2");
	}
	
//	--------------------------------------------------

	abstract protected DataSet getData();
	
	abstract protected XPanel getWorkingPanels(DataSet data);
	
	protected String ciLabelString() {
		return (group2First() ? getGroup2Name() : getGroup1Name()) + " " + getVarName() + " is higher by";
	}
	
	abstract protected boolean group2First();
	
	abstract protected void setDataForQuestion();
	
	
//-----------------------------------------------------------
	
	abstract protected void insertMessageContent(MessagePanel messagePanel);
	
	abstract protected int getMessageHeight();
	
//-----------------------------------------------------------
	
	abstract protected double evaluateCorrectSe();
	
	abstract protected double evaluateEstimate();
	
	abstract protected int getDf();
	
	protected double evaluateLowCorrect(double plusMinus) {
		double diff = evaluateEstimate();
		double meanDiff = group2First() ? -diff : diff;
		return meanDiff - plusMinus;
	}
	
	protected double evaluateHighCorrect(double plusMinus) {
		double diff = evaluateEstimate();
		double meanDiff = group2First() ? -diff : diff;
		return meanDiff + plusMinus;
	}
}