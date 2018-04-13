package factorialProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;

import factorial.*;


public class AnovaTableOnlyApplet extends XApplet {
	static final private String START_MODEL_PARAM = "startModel";
	static final private String MAX_SSQ_PARAM = "maxSsq";
	static final private String MAX_INTERACT_PARAM = "maxInteract";
	
	protected FactorialDataSet data;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout(0, 0));
		
		add("Center", displayPanel(data));
	}
	
	protected FactorialDataSet readData() {
		int[][] startModel;
		StringTokenizer st = new StringTokenizer(getParameter(START_MODEL_PARAM), "+");
		startModel = new int[st.countTokens()][];
		for (int i=0 ; i<startModel.length ; i++) {
			StringTokenizer st2 = new StringTokenizer(st.nextToken(), "* ");
			startModel[i] = new int[st2.countTokens()];
			for (int j=0 ; j<startModel[i].length ; j++)
				startModel[i][j] = Integer.parseInt(st2.nextToken());
		}
		
		String maxInteractString = getParameter(MAX_INTERACT_PARAM);
		int maxInteract = (maxInteractString == null) ? -1 : Integer.parseInt(maxInteractString);
		data = new FactorialDataSet(maxInteract, startModel, this);
		
		MultiFactorModel model = (MultiFactorModel)data.getVariable("model");
		model.updateLSParams("y");
		
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 6, 0, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
		
		StringTokenizer st = new StringTokenizer(getParameter(MAX_SSQ_PARAM));
		NumValue maxSsq = new NumValue(st.nextToken());
//		int maxDF = Integer.parseInt(st.nextToken());
		NumValue maxMsq = new NumValue(st.nextToken());
		NumValue maxF = new NumValue(st.nextToken());
		
			FactorialAnovaTableView anovaTable = new FactorialAnovaTableView(data, this, maxSsq, maxMsq, maxF,
											FactorialAnovaTableView.SSQ_F_PVALUE, "model", "y");
		thePanel.add("Center", anovaTable);
		
		return thePanel;
	}
}