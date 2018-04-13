package stdErrorProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import valueList.*;
import random.*;
import coreSummaries.*;
import coreVariables.*;

import cat.*;


public class ErrorCatSampleApplet extends ErrorSampleApplet {
	static final private String NCOLS_PARAM = "nCols";
	static final private String MAX_PROPN_PARAM = "maxPropn";
	static final private String SAMPLE_NAME_PARAM = "sampleName";
	
	static final private Color kSampleBackgroundColor = new Color(0xEDF2FF);
	
	protected double topProportion() {
		return 0.65;
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			StringTokenizer st = new StringTokenizer(getParameter(RANDOM_PARAM));
			int count = Integer.parseInt(st.nextToken());
			target = new NumValue(st.nextToken());
			double p[] = new double[2];
			p[0] = target.toDouble();
			p[1] = 1 - target.toDouble();
			RandomCat generator = new RandomCat(p, count);
			CatVariable y = new CatSampleVariable(getParameter(VAR_NAME_PARAM), generator);
			y.readLabels(getParameter(CAT_LABELS_PARAM));
		data.addVariable("y", y);
		
		return data;
	}
	
	protected SummaryDataSet getSummaryData(DataSet sourceData) {
		SummaryDataSet summaryData = new SummaryDataSet(sourceData, "y");
		
			int decimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
			
			PropnVariable estimator = new PropnVariable(translate("Proportion"), "y", decimals);
		summaryData.addVariable("est", estimator);
		
			ScaledVariable error = new ScaledVariable(getParameter(ERROR_NAME_PARAM), estimator,
																									"est", -target.toDouble(), 1.0, decimals);
		
		summaryData.addVariable("error", error);
		
		return summaryData;
	}
	
	protected XPanel dataPanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 5));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_TOP, 5));
			
				XLabel popnPropn = new XLabel(getParameter(SAMPLE_NAME_PARAM), XLabel.LEFT, this);
				popnPropn.setFont(getStandardBoldFont());
				popnPropn.setForeground(Color.blue);
//				popnPropn.lockBackground(Color.white);
			topPanel.add(popnPropn);
		
		thePanel.add("North", topPanel);
		
			XPanel listPanel = new InsetPanel(5, 0, 0, 7);
			listPanel.setLayout(new BorderLayout(0, 5));
			
				int nCols = Integer.parseInt(getParameter(NCOLS_PARAM));
				CatValueListView valueList = new CatValueListView(data, this, "y", null, nCols);
				valueList.setCanSelectValues(false);
				valueList.setForeground(Color.blue);
			listPanel.add("Center", valueList);
			
				XPanel propnPanel = new XPanel();
				propnPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
					NumValue maxPropn = new NumValue(getParameter(MAX_PROPN_PARAM));
					OneValueView sampPropnView = new OneValueView(summaryData, "est", this, maxPropn);
					sampPropnView.setLabel(getParameter(STATISTIC_NAME_PARAM) + " = ");
					sampPropnView.setForeground(Color.blue);
					sampPropnView.setFont(getStandardBoldFont());
					sampPropnView.setHighlightSelection(false);
				propnPanel.add(sampPropnView);
				
			listPanel.add("South", propnPanel);
		
			listPanel.lockBackground(kSampleBackgroundColor);
		thePanel.add("Center", listPanel);
		
			XPanel bottomPanel = new InsetPanel(0, 20, 0, 10);
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																											VerticalLayout.VERT_TOP, 5));
			
		
				OneValueView errorView = new OneValueView(summaryData, "error", this, maxPropn);
				errorView.setLabel(getParameter(ERROR_NAME_PARAM) + " = ");
				errorView.setForeground(Color.red);
				errorView.setFont(getStandardBoldFont());
			bottomPanel.add(errorView);
		
		thePanel.add("South", bottomPanel);
		
		return thePanel;
	}
}