package matrixProg;

import java.awt.*;

import dataView.*;
import coreVariables.*;

import matrix.*;


public class InteractModelEqnApplet extends GenericModelEqnApplet {
	static final private String INTERACTION_NAME_PARAM = "interactionName";
	
	static final private Color kTableBackgroundColor = new Color(0xC8CCED);

	protected DataSet readData() {
		DataSet data = super.readData();
		
		CoreVariable xVar = data.getVariable("x1");
		if (xVar instanceof CatVariable) {
			CatCatInteractionVariable interactVar = new CatCatInteractionVariable(
								getParameter(INTERACTION_NAME_PARAM), data, "x1", "x2");
			
			data.addVariable("interact", interactVar);
		}
		
		return data;
	}
	
	protected ModelTerm[] findXTerms(DataSet data, String paramSymbol) {
		ModelTerm stdTerms[] = super.findXTerms(data, paramSymbol);
//		int startIndex = 0;
//		for (int i=0 ; i<stdTerms.length ; i++)
//			startIndex += stdTerms[i].noOfParameters();
		
		ModelTerm terms[] = new ModelTerm[stdTerms.length + 1];
		System.arraycopy(stdTerms, 0, terms, 0, stdTerms.length);
		CoreVariable xVar = data.getVariable("x1");
		
		if (xVar instanceof CatVariable) {
			FactorTerm interactionTerm = new FactorTerm(data, "interact", 0, true,
																		ModelTerm.DELTA, 0, kXColors[stdTerms.length], this);
			interactionTerm.setHeadingString(getParameter(INTERACTION_NAME_PARAM));
			interactionTerm.setEffectString(translate("interaction"));
			terms[stdTerms.length] = interactionTerm;
		}
		else {
			String shortNumVarName = ((VariateTerm)terms[1]).getGenericVarName();
			NumCatInteractTerm interactionTerm = new NumCatInteractTerm(data, "x1", "x2",
													true, ModelTerm.DELTA, 1, shortNumVarName, kXColors[stdTerms.length - 1], this);
			interactionTerm.setHeadingString(getParameter(INTERACTION_NAME_PARAM));
			terms[stdTerms.length] = interactionTerm;
		}
		return terms;
	}
	
	protected void addBottomPanel(XPanel thePanel) {
		CoreVariable xVar = data.getVariable("x1");
		String xKey = (xVar instanceof CatVariable) ? "x1" : null;
		
		CatSelectorView table = new CatSelectorView(data, this, xKey, "x2", kXColors[0], kXColors[1]);
		table.lockBackground(kTableBackgroundColor);
		
		thePanel.add(table);
	}
	
	protected MatrixFormulaValue createResponseView(DataSet data, ModelTerm yTerm) {
		MatrixFormulaValue yView = super.createResponseView(data, yTerm);
		CoreVariable xVar = data.getVariable("x1");
		if (!(xVar instanceof CatVariable))
			yView.setGroupKey("x2");
		return yView;
	}
}