package glmAnovaProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;

import glmAnova.*;


public class AnovaTableSeqApplet extends XApplet {
	static final protected String MAX_SSQ_PARAM = "maxSsq";
	static final protected String INIT_VARIABLES_PARAM = "initVariables";
	static final protected String SHOW_R2_PARAM = "showR2";
	
//	static final private int kMaxR2Decimals = 4;
	
	private LabelValue kExplainedLabel, kUnexplainedLabel, kTotalLabel;
	
	protected DataSet data;
	
	protected String xKeys[];
//	private String fitKeys[];
	protected String componentKeys[];
	
	protected String componentName[];
	protected String variableName[];
	protected Color componentColor[];
	
	protected NumValue maxSsq, maxMsq, maxF;
	protected int maxDF;
	protected boolean showTests;
	
	private SsqChartView ssqChart;
	private R2SeqView r2ValueView;
	
	public void setupApplet() {
		readMaxSsqs();
		
		data = readData();
		
		setLayout(new BorderLayout(10, 0));
		
		if (!showTests) {
			kExplainedLabel = new LabelValue(translate("Explained"));
			kUnexplainedLabel = new LabelValue(translate("Unexplained"));
			kTotalLabel = new LabelValue(translate("Total"));
			
			add("East", rightPanel(data));
		}
			
			XPanel tablePanel = new XPanel();
			tablePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_SPACED, 0));
			
				AnovaSeqTableView table = new AnovaSeqTableView(data, this,
										componentKeys, maxSsq, componentName, componentColor, variableName, ssqChart);
				table.setFont(getBigFont());
				if (showTests)
					table.setShowTests(true, maxMsq, maxF);
				
			tablePanel.add(table);
				
			String showR2String = getParameter(SHOW_R2_PARAM);
			if (showR2String == null || showR2String.equals("true")) {
				XPanel r2Panel = new XPanel();
				r2Panel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				
					r2ValueView = new R2SeqView(data, componentKeys, this);
					r2ValueView.setFont(getBigFont());
					table.setLinkedView(r2ValueView);
				r2Panel.add(r2ValueView);
			
				tablePanel.add(r2Panel);
			}
				
		add("Center", tablePanel);
		
			String initVarString = getParameter(INIT_VARIABLES_PARAM);
			if (initVarString != null) {
				int initVars = Integer.parseInt(initVarString);
				table.setLastSeparateX(initVars - 1);
			}
	}
	
	protected void readMaxSsqs() {
		StringTokenizer st = new StringTokenizer(getParameter(MAX_SSQ_PARAM));
		maxSsq = new NumValue(st.nextToken());
		maxDF = Integer.parseInt(st.nextToken());
		showTests = st.hasMoreTokens();
		if (showTests) {
			maxMsq = new NumValue(st.nextToken());
			maxF = new NumValue(st.nextToken());
		}
	}
	
	protected DataSet readData() {
		SequentialDataSet data = new SequentialDataSet(this);
		xKeys = data.getXKeys();
//		fitKeys = data.getFitKeys();
		componentKeys = data.getComponentKeys();
		componentName = data.getComponentNames();
		variableName = data.getVariableNames();
		componentColor = data.getComponentColors();
		return data;
	}
	
	private XPanel rightPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
			
			XLabel ssqLabel = new XLabel(translate("Sums of squares"), XLabel.RIGHT, this);
			ssqLabel.setFont(getBigBoldFont());
		thePanel.add("North", ssqLabel);
		
			ssqChart = new SsqChartView(data, this, componentKeys, kExplainedLabel,
									kUnexplainedLabel, kTotalLabel, Color.blue, Color.red, Color.black);
			ssqChart.setFont(getBigFont());
			
		thePanel.add("Center", ssqChart);
		return thePanel;
	}
}