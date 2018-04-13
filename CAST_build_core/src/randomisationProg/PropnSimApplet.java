package randomisationProg;

import java.util.*;

import dataView.*;
import utils.*;
import random.*;
import coreSummaries.*;

import cat.*;


public class PropnSimApplet extends CoreStatDistanceApplet {
	static final private String PROPN_DECIMALS_PARAM = "propnDecimals";
	
	static final private String ACTUAL_PROB_PARAM = "actualProb";
	
	static final private String PROB_LABEL_PARAM = "probLabel";
	static final private String N_LABEL_PARAM = "nLabel";
	
	
	protected double dataPanelPropn() {
		return 0.35;
	}
	
	protected String lowerCaseParam() {
		return "propn";
	}
	
	protected void readParameters() {
		super.readParameters();
		
		StringTokenizer st = new StringTokenizer(getParameter(SAMPLE_INFO_PARAM));
		sampleSize = Integer.parseInt(st.nextToken());
		nullParam = new NumValue(st.nextToken());
		popnSd = new NumValue(Math.sqrt(nullParam.toDouble() * (1 - nullParam.toDouble())), nullParam.decimals);
		
		actualParam = new NumValue(getParameter(ACTUAL_PROB_PARAM));
		
		paramSdDecimals = Integer.parseInt(getParameter(PROPN_DECIMALS_PARAM));
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		double p[] = new double[2];
		p[0] = nullParam.toDouble();
		p[1] = 1.0 - p[0];
		RandomMultinomial generator = new RandomMultinomial(sampleSize, p);
		
		CatSampleVariable sv = new CatSampleVariable(getParameter(CAT_NAME_PARAM), generator);
		sv.readLabels(getParameter(CAT_LABELS_PARAM));
		data.addVariable("y", sv);
		
		return data;
	}
	
	protected void addStatistic(SummaryDataSet summaryData) {
		PropnVariable propnVar = new PropnVariable(translate("Propn"), "y", paramSdDecimals);
		summaryData.addVariable("stat", propnVar);
	}
	
	protected XPanel modelInfoPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
			String propnLabel = getParameter(PROB_LABEL_PARAM);
			XLabel propnText = new XLabel(propnLabel + " = " + nullParam.toString(), XLabel.CENTER, this);
			propnText.setFont(getStandardBoldFont());
			propnText.setForeground(kDarkRed);
		thePanel.add(propnText);
		
			String nLabel = getParameter(N_LABEL_PARAM);
			XLabel sampleSizeText = new XLabel(nLabel + " " + sampleSize, XLabel.CENTER, this);
			sampleSizeText.setFont(getStandardBoldFont());
			sampleSizeText.setForeground(kDarkRed);
		
		thePanel.add(sampleSizeText);
		
		return thePanel;
	}
	
	protected XPanel sampleViewPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.6, 5));
		
		thePanel.add(ProportionLayout.LEFT, dataTablePanel(data));
		thePanel.add(ProportionLayout.RIGHT, dataPieView(data));
		
		return thePanel;
	}
	
	private DataView dataPieView(DataSet data) {
		return new PieView(data, this, "y", CatDataView.SELECT_ONE);
	}
	
	protected XPanel dataTablePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
		
		CatVariable v = (CatVariable)data.getVariable("y");
		XLabel varName = new XLabel(v.name, XLabel.CENTER, this);
		varName.setFont(getStandardBoldFont());
		thePanel.add(varName);
		
		int decimals = Integer.parseInt(getParameter(PROPN_DECIMALS_PARAM));
		FreqTableView tableView = new FreqTableView(data, this, "y", CatDataView.SELECT_ONE, decimals);
		
		thePanel.add(tableView);
		
		return thePanel;
	}
	
	protected String summaryName(DataSet data) {
		CatVariable yVar = (CatVariable)data.getVariable("y");
		
		return translate("Propn") + " of " + yVar.getLabel(0).toString();
	}
}