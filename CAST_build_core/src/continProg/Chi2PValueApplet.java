package continProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import distn.*;

import contin.*;


public class Chi2PValueApplet extends XApplet {
	static final public String DATA_NAME_PARAM = "dataName";

	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String X_VAR_NAME_PARAM = "xVarName";
	
	static final private String Y_LABELS_PARAM = "yLabels";
	static final private String X_LABELS_PARAM = "xLabels";
	
	static final private String Y_VALUES_PARAM = "yValues";
	static final private String X_VALUES_PARAM = "xValues";
	
	static final private String DESCRIPTION_PARAM = "description";
	static final private String CONCLUSION_PARAM = "conclusion";
	static final private String DESCRIPTION_WIDTH_PARAM = "descriptionWidth";
	static final private String CONCLUSION_WIDTH_PARAM = "conclusionWidth";
	
	static final private String MAX_EXPECTED_PARAM = "maxExpected";
	static final private String MAX_CHI2_PARAM = "maxChi2";
	
//	static final private double kMaxChi2Prob = 0.995;
	
	private DataSet data;
	private SummaryDataSet summaryData;
	
	private String[] dataName;
	private String[] description;
	private String[] conclusion;
	
	private XChoice dataSetChoice;
	private int currentDataSetIndex = 0;
	
	private ObsExpTableView oeView;
	private XTextArea descriptionText, conclusionText;
	
	private HorizAxis chi2Axis;
	private TailAreaView chi2View;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new ProportionLayout(0.55, 4, ProportionLayout.HORIZONTAL,
																							ProportionLayout.TOTAL));
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new BorderLayout(0, 6));
			leftPanel.add("North", dataChoicePanel(data));;
			leftPanel.add("Center", dataPanel(data));
			leftPanel.add("South", descriptionPanel());
			
		add(ProportionLayout.LEFT, leftPanel);
		
		summaryData = getSummaryData(data);
		setChi2();
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new BorderLayout(0, 5));
			rightPanel.add("Center", pValuePanel(data, summaryData));
			rightPanel.add("South", conclusionPanel());
		
		add(ProportionLayout.RIGHT, rightPanel);
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		int nDataSets = 1;
		while (getParameter(DATA_NAME_PARAM + (nDataSets + 1)) != null)
			nDataSets ++;
		
		dataName = new String[nDataSets];
		description = new String[nDataSets];
		conclusion = new String[nDataSets];
		
		for (int i=0 ; i<nDataSets ; i++) {
			String suffix = getSuffix(i);
			dataName[i] = getParameter(DATA_NAME_PARAM + suffix);
			description[i] = getParameter(DESCRIPTION_PARAM + suffix);
			conclusion[i] = getParameter(CONCLUSION_PARAM + suffix);
			CatVariable xVar = new CatVariable(getParameter(X_VAR_NAME_PARAM + suffix), Variable.USES_REPEATS);
			xVar.readLabels(getParameter(X_LABELS_PARAM + suffix));
			xVar.readValues(getParameter(X_VALUES_PARAM + suffix));
			data.addVariable("x" + suffix, xVar);
			CatVariable yVar = new CatVariable(getParameter(Y_VAR_NAME_PARAM + suffix), Variable.USES_REPEATS);
			yVar.readLabels(getParameter(Y_LABELS_PARAM + suffix));
			yVar.readValues(getParameter(Y_VALUES_PARAM + suffix));
			data.addVariable("y" + suffix, yVar);
		}
		
		return data;
	}
	
	private String getSuffix(int i) {
		return (i == 0) ? "" : String.valueOf(i + 1);
	}
	
	private SummaryDataSet getSummaryData(DataSet data) {
		SummaryDataSet summaryData = new SummaryDataSet(data, "y");
		
		Chi2DistnVariable chi2Distn = new Chi2DistnVariable("chi2 distn");
		summaryData.addVariable("theory", chi2Distn);
		
		chi2Axis = new HorizAxis(this);
		
		return summaryData;
	}
	
	private void setChi2() {
		Chi2DistnVariable chi2Distn = (Chi2DistnVariable)summaryData.getVariable("theory");
		double chi2 = oeView.getChi2();
//		double maxOnAxis = Math.max(1.1 * chi2, chi2Distn.getQuantile(kMaxChi2Prob));
										//		This works with MRJ but crashes Netscape VM
		int df = chi2Distn.getDF();
		double maxOnAxis = Math.max(1.2 * chi2, df + 4.0 * Math.sqrt(2.0 * df));
		String maxString = new NumValue(maxOnAxis, 2).toString();
		String labelString = "0 " + maxString + " 0 " + ((maxOnAxis < 10.0) ? "1"
																			: (maxOnAxis < 20.0) ? "2"
																			: (maxOnAxis < 50.0) ? "5"
																			: "10");
		chi2Axis.readNumLabels(labelString);
		
		chi2Distn.setDF(oeView.getDF());
		summaryData.setSelection("theory", chi2, Double.POSITIVE_INFINITY);
		
		summaryData.variableChanged("theory");
	}
	
	protected XPanel dataPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
		NumValue maxExpected = new NumValue(getParameter(MAX_EXPECTED_PARAM));
		
		oeView = new ObsExpTableView(data, this, "y", "x", maxExpected);
		thePanel.add("Center", oeView);
		
		return thePanel;
	}
	
	protected XPanel descriptionPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
		int descriptionWidth = Integer.parseInt(getParameter(DESCRIPTION_WIDTH_PARAM));
		descriptionText = new XTextArea(description, 0, descriptionWidth, this);
		descriptionText.lockBackground(Color.white);
		thePanel.add("Center", descriptionText);
		
		return thePanel;
	}
	
	protected XPanel conclusionPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
		int conclusionWidth = Integer.parseInt(getParameter(CONCLUSION_WIDTH_PARAM));
		conclusionText = new XTextArea(conclusion, 0, conclusionWidth, this);
		conclusionText.lockBackground(Color.white);
		conclusionText.setForeground(Color.red);
		thePanel.add("Center", conclusionText);
		
		return thePanel;
	}
	
	protected XPanel pValuePanel(DataSet data, SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 3));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 3));
		
				NumValue maxChi2 = new NumValue(getParameter(MAX_CHI2_PARAM));
			topPanel.add(new Chi2ValueView(data, this, oeView, Chi2ValueView.BRIEF, maxChi2));
		
			topPanel.add(new Chi2DFValueView(data, this, oeView));
		thePanel.add("North", topPanel);
		thePanel.add("Center", chi2DistnPanel(summaryData));
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			bottomPanel.add(new Chi2PValueView(data, this, oeView));
		thePanel.add("South", bottomPanel);
		
		return thePanel;
	}
	
	protected XPanel dataChoicePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
		dataSetChoice = new XChoice(this);
			for (int i=0 ; i<dataName.length ; i++)
				dataSetChoice.addItem(dataName[i]);
			dataSetChoice.select(currentDataSetIndex);
		thePanel.add(dataSetChoice);
		
		return thePanel;
	}
	
	private XPanel chi2DistnPanel(SummaryDataSet summaryData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		thePanel.add("Bottom", chi2Axis);
		
			chi2View = new TailAreaView(summaryData, this, chi2Axis, "theory");
			chi2View.lockBackground(Color.white);
		thePanel.add("Center", chi2View);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == dataSetChoice) {
			if (dataSetChoice.getSelectedIndex() != currentDataSetIndex) {
				currentDataSetIndex = dataSetChoice.getSelectedIndex();
				String suffix = getSuffix(currentDataSetIndex);
				oeView.setVariables("y" + suffix, "x" + suffix);
				oeView.changedCategoryLabels();
				data.variableChanged("y" + suffix);
				descriptionText.setText(currentDataSetIndex);
				
				setChi2();
				conclusionText.setText(currentDataSetIndex);
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