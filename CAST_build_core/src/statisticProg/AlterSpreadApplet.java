package statisticProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import axis.*;
import coreVariables.*;
import valueList.*;
import formula.*;

import statistic.*;


public class AlterSpreadApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String CENTER_SPREAD_PARAM = "centerSpread";
	static final private String DECIMALS_PARAM = "decimals";
	static final private String MAX_SD_PARAM = "maxSd";
	
	static final private Color kSdBackground = new Color(0xEEEEDD);
	
	private NumValue mean;
	private double minSpread, maxSpread, startSpread;
	private int dataDecimals, meanSdDecimals;
	
	private XNoValueSlider spreadSlider;
	private FixedValueView sdView;
	
	private DataSet data;
	
	private JitterMeanSdView theView;
	
	public void setupApplet() {
		readParams();
		
		data = getData();
		
		setLayout(new BorderLayout(0, 8));
		add("North", dataDisplay(data));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new BorderLayout(0, 12));
			mainPanel.add("North", sliderPanel());
			mainPanel.add("Center", bottomPanel(data));
		
		add("Center", mainPanel);
		
		updateScaling();
	}
	
	private void readParams() {
		StringTokenizer st = new StringTokenizer(getParameter(CENTER_SPREAD_PARAM));
		mean = new NumValue(st.nextToken());
		minSpread = Double.parseDouble(st.nextToken());
		maxSpread = Double.parseDouble(st.nextToken());
		startSpread = (minSpread + maxSpread) / 2;
		
		st = new StringTokenizer(getParameter(DECIMALS_PARAM));
		dataDecimals = Integer.parseInt(st.nextToken());
		meanSdDecimals = Integer.parseInt(st.nextToken());
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		
			NumVariable baseVar = new NumVariable("Base var");
			baseVar.readValues(getParameter(VALUES_PARAM));		//	should have mean=0 & approx range (-1, +1)
		data.addVariable("base", baseVar);
		
			String residName = MText.expandText("  (x - x#bar#)  ");
			ScaledVariable residVar = new ScaledVariable(residName, baseVar, "base",
																															0, startSpread, dataDecimals);
		data.addVariable("resid", residVar);
		
			ScaledVariable yVar = new ScaledVariable("x", residVar, "resid",
																													mean.toDouble(), 1, dataDecimals);
		data.addVariable("y", yVar);
		
			String resid2Name = MText.expandText("  (x - x#bar#)#sup2#");
		QuadraticVariable resid2Var = new QuadraticVariable(resid2Name, residVar, 0, 0, 1, 2 * dataDecimals);
		data.addVariable("resid2", resid2Var);
		
		return data;
	}
	
	private void updateScaling() {
		double sliderPropn = (spreadSlider.getValue() - spreadSlider.getMinValue())
												/ (double)(spreadSlider.getMaxValue() - spreadSlider.getMinValue());
		double scaleFactor = minSpread + (maxSpread - minSpread) * sliderPropn;
		
		ScaledVariable residVar = (ScaledVariable)data.getVariable("resid");
		residVar.setScale(0.0, scaleFactor, dataDecimals);
		int selectedIndex = data.getSelection().findSingleSetFlag();
		data.variableChanged("resid", selectedIndex);
		
		sdView.setValue(theView.getSD().toDouble());
	}
	
	private XPanel bottomPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
		thePanel.add("West", dataList(data));
		
		thePanel.add("Center", sdPanel(data));
		
		return thePanel;
	}
	
	private XPanel sdPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
			XPanel innerPanel = new InsetPanel(20, 10);
			innerPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 10));
			
				Font f = getBigFont();
				Font stdFont = new Font(f.getName(), Font.PLAIN, f.getSize() * 14 / 12);
				FormulaContext context = new FormulaContext(Color.red, kSdBackground, stdFont, this);
				Font sigmaFont = new Font(f.getName(), Font.PLAIN, f.getSize() * 18 / 12);
				FormulaContext sigmaContext = new FormulaContext(Color.red, kSdBackground, sigmaFont, this);
				
				MBinary devn = new MBinary(MBinary.MINUS, new MText("x", context), new MText("x#bar#", context), context);
				MBracket bracketDevn = new MBracket(devn, context);
				MSuperscript sqrDevn = new MSuperscript(bracketDevn, "2", context);
				MBinary ssq = new MBinary(Binary.BLANK, new MText("#capitalSigma#", sigmaContext), sqrDevn, context);
				
				MBinary df = new MBinary(MBinary.MINUS, new MText("n", context), new MText("1", context), context);
				
				MRatio variance = new MRatio(ssq, df, context);
				MRoot sd = new MRoot(variance, context);
			innerPanel.add(sd);
				
				NumValue maxSD = new NumValue(getParameter(MAX_SD_PARAM));
				sdView = new FixedValueView("s =", maxSD, 0.0, this);
				sdView.setForeground(Color.red);
				sdView.setFont(getBigFont());
			innerPanel.add(sdView);
			innerPanel.lockBackground(kSdBackground);
		
		thePanel.add(innerPanel);
		return thePanel;
	}
	
	private XPanel dataList(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new CenterFillLayout(CenterFillLayout.FILL_VERT));
		
			ScrollValueList theList = new ScrollValueList(data, this, FullValueList.HEADING);
			theList.addVariableToList("y", ScrollValueList.RAW_VALUE);
			theList.addVariableToList("resid", ScrollValueList.RAW_VALUE);
			theList.addVariableToList("resid2", ScrollValueList.RAW_VALUE);
			boolean displayTotals[] = {false, true, true};
			theList.addTotals(displayTotals);
			theList.setCanSelectRows(true);
			theList.setRetainLastSelection(true);
			theList.setFont(getBigFont());
		
		thePanel.add(theList);
		return thePanel;
	}
	
	private XPanel dataDisplay(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FixedSizeLayout(100, 180));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new AxisLayout());
			
				HorizAxis axis = new HorizAxis(this);
				String labelInfo = getParameter(AXIS_INFO_PARAM);
				axis.readNumLabels(labelInfo);
//				axis.setAxisName(data.getVariable("y").name);
			mainPanel.add("Bottom", axis);
			
				theView = new JitterMeanSdView(data, this, axis, "y", "resid", meanSdDecimals);
				theView.lockBackground(Color.white);
				theView.setRetainLastSelection(true);
				theView.setCrossSize(DataView.LARGE_CROSS);
			mainPanel.add("Center", theView);
		
		thePanel.add(mainPanel);
		
		return thePanel;
	}
	
	private XPanel sliderPanel() {
		XPanel thePanel = new InsetPanel(80, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
		
			spreadSlider = new XNoValueSlider(translate("low"), translate("high"), translate("Spread"), 0, 200, 100, this);
		thePanel.add("Center", spreadSlider);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == spreadSlider) {
			updateScaling();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}