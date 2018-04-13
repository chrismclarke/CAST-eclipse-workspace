package twoGroup;

import java.util.*;

import dataView.*;
import formula.*;
import imageGroups.*;
import utils.*;



public class SDCalcPanel extends MainFormulaPanel implements StatusInterface {
	private Edit sdEdit, nEdit;
	
	public SDCalcPanel(NumValue maxResultVal, FormulaContext context) {
		super(GroupsEqualsImages.sigmaXbarHat, GroupsEqualsImages.kSigmaParamWidth,
					GroupsEqualsImages.kSigmaParamAscent, GroupsEqualsImages.kSigmaParamDescent,
					null, maxResultVal, context);
		setFormula(createFormula(context));
	}
	
	protected FormulaPanel createFormula(FormulaContext context) {
		FormulaContext smallContext = context.getSmallerContext();
		
		sdEdit = new Edit("1.0", 3, context);
		nEdit = new Edit("1", 3, context);
		return new Ratio(sdEdit, new Root(nEdit, smallContext), context);
	}
	
	public void changeMaxValue(NumValue maxSd, NumValue maxSe) {
		sdEdit.setColumns(maxSd.toString().length() - 1);
		super.changeMaxValue(maxSe);
	}
	
	public String getStatus() {
		return sdEdit.getValue() + " " + nEdit.getValue();
	}
	
	public void setStatus(String statusString) {
		StringTokenizer st = new StringTokenizer(statusString);
		
		NumValue sd = new NumValue(st.nextToken());
		int n = Integer.parseInt(st.nextToken());
		setValues(sd, n);
	}
	
	public void setValues(NumValue sd, int n) {
		sdEdit.setValue(sd);
		nEdit.setValue(new NumValue(n, 0));
		displayResult();
	}
}