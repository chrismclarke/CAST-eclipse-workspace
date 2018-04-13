package exerciseMeanSum;

import java.util.*;

import dataView.*;
import formula.*;
import utils.*;
import exercise2.*;



public class MeanSumTemplatePanel extends MainFormulaPanel implements StatusInterface {
	static final public int SUM_MEAN = 0;
	static final public int MEAN_SD = 1;
	static final public int SUM_SD = 2;
	
	@SuppressWarnings("unused")
	private int templateType;
	private Edit nEdit, muEdit, sigmaEdit;
	
	public MeanSumTemplatePanel(int templateType, NumValue maxResult, FormulaContext context) {
		super(null, null, maxResult, context);
		
		this.templateType = templateType;
		switch (templateType) {
			case SUM_MEAN:
				setFormula(createSumMeanFormula(context));
				break;
			case MEAN_SD:
				setFormula(createMeanSdFormula(context));
				break;
			case SUM_SD:
				setFormula(createSumSdFormula(context));
				break;
		}
	}
	
	public String getStatus() {
		String s = "";
		if (nEdit != null)
			s += nEdit.getValue() + " ";
		if (muEdit != null)
			s += muEdit.getValue() + " ";
		if (sigmaEdit != null)
			s += sigmaEdit.getValue() + " ";
		return s;
	}
	
	public void setStatus(String statusString) {
		StringTokenizer st = new StringTokenizer(statusString);
		
		NumValue n = (nEdit == null) ? null : new NumValue(st.nextToken());
		NumValue mu = (muEdit == null) ? null : new NumValue(st.nextToken());
		NumValue sigma = (sigmaEdit == null) ? null : new NumValue(st.nextToken());
		setValues(n, mu, sigma);
	}
	
	private FormulaPanel createSumMeanFormula(FormulaContext context) {
		nEdit = new Edit("1", 3, context);
		muEdit = new Edit("0.5", 3, context);
		
		return new Binary(Binary.TIMES, nEdit, muEdit, context);
	}
	
	private FormulaPanel createMeanSdFormula(FormulaContext context) {
		nEdit = new Edit("1", 3, context);
		sigmaEdit = new Edit("1", 3, context);
		
		return new Ratio(sigmaEdit, new Root(nEdit, context), context);
	}
	
	private FormulaPanel createSumSdFormula(FormulaContext context) {
		nEdit = new Edit("1", 3, context);
		sigmaEdit = new Edit("1", 3, context);
		
		return new Binary(Binary.TIMES, new Root(nEdit, context), sigmaEdit, context);
	}
	
	public void setValues(NumValue n, NumValue mu, NumValue sigma) {
		nEdit.setValue(n);
		if (muEdit != null)
			muEdit.setValue(mu);
		if (sigmaEdit != null)
			sigmaEdit.setValue(sigma);
		super.displayResult();
	}
	
	public void displayResult() {
		super.displayResult();
		((ExerciseApplet)context.getApplet()).noteChangedWorking();
	}
}