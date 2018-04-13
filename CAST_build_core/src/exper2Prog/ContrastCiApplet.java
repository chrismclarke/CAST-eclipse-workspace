package exper2Prog;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import models.*;
import formula.*;
import valueList.*;
import distn.*;
import coreSummaries.*;

import exper2.*;


public class ContrastCiApplet extends XApplet {
	static final private String Y_NAME_PARAM = "yVarName";
	static final private String Y_VALUES_PARAM = "yValues";
	static final private String X_NAME_PARAM = "xVarName";
	static final private String X_VALUES_PARAM = "xValues";
	static final private String X_LABELS_PARAM = "xLabels";
	static final private String Y_AXIS_INFO_PARAM = "yAxis";
	
	static final private String MAX_PARAM_PARAM = "maxParam";
	static final private String CONTRAST_PARAM = "contrast";
	static final private String MAX_SE_PARAM = "maxSe";
	
	static final private Color kEstSeBackground = new Color(0xDDDDEE);
	static final private Color kInferenceLabelColor = new Color(0x990000);
	
	static final private NumValue kZeroValue = new NumValue(0, 0);
	static final private NumValue kMaxPValue = new NumValue(9, 4);
	
	private DataSet data;
	
	private ShowContrastView theView;
	
	private NumValue maxParam, maxSe;
	
	private XNumberEditPanel contrastEdit[];
	private XButton calculateButton;
	private FixedValueView contrastValue, contrastValue2, contrastValue3;
	
	private int df;
	private FixedValueView seValue, seValue2, seValue3;
	private FixedValueView pValueView;
	private MainFormulaPanel tPanel;
	
	private FixedValueView ciValue;
	
	private boolean showingContrast = true;
	private XChoice contrastChoice;
	private NumValue[][] fixedContrasts;
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout(0, 6));
			
		add("Center", displayPanel(data));
		add("South", bottomPanel(data));
		
		clearContrastDisplay();
		if (contrastChoice != null) {
			applyFixedContrast(0);
			contrastChoice.select(1);
		}
		checkValidContrast();
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		
			NumVariable yVar = new NumVariable(getParameter(Y_NAME_PARAM));
			yVar.readValues(getParameter(Y_VALUES_PARAM));
		data.addVariable("y", yVar);
			
			CatVariable xVar = new CatVariable(getParameter(X_NAME_PARAM));
			xVar.readLabels(getParameter(X_LABELS_PARAM));
			xVar.readValues(getParameter(X_VALUES_PARAM));
				
		data.addVariable("x", xVar);
		
			String[] keys = {"x"};
			maxParam = new NumValue(getParameter(MAX_PARAM_PARAM));
			int nLevels = xVar.noOfCategories();
			int[] decimals = new int[nLevels];
			for (int i=0 ; i<nLevels ; i++)
				decimals[i] = maxParam.decimals;
				
			MultipleRegnModel ls = new MultipleRegnModel("Full model", data, keys);
			ls.setLSParams("y", decimals, 9);
		data.addVariable("ls", ls);
		
		return data;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(100, 0);
		thePanel.setLayout(new BorderLayout());
			
			XPanel scatterPanel = new XPanel();
			scatterPanel.setLayout(new AxisLayout());
				
					NumVariable yVar = (NumVariable)data.getVariable("y");
					CatVariable xVar = (CatVariable)data.getVariable("x");
					
					HorizAxis xAxis = new HorizAxis(this);
					xAxis.setCatLabels(xVar);
					xAxis.setAxisName(xVar.name);
				
				scatterPanel.add("Bottom", xAxis);
				
					VertAxis yAxis = new VertAxis(this);
					String labelInfo = getParameter(Y_AXIS_INFO_PARAM);
					yAxis.readNumLabels(labelInfo);
				
				scatterPanel.add("Left", yAxis);
					
					theView = new ShowContrastView(data, this, xAxis, yAxis, null, "x", "y", "ls");
					theView.lockBackground(Color.white);
				
				scatterPanel.add("Center", theView);
				
		thePanel.add("Center", scatterPanel);
			
			XLabel yVariateName = new XLabel(yVar.name, XLabel.LEFT, this);
			yVariateName.setFont(yAxis.getFont());
			
		thePanel.add("North", yVariateName);
		
		return thePanel;
	}
	
	private XPanel contrastPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(10, 0));
		
			int nFixed = 0;
			while (true) {
				String fixedContrast = getParameter(CONTRAST_PARAM + (nFixed + 1));
				if (fixedContrast == null)
					break;
				nFixed ++;
			}
			
			if (nFixed > 0) {
				fixedContrasts = new NumValue[nFixed][];
				contrastChoice = new XChoice(this);
				contrastChoice.addItem("Custom contrast");
				
				for (int i=0 ; i<nFixed ; i++) {
					StringTokenizer st = new StringTokenizer(getParameter(CONTRAST_PARAM + (i + 1)), "#");
					contrastChoice.addItem(st.nextToken());
					
					StringTokenizer st2 = new StringTokenizer(st.nextToken());
					fixedContrasts[i] = new NumValue[st2.countTokens()];
					for (int j=0 ; j<fixedContrasts[i].length ; j++)
						fixedContrasts[i][j] = new NumValue(st2.nextToken());
				}
				thePanel.add("West", contrastChoice);
			}
			
		
			XPanel formulaPanel = new XPanel();
			formulaPanel.setLayout(new MultiRowLayout(2, 0, 0));
				
				MultipleRegnModel ls = (MultipleRegnModel)data.getVariable("ls");
				int nParams = ls.noOfParameters();
				contrastEdit = new XNumberEditPanel[nParams];
				for (int i=0 ; i<nParams ; i++) {
					String textAfter = "#beta##sub" + (i + 1) + "#";
					String textBefore = (i == 0) ? null : "+ ";
					textAfter = MText.expandText(textAfter);
					contrastEdit[i] = new XNumberEditPanel(textBefore, textAfter, null, 4, this);
					contrastEdit[i].setDoubleValue(kZeroValue);
					formulaPanel.add(contrastEdit[i]);
				}
				contrastEdit[0].disable();
		
		thePanel.add("Center", formulaPanel);
		
		return thePanel;
	}
	
	
	private XPanel bottomPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 10));
		
		thePanel.add(controlPanel(data));
		
			XPanel inferencePanel = new XPanel();
			GridBagLayout gbl = new GridBagLayout();
			inferencePanel.setLayout(gbl);
			
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.anchor = GridBagConstraints.CENTER;
				gbc.fill = GridBagConstraints.NONE;
				gbc.gridheight = gbc.gridwidth = 1;
				gbc.gridx = gbc.gridy = 0;
				gbc.insets = new Insets(0,0,0,0);
				gbc.ipadx = 10;
				gbc.ipady = 0;
				gbc.weightx = gbc.weighty = 0.0;
			
				XLabel testLabel = new XLabel(translate("Is contrast zero") + "?", XLabel.LEFT, this);
				testLabel.setFont(getBigBoldFont());
				testLabel.setForeground(kInferenceLabelColor);
				
			inferencePanel.add(testLabel);
			gbl.setConstraints(testLabel, gbc);
			
				XPanel testPanel = testPanel(data);
				
			inferencePanel.add(testPanel);
			gbc.gridx++;
			gbl.setConstraints(testPanel, gbc);
			
			
				XLabel ciLabel = new XLabel(translate("95% CI") + ":", XLabel.LEFT, this);
				ciLabel.setFont(getBigBoldFont());
				ciLabel.setForeground(kInferenceLabelColor);
				
			inferencePanel.add(ciLabel);
			gbc.gridx = 0;
			gbc.gridy = 2;
			gbl.setConstraints(ciLabel, gbc);
			
				XPanel ciPanel = ciPanel(data);
				
			inferencePanel.add(ciPanel);
			gbc.gridx++;
			gbl.setConstraints(ciPanel, gbc);
			
				Separator sep = new Separator(0.8, 10);
			
			inferencePanel.add(sep);
			gbc.gridx = 0;
			gbc.gridy = 1;
			gbc.gridwidth = 2;
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbl.setConstraints(sep, gbc);
			
		
		thePanel.add(inferencePanel);
		
		return thePanel;
	}
	
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 10));
		
		thePanel.add(contrastPanel(data));
			
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				XPanel innerPanel = new InsetPanel(10, 5);
				innerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
				
					calculateButton = new XButton(translate("Inference about contrast"), this);
				innerPanel.add(calculateButton);
					
				innerPanel.add(summaryPanel(data));
				
				innerPanel.lockBackground(kEstSeBackground);
				
			bottomPanel.add(innerPanel);
			
		thePanel.add(bottomPanel);
		
		return thePanel;
	}
	
	private XPanel summaryPanel(DataSet data) {
		XPanel estSePanel = new XPanel();
		estSePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 2));
	
			contrastValue = new FixedValueView(translate("Estimate"), maxParam, 0.0, this);
			contrastValue.addEqualsSign();
		estSePanel.add(contrastValue);
		
			maxSe = new NumValue(getParameter(MAX_SE_PARAM));
			seValue = new FixedValueView(translate("Standard error"), maxSe, 0.0, this);
			seValue.addEqualsSign();
		estSePanel.add(seValue);
		
		return estSePanel;
	}
	
	private XPanel testPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
		
		thePanel.add(tFormulaPanel());
		
			XPanel pValuePanel = new XPanel();
			pValuePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
				
				CatVariable xVar = (CatVariable)data.getVariable("x");
				df = xVar.noOfValues() - xVar.noOfCategories();
				FixedValueView dfValue = new FixedValueView("df", new NumValue(df, 0), df, this);
				dfValue.unboxValue();
				dfValue.addEqualsSign();
			pValuePanel.add(dfValue);
			
				pValueView = new FixedValueView(translate("p-value"), kMaxPValue, Double.NaN, this);
				pValueView.addEqualsSign();
			pValuePanel.add(pValueView);
			
		thePanel.add(pValuePanel);
		
		return thePanel;
	}
	
	private XPanel tFormulaPanel() {
		NumValue maxResult = new NumValue(999.999, 3);
		FormulaContext context = new FormulaContext(Color.black, getStandardFont(), this);
		
		contrastValue2 = new FixedValueView(null, maxParam, 12.34, this);
		contrastValue2.unboxValue();
		contrastValue2.setCenterValue(true);
		SummaryValue numer = new SummaryValue(contrastValue2, context);
		seValue2 = new FixedValueView(null, maxSe, 99.99, this);
		seValue2.unboxValue();
		seValue2.setCenterValue(true);
		SummaryValue denom = new SummaryValue(seValue2, context);
		
		Ratio tRatio = new Ratio(numer, denom, context);
		
		tPanel = new MainFormulaPanel("t =", tRatio, maxResult, context);
		tPanel.displayResult();
		return tPanel;
	}
	
	private XPanel ciPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(5, 0));
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_TOP, 2));
			
				LabelValue leftText = new LabelValue(MText.expandText(translate("est") + " #plusMinus# t#sub" + df + "# #times# " + translate("se")));
				FixedValueView leftFormula = new FixedValueView(null, leftText, leftText, this);
				leftFormula.unboxValue();
			
			leftPanel.add(leftFormula);
		
		thePanel.add("West", leftPanel);
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_TOP, 2));
			
				XPanel topPanel = new XPanel();
				topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 3, 0));
				
					contrastValue3 = new FixedValueView(null, maxParam, 12.34, this);
					contrastValue3.unboxValue();
					contrastValue3.setCenterValue(true);
					contrastValue3.addEqualsSign();
				topPanel.add(contrastValue3);
					
					double t = TTable.quantile(0.975, df);
					FixedValueView t95Value = new FixedValueView(MText.expandText("#plusMinus#"), new NumValue(t, 3), t, this);
					t95Value.unboxValue();
				topPanel.add(t95Value);
					
					seValue3 = new FixedValueView(MText.expandText("#times#"), maxSe, 99.99, this);
					seValue3.unboxValue();
					seValue3.setCenterValue(true);
				topPanel.add(seValue3);
			
			rightPanel.add(topPanel);
			
				IntervalValue dummyInterval = new IntervalValue(maxParam.toDouble(), maxParam.toDouble(), maxParam.decimals);
				ciValue = new FixedValueView(null, dummyInterval, dummyInterval, this);
				ciValue.addEqualsSign();
				ciValue.setCenterValue(true);
			rightPanel.add(ciValue);
		
		thePanel.add(rightPanel);
		
		return thePanel;
	}
	
	private void checkValidContrast() {
		boolean isValid = false;
		double sumCoeffs = 0;
		int decimals = 0;
		for (int i=1 ; i<contrastEdit.length ; i++) {
			NumValue coeff = contrastEdit[i].getNumValue();
			if (coeff.toDouble() != 0)
				isValid = true;
			sumCoeffs += coeff.toDouble();
			decimals = Math.max(decimals, coeff.decimals);
		}
		contrastEdit[0].setDoubleValue(new NumValue(-sumCoeffs, decimals));
		calculateButton.setEnabled(isValid);
	}
	
	private void doContrastInference(double[] contrast) {
		MultipleRegnModel fullModel = (MultipleRegnModel)data.getVariable("ls");
		double contrastVar = fullModel.getContrastVar("y", contrast);
		System.out.println("contrastVar = " + contrastVar);
		
		double contrastVar2 = fullModel.getContrastVar("y", contrast);
		System.out.println("contrastVar2 = " + contrastVar2);
		
		double contrastSe = Math.sqrt(contrastVar);
		seValue.setValue(contrastSe);
		seValue2.setValue(contrastSe);
		seValue3.setValue(contrastSe);
		
		double contrastSum = 0.0;
		for (int i=1 ; i<contrast.length ; i++)
			contrastSum += contrast[i] * fullModel.getParameter(i).toDouble();
		contrastValue.setValue(contrastSum);
		contrastValue2.setValue(contrastSum);
		contrastValue3.setValue(contrastSum);
		
		tPanel.displayResult();
		
		double t = contrastSum / contrastSe;
		double pValue = 2 * TTable.cumulative(-Math.abs(t), df);
		pValueView.setValue(pValue);
		
		double t95 = TTable.quantile(0.975, df);
		IntervalValue ciInterval = new IntervalValue(contrastSum, contrastSe, t95, maxParam.decimals);
		ciValue.setValue(ciInterval);
		
		theView.setContrast(contrast);
		theView.repaint();
		
		showingContrast = true;
	}
	
	private void clearContrastDisplay() {
		if (showingContrast) {
			seValue.clearValue();
			seValue2.setValue(Double.NaN);
			seValue3.setValue(Double.NaN);
			
			contrastValue.clearValue();
			contrastValue2.setValue(Double.NaN);
			contrastValue3.setValue(Double.NaN);
			pValueView.clearValue();
			ciValue.setValue(new LabelValue("? to ?"));
			
			theView.setContrast(null);
			theView.repaint();
			
			if (contrastChoice != null)
				contrastChoice.select(0);
				
			showingContrast = false;
		}
	}
	
	private void applyFixedContrast(int index) {
		double contrast[] = new double[contrastEdit.length];
		for (int i=0 ; i<contrastEdit.length ; i++) {
			NumValue c = fixedContrasts[index][i];
			contrastEdit[i].setDoubleValue(c);
			contrast[i] = c.toDouble();
		}
		doContrastInference(contrast);
	}
	
	private boolean localAction(Object target) {
		if (target == calculateButton) {
			double contrast[] = new double[contrastEdit.length];
			for (int i=0 ; i<contrastEdit.length ; i++)
				contrast[i] = contrastEdit[i].getDoubleValue();
			
			doContrastInference(contrast);
			return true;
		}
		else if (target == contrastChoice) {
			int contrastIndex = contrastChoice.getSelectedIndex();
			if (contrastIndex == 0) {
				clearContrastDisplay();
				checkValidContrast();
			}
			else
				applyFixedContrast(contrastIndex - 1);
			return true;
		}
		for (int i=0 ; i<contrastEdit.length ; i++)
			if (target == contrastEdit[i]) {
				clearContrastDisplay();
				checkValidContrast();
			}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}