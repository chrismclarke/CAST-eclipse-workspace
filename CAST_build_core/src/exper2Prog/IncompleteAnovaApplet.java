package exper2Prog;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;
import coreVariables.*;
import formula.*;

import ssq.*;
import indicator.*;
import glmAnova.*;
import exper2.*;

public class IncompleteAnovaApplet extends XApplet {
	static final private String Y_NAME_PARAM = "yVarName";
	static final private String Y_VALUES_PARAM = "yValues";
	static final private String X_NAME_PARAM = "xVarName";
	static final private String X_VALUES_PARAM = "xValues";
	static final private String X_LABELS_PARAM = "xLabels";
	static final private String Z_NAME_PARAM = "zVarName";
	static final private String Z_VALUES_PARAM = "zValues";
	static final private String Z_LABELS_PARAM = "zLabels";
	static final private String W_NAME_PARAM = "wVarName";
	static final private String W_VALUES_PARAM = "wValues";
	static final private String W_LABELS_PARAM = "wLabels";
	
	static final private String MAX_SSQ_PARAM = "maxSsq";
	static final private String COMPONENT_NAMES_PARAM = "componentNames";
	static final private String MAX_PARAM_PARAM = "maxParam";
	static final private String FIT_DECIMALS_PARAM = "fitDecimals";
	static final private String MAX_MISSING_PARAM = "maxMissing";
	static final private String DISPLAY_TYPE_PARAM = "displayType";
	
//	static final private String kNullKeys[] = {};
	static final private String kXKeys[] = {"x"};
	static final private String kXZKeys[] = {"x", "z"};
	static final private String kXZWKeys[] = {"x", "z", "w"};
	
	static final private String kComponentKeys[] = {"total", "xComp", "zComp", "wComp", "resid"};
	
	static final private Color kTableBackgroundColor = new Color(0xDAE4FF);
	static final private Color kParamBackgroundColor = new Color(0xD6E1FF);
	static final private Color kEffectLabelColor = new Color(0x000066);
	
	private NumValue maxParam;
	private double[] allY;
	private DataSet data;
	private AnovaSummaryData summaryData;
	private NumValue maxSsq, maxMss, maxF, maxRSquared;
	
	private InteractionEstimatesView interactions = null;
	
	private XSlider missingSlider;
	private int currentMissing = 0;
	
	public void setupApplet() {
		data = readData();
		summaryData = getSummaryData(data);
			summaryData.setSingleSummaryFromData();
		updateLS();
		
		setLayout(new BorderLayout(0, 30));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new BorderLayout(40, 0));
		
			topPanel.add("Center", dataTablePanel(data));
			topPanel.add("East", controlPanel(data));
		
		add("Center", topPanel);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new BorderLayout(0, 20));
		
//			bottomPanel.add("North", controlPanel(data));
			
				XPanel summaryPanel = getParameter(DISPLAY_TYPE_PARAM).equals("anova")
																		? anovaTablePanel(summaryData, data)
																		: parametersPanel(data);
			bottomPanel.add("Center", summaryPanel);
		
		add("South", bottomPanel);
	}
	
	private int[] intArray(int val, int rep) {
		int result[] = new int[rep];
		for (int i=0 ; i<rep ; i++)
			result[i] = val;
		return result;
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		
		int fitDecimals = Integer.parseInt(getParameter(FIT_DECIMALS_PARAM));
		maxParam = new NumValue(getParameter(MAX_PARAM_PARAM));
		int paramDecimals = maxParam.decimals;
		
			NumVariable yVar = new NumVariable(getParameter(Y_NAME_PARAM));
			yVar.readValues(getParameter(Y_VALUES_PARAM));
			allY = new double[yVar.noOfValues()];
			for (int i=0 ; i<allY.length ; i++)
				allY[i] = yVar.doubleValueAt(i);
		data.addVariable("y", yVar);
			String xLabels = MText.expandText(getParameter(X_LABELS_PARAM));
		data.addCatVariable("x", getParameter(X_NAME_PARAM), getParameter(X_VALUES_PARAM), xLabels);
			String zLabels = MText.expandText(getParameter(Z_LABELS_PARAM));
		data.addCatVariable("z", getParameter(Z_NAME_PARAM), getParameter(Z_VALUES_PARAM), zLabels);
		
		String wValueString = getParameter(W_VALUES_PARAM);
		if (wValueString != null)
			data.addCatVariable("w", getParameter(W_NAME_PARAM), wValueString, getParameter(W_LABELS_PARAM));
		else
			data.addVariable("w", new InteractionVariable(getParameter(W_NAME_PARAM), data, kXZKeys));
	
			MultipleRegnModel lsX = new MultipleRegnModel("LS_X", data, kXKeys);
			lsX.setParameterDecimals(intArray(paramDecimals, lsX.noOfParameters()));
		data.addVariable("lsX", lsX);
		data.addVariable("x_fit", new FittedValueVariable("Fit_X", data, kXKeys, "lsX", fitDecimals));
		
			MultipleRegnModel lsXZ = new MultipleRegnModel("LS_XZ", data, kXZKeys);
			lsXZ.setParameterDecimals(intArray(paramDecimals, lsXZ.noOfParameters()));
		data.addVariable("lsXZ", lsXZ);
		data.addVariable("xz_fit", new FittedValueVariable("Fit_XZ", data, kXZKeys, "lsXZ", fitDecimals));
		
			MultipleRegnModel lsXZW = new MultipleRegnModel("LS_XZW", data, kXZWKeys);
			lsXZW.setParameterDecimals(intArray(paramDecimals, lsXZW.noOfParameters()));
		data.addVariable("lsXZW", lsXZW);
		data.addVariable("xzw_fit", new FittedValueVariable("Fit_XZW", data, kXZWKeys, "lsXZW", fitDecimals));
		
			BasicComponentVariable totalComp = new BasicComponentVariable("Total", data, kXKeys, "y",
											"lsX", BasicComponentVariable.TOTAL, fitDecimals);
		data.addVariable("total", totalComp);
		
			BasicComponentVariable residComp = new BasicComponentVariable("Resid", data, kXZWKeys, "y",
											"lsXZW", BasicComponentVariable.RESIDUAL, fitDecimals);
		data.addVariable("resid", residComp);
		
			BasicComponentVariable xComp = new BasicComponentVariable("Expl_X", data, kXKeys, "y",
											"lsX", BasicComponentVariable.EXPLAINED, fitDecimals);
		data.addVariable("xComp", xComp);
		
			SeqComponentVariable zComp = new SeqComponentVariable("Expl_Z_X", data, "xz_fit",
																																				"x_fit", "y", fitDecimals);
		data.addVariable("zComp", zComp);
		
			SeqComponentVariable wComp = new SeqComponentVariable("Expl_W_XZ", data, "xzw_fit",
																																			"xz_fit", "y", fitDecimals);
		data.addVariable("wComp", wComp);
		
		return data;
	}
	
	private AnovaSummaryData getSummaryData(DataSet data) {
		StringTokenizer st = new StringTokenizer(getParameter(MAX_SSQ_PARAM));
		maxSsq = new NumValue(st.nextToken());
		maxMss = new NumValue(st.nextToken());
		maxF = new NumValue(st.nextToken());
		maxRSquared = new NumValue(st.nextToken());
		return new AnovaSummaryData(data, "error", kComponentKeys, maxSsq.decimals,
																	maxRSquared.decimals, maxMss.decimals, maxF.decimals);
	}
	
	private XPanel dataTablePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlexGridLayout(2, 6, 3, 1, 1));
		thePanel.add(new XPanel());
			
			XPanel xPanel = new XPanel();
			xPanel.setLayout(new BorderLayout(0, 3));
				
				CatVariable xVar = (CatVariable)data.getVariable("x");
				String xName = xVar.name;
				int nx = xVar.noOfCategories();
				XLabel xLabel = new XLabel(xName, XLabel.CENTER, this);
				xLabel.setFont(getStandardBoldFont());
			xPanel.add("North", xLabel);
			
				XPanel xLevelPanel = new XPanel();
				xLevelPanel.setLayout(new GridLayout(1, nx));
				for (int i=0 ; i<nx ; i++) {
					XLabel levelLabel = new XLabel(xVar.getLabel(i).toString(), XLabel.CENTER, this);
					xLevelPanel.add(levelLabel);
				}
			xPanel.add("Center", xLevelPanel);
			
		thePanel.add(xPanel);
			
			XPanel zPanel = new XPanel();
			zPanel.setLayout(new BorderLayout(6, 0));
				
				CatVariable zVar = (CatVariable)data.getVariable("z");
				String zName = zVar.name;
				int nz = zVar.noOfCategories();
				XLabel zLabel = new XVertLabel(zName, XLabel.CENTER, this);
				zLabel.setFont(getStandardBoldFont());
			zPanel.add("West", zLabel);
			
				XPanel zLevelPanel = new XPanel();
				zLevelPanel.setLayout(new GridLayout(nz, 1));
				for (int i=0 ; i<nz ; i++) {
					XLabel levelLabel = new XLabel(zVar.getLabel(i).toString(), XLabel.CENTER, this);
					zLevelPanel.add(levelLabel);
				}
			zPanel.add("Center", zLevelPanel);
			
		thePanel.add(zPanel);
		
		thePanel.add(new DataTableView(data, this, "y", "x", "z"));
			
		
		return thePanel;
	}
	
	private XPanel parametersPanel(DataSet data) {
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
				String xKey[] = {"x", "z"};
				int termsPerColumn[] = new int[3];
				for (int i=0 ; i<3 ; i++)
					termsPerColumn[i] = 1;
				
				TermEstimatesView mainEffects = new TermEstimatesView(data, this, xKey, "lsXZW", maxMainEffects, null,
										termsPerColumn);
			
			insetPanel.add(mainEffects);
			
				XLabel interactionsLabel = new XLabel(translate("Interactions"), XLabel.CENTER, this);
				interactionsLabel.setFont(getBigBoldFont());
				interactionsLabel.setForeground(kEffectLabelColor);
			insetPanel.add(interactionsLabel);
			
				interactions = new InteractionEstimatesView(data, this, "x", "z", "lsXZW", maxParam);
			
			insetPanel.add(interactions);
			
			insetPanel.lockBackground(kParamBackgroundColor);
		thePanel.add(insetPanel);
		
		return thePanel;
	}
	
	private XPanel anovaTablePanel(AnovaSummaryData summaryData, DataSet data) {
		XPanel thePanel = new InsetPanel(20, 5);
		thePanel.setLayout(new BorderLayout(0, 0));
		
			String componentName[] = new String[kComponentKeys.length];
			String componentNameString = getParameter(COMPONENT_NAMES_PARAM);
			if (componentNameString != null) {
				StringTokenizer st = new StringTokenizer(componentNameString, "#");
				for (int i=0 ; i<componentName.length ; i++)
					componentName[i] = st.nextToken();
			}
			else {
				componentName[0] = translate("Total");
				for (int i=0 ; i<kXZWKeys.length ; i++)
					componentName[i + 1] = data.getVariable(kXZWKeys[i]).name;
				componentName[kXZWKeys.length + 1] = translate("Residual");
			}
		
			AnovaTableView tableView = new AnovaTableView(summaryData, this,
										kComponentKeys, maxSsq, maxMss, maxF, AnovaTableView.SSQ_F_PVALUE);
			tableView.setComponentNames(componentName);
		
		thePanel.add("Center", tableView);
		thePanel.lockBackground(kTableBackgroundColor);
			
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			int maxMissing = Integer.parseInt(getParameter(MAX_MISSING_PARAM));
			int n = allY.length;
			missingSlider = new IntegerSlider("n", n - maxMissing, n, n,
																								IntegerSlider.VERTICAL, this, true);
			
		thePanel.add("Center", missingSlider);
		
		return thePanel;
	}
	
/*
	private void printComponents() {
		BasicComponentVariable totalComp = (BasicComponentVariable)data.getVariable("total");
		BasicComponentVariable residComp = (BasicComponentVariable)data.getVariable("resid");
		BasicComponentVariable xComp = (BasicComponentVariable)data.getVariable("xComp");
		SeqComponentVariable zComp = (SeqComponentVariable)data.getVariable("zComp");
		SeqComponentVariable wComp = (SeqComponentVariable)data.getVariable("wComp");
		
		System.out.println("\nComponents:\ntotal  resid     x      z     interact");
		int n = totalComp.noOfValues();
		for (int i=0 ; i<n ; i++)
			System.out.println(totalComp.valueAt(i) + "  " + residComp.valueAt(i) + "  "
									+ xComp.valueAt(i) + "  " + zComp.valueAt(i) + "  " + wComp.valueAt(i));
	}
*/
	
	private boolean[] getNullReps(DataSet data, CatVariable xVar, CatVariable zVar,
																									CatVariable wVar, NumVariable yVar) {
												//	only for x-z interactions where x & z are not zero
//		int nx = xVar.noOfCategories();
//		int nz = zVar.noOfCategories();
		int nw = wVar.noOfCategories();
		
		int reps[] = new int[wVar.noOfCategories()];
		for (int i=0 ; i<yVar.noOfValues() ; i++)
			if (!Double.isNaN(yVar.doubleValueAt(i)))
				reps[wVar.getItemCategory(i)] ++;
		
		boolean emptyCats = false;
		for (int i=0 ; i<reps.length ; i++)
			if (reps[i] == 0)
				emptyCats = true;
		
		if (emptyCats) {
			boolean nullReps[] = new boolean[nw - 1];
			for (int i=1 ; i<reps.length ; i++)
				nullReps[i - 1] = reps[i] == 0;
			return nullReps;
		}
		else
			return null;
	}
	
	private double[] getFullConstraints(DataSet data, CatVariable xVar, CatVariable zVar,
																									CatVariable wVar, boolean[] nullReps) {
		if (nullReps == null)
			return null;
		else {
			int nx = xVar.noOfCategories();
			int nz = zVar.noOfCategories();
			int nw = wVar.noOfCategories();
			
			double constraints[] = new double[nx + nz + nw - 2];
			for (int i=0 ; i<nx+nz-1 ; i++)
				constraints[i] = Double.NaN;
			for (int i=0 ; i<nullReps.length ; i++)
				constraints[nx + nz + i - 1] = nullReps[i] ? 0.0 : Double.NaN;
			return constraints;
		}
	}
	
	private void updateLS() {
		CatVariable xVar = (CatVariable)data.getVariable("x");
		CatVariable zVar = (CatVariable)data.getVariable("z");
		CatVariable wVar = (CatVariable)data.getVariable("w");
		NumVariable yVar = (NumVariable)data.getVariable("y");
		
		MultipleRegnModel lsX = (MultipleRegnModel)data.getVariable("lsX");
		lsX.updateLSParams("y");
	
		MultipleRegnModel lsXZ = (MultipleRegnModel)data.getVariable("lsXZ");
		lsXZ.updateLSParams("y");
	
		MultipleRegnModel lsXZW = (MultipleRegnModel)data.getVariable("lsXZW");
		boolean nullReps[] = getNullReps(data, xVar, zVar, wVar, yVar);
		double wConstraints[] = getFullConstraints(data, xVar, zVar, wVar, nullReps);
		lsXZW.updateLSParams("y", wConstraints);
		
		if (interactions != null)
			interactions.setInestimableParams(nullReps);
		
		data.variableChanged("y");
		
//		printComponents();
		
		summaryData.redoLastSummary();
	}

	
	private boolean localAction(Object target) {
		if (target == missingSlider) {
			int n = allY.length;
			int newMissing = n - missingSlider.getValue();
			if (newMissing != currentMissing) {
				NumVariable yVar = (NumVariable)data.getVariable("y");
				int newIndex0 = yVar.noOfValues() - newMissing;
				int currentIndex0 = yVar.noOfValues() - currentMissing;
				if (newMissing > currentMissing)
					for (int i=newIndex0 ; i<currentIndex0 ; i++)
						((NumValue)yVar.valueAt(i)).setValue(Double.NaN);
				else
					for (int i=currentIndex0 ; i<newIndex0 ; i++)
						((NumValue)yVar.valueAt(i)).setValue(allY[i]);
				
				currentMissing = newMissing;
				updateLS();
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