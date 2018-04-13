package curveInteractProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import models.*;

import ssq.*;
import curveInteract.*;


public class QuadParamAnovaApplet extends QuadParamTestApplet {
	static final private String MODEL_SEQUENCE_PARAM = "modelSequence";
	static final private String COMP_NAME_PARAM = "compName";
	static final private String MAX_SSQ_PARAM = "maxSsq";
	static final private String SHOW_T_PARAM = "showT";
	
//	static final private String kLSInteractKeys[] = {"lsMean", "lsXZ", "ls"};
//	
//	static final private int kModelParams[] = {1, 3, 4};
	
	protected NumValue maxSsq, maxMsq, maxF;
	
	private SummaryDataSet summaryData;
	
	private String lsKeys[];
	private int nModelParams[];
	private String compName[];
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		StringTokenizer stModel = new StringTokenizer(getParameter(MODEL_SEQUENCE_PARAM));
		StringTokenizer stName = new StringTokenizer(getParameter(COMP_NAME_PARAM), "#");
		int nModels = stModel.countTokens();
		
		lsKeys = new String[nModels];
		nModelParams = new int[nModels];
		compName = new String[nModels + 1];		//	first is total, last is residual
		
		for (int i=0 ; i<nModels ; i++) {
			lsKeys[i] = (i == nModels - 1) ? "model" : "ls" + i;		//	"model" is full model
			String modelParams = stModel.nextToken();
			nModelParams[i] = modelParams.length();
			
			if (i < nModels - 1) {		//	since full model has already been created by super-class
				double constraints[] = new double[6];		//	all zero
				for (int j=0 ; j<nModelParams[i] ; j++)
					constraints[modelParams.charAt(j) - '0'] = Double.NaN;
					
				ResponseSurfaceModel ls = new ResponseSurfaceModel(lsKeys[i], data, kXZKeys,
																													getParameter(INITIAL_PARAM_PARAM));
				ls.updateLSParams("y", constraints);
				
				data.addVariable(lsKeys[i], ls);
			}
			compName[i] = stName.nextToken();
		}
		compName[nModels] = stName.nextToken();
		
		
		SsqDiffComponentVariable.addComponentsToData(data, kXZKeys,
																				"y", lsKeys, compName, nModelParams, 9);
		
		StringTokenizer st = new StringTokenizer(getParameter(MAX_SSQ_PARAM));
		maxSsq = new NumValue(st.nextToken());
		maxMsq = new NumValue(st.nextToken());
		maxF = new NumValue(st.nextToken());
		summaryData = new AnovaSummaryData(data, "error",
																		SsqDiffComponentVariable.getComponentKeys(compName.length),
																		maxSsq.decimals, 4);
		summaryData.setSingleSummaryFromData();
		
		return data;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 5, 0, 0);
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
		String showTString = getParameter(SHOW_T_PARAM);
		if (showTString != null && showTString.equals("true"))
			thePanel.add(super.controlPanel(data));
			
			XPanel anovaPanel = new InsetPanel(20, 7);
			anovaPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
				AnovaTableView anovaTable = new AnovaTableView(summaryData, this,
															SsqDiffComponentVariable.getComponentKeys(compName.length),
															maxSsq, maxMsq, maxF, AnovaTableView.SSQ_F_PVALUE);
				anovaTable.setComponentNames(compName);
				anovaTable.setComponentColors(SsqDiffComponentVariable.getComponentColors(compName.length));
			anovaPanel.add(anovaTable);
		
			anovaPanel.lockBackground(kTableBackground);
		thePanel.add(anovaPanel);
		
		return thePanel;
	}
}