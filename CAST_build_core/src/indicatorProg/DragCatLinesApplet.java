package indicatorProg;

import java.awt.*;

import dataView.*;
import utils.*;
import models.*;
import coreVariables.*;

import indicator.*;


public class DragCatLinesApplet extends CoreLinesApplet {
	static final private String MAX_PARAM_PARAM = "maxParam";
	
	static final private String[] kXDataInteractKey = {"x", "z", "xz"};
	static final private String[] kXHandleInteractKey = {"xHandle", "zHandle", "xzHandle"};
	
	static final private Color kEffectLabelColor = new Color(0x000066);
	
	private CatCatInteractionVariable xzInteractDataVar;
	private CatCatInteractionVariable xzInteractHandleVar;
	
	private boolean hasInteraction = false;
	private XCheckbox interactionCheck;
	
	protected String[] getXDataKeys() {
		return kXDataInteractKey;
	}
	
	protected String[] getXHandleKeys() {
		return kXHandleInteractKey;
	}
	
	protected void addExplanVariables(DataSet data) {
		super.addExplanVariables(data);
		xzInteractDataVar = new CatCatInteractionVariable("Interaction", data,
																							getXDataKeys()[0], getXDataKeys()[1]);
		data.addVariable(getXDataKeys()[2], xzInteractDataVar);
	}
	
	protected void createHandleVars(DataSet data) {
		super.createHandleVars(data);
		xzInteractHandleVar = new CatCatInteractionVariable("Interaction", data,
																getXHandleKeys()[0], getXHandleKeys()[1], xzInteractDataVar);
		data.addVariable(getXHandleKeys()[2], xzInteractHandleVar);
	}
	
	protected void fillXArray(Value[] x, Value xVal, Value zVal, Variable xVar, CatVariable zVar) {
		x[0] = xVal;
		x[1] = zVal;
		int xCat = ((CatVariable)xVar).labelIndex(xVal);
		int zCat = zVar.labelIndex(zVal);
		x[2] = xzInteractDataVar.getLabel(xCat, zCat);
	}
	
	protected void addBaselineHandles(Variable xVar, NumVariable yVar, CatVariable zVar,
												MultipleRegnModel model) {
		Value[] x = new Value[getXDataKeys().length];
		CatVariable xCatVar = (CatVariable)xVar;
		int nx = xCatVar.noOfCategories();
		
		for (int i=0 ; i<nx ; i++) {
			fillXArray(x, xCatVar.getLabel(i), zVar.getLabel(0), xVar, zVar);
			NumValue y = new NumValue(model.evaluateMean(x));
			
			xVar.addValue(x[0]);
			yVar.addValue(y);
			zVar.addValue(x[1]);
		}
	}
	
	protected void addGroupHandles(Variable xVar, NumVariable yVar, CatVariable zVar,
																															MultipleRegnModel model) {
		CatVariable xCatVar = (CatVariable)xVar;
		int nx = xCatVar.noOfCategories();
		int nz = zVar.noOfCategories();
		Value[] x = new Value[getXDataKeys().length];
		
		for (int i=1 ; i<nz ; i++) {
			fillXArray(x, xCatVar.getLabel(0), zVar.getLabel(i), xVar, zVar);
			NumValue y = new NumValue(model.evaluateMean(x));
			
			xVar.addValue(x[0]);
			yVar.addValue(y);
			zVar.addValue(x[1]);
		}
		
		for (int j=1 ; j<nz ; j++)
			for (int i=1 ; i<nx ; i++) {
				fillXArray(x, xCatVar.getLabel(i), zVar.getLabel(j), xVar, zVar);
				NumValue y = new NumValue(model.evaluateMean(x));
			
				xVar.addValue(x[0]);
				yVar.addValue(y);
				zVar.addValue(x[1]);
			}
	}
	
	protected DragParallelLinesView getLinesView(DataSet data) {
		DragCatCatLinesView theView = new DragCatCatLinesView(data, this, xAxis, yAxis,
									getXDataKeys(), kYDataKey,  getXHandleKeys(), kYHandleKey, "model", paramDecimals);
		theView.setJitter(0.02, 319823746);
		setAllowInteraction(false);						//	initially no interaction
		((DragCatCatLinesView)theView).setConstraints(getConstraints());
		return theView;
	}
	
	protected double[] getConstraints() {
		CatVariable xVar = (CatVariable)data.getVariable(getXDataKeys()[0]);
		int nx = xVar.noOfCategories();
		CatVariable zVar = (CatVariable)data.getVariable(getXDataKeys()[1]);
		int nz = zVar.noOfCategories();
		double constraints[] = new double[nx * nz];
		
		if (hasInteraction)
			for (int i=0 ; i<nx*nz ; i++)
				constraints[i] = Double.NaN;
		else
			for (int i=0 ; i<nx+nz-1 ; i++)
				constraints[i] = Double.NaN;
		return constraints;
	}
	
	protected void addInteractionCheck(XPanel thePanel) {
			interactionCheck = new XCheckbox(translate("Allow interaction"), this);
		thePanel.add(interactionCheck);
	}
	
	protected XPanel equationPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			InsetPanel insetPanel = new InsetPanel(10, 4, 10, 3);
			insetPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 5));
				
				NumValue maxParam = new NumValue(getParameter(MAX_PARAM_PARAM));
			
				XLabel mainEffectLabel = new XLabel(translate("Main effects"), XLabel.CENTER, this);
				mainEffectLabel.setFont(getBigBoldFont());
				mainEffectLabel.setForeground(kEffectLabelColor);
			insetPanel.add(mainEffectLabel);
			
				NumValue maxMainEffects[] = new NumValue[3];
				for (int i=0 ; i<3 ; i++)
					maxMainEffects[i] = maxParam;
				String xKey[] = new String[2];
				xKey[0] = getXHandleKeys()[0];
				xKey[1] = getXHandleKeys()[1];
				int termsPerColumn[] = new int[3];
				for (int i=0 ; i<3 ; i++)
					termsPerColumn[i] = 1;
				
				TermEstimatesView mainEffects = new TermEstimatesView(data, this, xKey, "model", maxMainEffects, null,
										termsPerColumn);
			
			insetPanel.add(mainEffects);
			
				XLabel interactionsLabel = new XLabel(translate("Interactions"), XLabel.CENTER, this);
				interactionsLabel.setFont(getBigBoldFont());
				interactionsLabel.setForeground(kEffectLabelColor);
			insetPanel.add(interactionsLabel);
			
				InteractionEstimatesView interactions = new InteractionEstimatesView(data, this, xKey[0], xKey[1],
										"model", maxParam);
			
			insetPanel.add(interactions);
			
			insetPanel.lockBackground(kEqnBackgroundColor);
		thePanel.add(insetPanel);
		
		return thePanel;
	}
	
	private void setAllowInteraction(boolean allowInteraction) {
		MultipleRegnModel model = (MultipleRegnModel)data.getVariable("model");
		NumVariable yVariable = (NumVariable)data.getVariable(kYHandleKey);
		CatVariable xVar = (CatVariable)data.getVariable(getXHandleKeys()[0]);
		int nx = xVar.noOfCategories();
		CatVariable zVar = (CatVariable)data.getVariable(getXHandleKeys()[1]);
		int nz = zVar.noOfCategories();
		
		hasInteraction = allowInteraction;
		if (allowInteraction) {
			for (int i=nx+nz-1 ; i<nx*nz ; i++)
				((NumValue)yVariable.valueAt(i)).setValue(0.0);
			updateHandleY(model);
		}
		else {
			for (int i=nx+nz-1 ; i<nx*nz ; i++)
				((NumValue)yVariable.valueAt(i)).setValue(Double.NaN);
			model.setXKey(getXHandleKeys());
			model.setLSParams(kYHandleKey, getConstraints(), paramDecimals, 0);
		}
		if (theView != null)
			((DragCatCatLinesView)theView).setConstraints(getConstraints());
		data.variableChanged(kYHandleKey);
	}
	
	private boolean localAction(Object target) {
		if (target == interactionCheck) {
			setAllowInteraction(interactionCheck.getState());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
}