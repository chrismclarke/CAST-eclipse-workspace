package experProg;

import java.util.*;

import dataView.*;


abstract public class CoreMultiFactorApplet extends XApplet {
	static final public String CONSTANT_PARAM = "constant";
	
//	static final private String TREAT1_EFFECT_PARAM = "treat1Effect";
//	static final private String TREAT1_NAME_PARAM = "treat1Name";
//	static final private String TREAT1_VALUES_PARAM = "treat1Values";
//	static final private String TREAT1_LABELS_PARAM = "treat1Labels";
	
	static final private String RESPONSE_VALUES_PARAM = "responseValues";
	static final protected String RESPONSE_NAME_PARAM = "responseName";
	
	protected double constant;
	protected double effects[][];
	
	protected DataSet data;
	
	protected void readEffects() {
		StringTokenizer st = new StringTokenizer(getParameter(CONSTANT_PARAM));
		constant = Double.parseDouble(st.nextToken());
									//	for RotateLineFactorApplet, there are also min and max for constant
		
		int nCats=0;
		while (true) {
			String effectsString = getParameter("treat" + (nCats+1) + "Effect");
			if (effectsString == null)
				break;
			nCats++;
		}
		
		effects = new double[nCats][];
		
		for (int i=0 ; i<nCats ; i++) {
			String effectsString = getParameter("treat" + (i+1) + "Effect");
			st = new StringTokenizer(effectsString);
			
			double[] oneTreatEffect = new double[st.countTokens()];
			for (int j=0 ; j<oneTreatEffect.length ; j++)
				oneTreatEffect[j] = Double.parseDouble(st.nextToken());
			effects[i] = oneTreatEffect;
		}
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		int i=0;
		while (true) {
			String catCore = "treat" + (i+1);
			String catName = getParameter(catCore + "Name");
			if (catName == null)
				break;
			
			CatVariable treatVar = new CatVariable(catName);
			treatVar.readLabels(getParameter(catCore + "Labels"));
			treatVar.readValues(getParameter(catCore + "Values"));
			data.addVariable(catCore, treatVar);
			i++;
		}
		
		String responseValues = getParameter(RESPONSE_VALUES_PARAM);
		if (responseValues != null)
			data.addNumVariable("response", getParameter(RESPONSE_NAME_PARAM),
																									getParameter(RESPONSE_VALUES_PARAM));
		
		return data;
	}
	
	
	abstract public void finishAnimation();
}