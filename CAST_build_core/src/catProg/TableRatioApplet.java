package catProg;

import java.awt.*;

import dataView.*;
import utils.*;
import coreVariables.*;

import cat.*;


public class TableRatioApplet extends XApplet {
	static final private String NUMER_NAME_PARAM = "numerName";
	static final private String NUMER_VALUES_PARAM = "numerValues";
	static final private String DENOM_NAME_PARAM = "denomName";
	static final private String DENOM_VALUES_PARAM = "denomValues";
	static final private String RATIO_NAME_PARAM = "ratioName";
	static final private String RATIO_DECIMALS_PARAM = "ratioDecimals";
	static final private String TOTAL_LABEL_PARAM = "totalLabel";
	
	static final private String[] kDisplayKeys = {"numerY", "denomY", "ratio"};
	static final private int[] kMinShift = {0, 0, 0};
	static final private int[] kMaxShift = {0, 0, 4};
	
	private DataSet data;
	private TableValuesView theTable;
	private XCheckbox ratioCheck;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout(0, 10));
		
		add("Center", tablePanel(data));
		add("South", controlPanel(data));
	}
	
	private void addReadVariable(DataSet data, String baseKey, String displayKey,
																								String varNameParam, String valuesParam) {
			NumVariable baseY = new NumVariable(baseKey);
			baseY.readValues(getParameter(valuesParam));
			double total = 0.0;
			ValueEnumeration ye = baseY.values();
			while (ye.hasMoreValues())
				total += ye.nextDouble();
			baseY.addValue(new NumValue(total, baseY.getMaxDecimals()));
		data.addVariable(baseKey, baseY);
		
			ShiftedVariable displayY = new ShiftedVariable(getParameter(varNameParam), baseY,
																																				baseKey, this);
		data.addVariable(displayKey, displayY);
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		
			LabelVariable labelVar = new LabelVariable(getParameter(LABEL_NAME_PARAM));
			labelVar.readValues(getParameter(LABELS_PARAM));
			labelVar.addValue(new LabelValue(getParameter(TOTAL_LABEL_PARAM)));
		data.addVariable("label", labelVar);
		
		addReadVariable(data, "numerBase", "numerY", NUMER_NAME_PARAM, NUMER_VALUES_PARAM);
		addReadVariable(data, "denomBase", "denomY", DENOM_NAME_PARAM, DENOM_VALUES_PARAM);
		
			int ratioDecimals = Integer.parseInt(getParameter(RATIO_DECIMALS_PARAM));
			RatioVariable baseRatioVar = new RatioVariable("ratioBase", data,
																												"numerY", "denomY", ratioDecimals);
		data.addVariable("ratioBase", baseRatioVar);
		
			ShiftedVariable displayRatio = new ShiftedVariable(getParameter(RATIO_NAME_PARAM),
																											baseRatioVar, "ratioBase", this);
		data.addVariable("ratio", displayRatio);
		
		return data;
	}

	private XPanel tablePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			theTable = new TableValuesView(data, this, "label", kDisplayKeys, kMinShift, kMaxShift);
		theTable.setYDisplayColumns(1);
		theTable.setFont(getBigFont());
		thePanel.add(theTable);
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
			ratioCheck = new XCheckbox(translate("Show") + " " + getParameter(RATIO_NAME_PARAM), this);
		thePanel.add(ratioCheck);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == ratioCheck) {
			theTable.setYDisplayColumns(ratioCheck.getState() ? 3 : 1);
			theTable.repaint();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
	
}