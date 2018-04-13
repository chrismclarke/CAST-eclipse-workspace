package exerciseGroups;

import dataView.*;
import formula.*;


public class MSumDiffSdFormula {
	static final public int SINGLE_VALUES = 0;
	static final public int MEANS = 1;
	
//	static final private NumValue kOne = new NumValue(1, 0);
	
	static public MFormula sumDiffSdFormula(XApplet applet, int singleMeanType, boolean swap12) {
		FormulaContext stdContext = new FormulaContext(null, null, applet);
		String str1 = swap12 ? "2" : "1";
		String str2 = swap12 ? "1" : "2";
		
		MFormula s12 = new MSuperscript(new MSubscript(new MText("#sigma#", stdContext), str1,
																																				stdContext), "2", stdContext);
		MFormula s22 = new MSuperscript(new MSubscript(new MText("#sigma#", stdContext), str2,
																																				stdContext), "2", stdContext);
		
		if (singleMeanType == MEANS) {
			MFormula n1 = new MSubscript(new MText("n", stdContext), str1, stdContext);
			s12 = new MRatio(s12, n1, stdContext);
			MFormula n2 = new MSubscript(new MText("n", stdContext), str2, stdContext);
			s22 = new MRatio(s22, n2, stdContext);
		}
		
		MBinary varFormula = new MBinary(MBinary.PLUS, s12, s22, stdContext);
		return new MRoot(varFormula, stdContext);
	}
	
}