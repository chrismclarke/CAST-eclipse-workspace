package normalProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import distn.*;
import coreVariables.*;
import formula.*;
import imageUtils.*;


import normal.*;


public class ZScoreInvProbApplet extends ZScoreApplet {
	static final private String MAX_X_PARAM = "maxX";

	static final private String kStdNormalAxis = "-3.5 3.5 -3 1";
	
	private StdNormalView zView;
	
	protected int verticalGap() {
		return 5;
	}
	
	protected NumValue getMaxX(NumValue minSliderValue, NumValue maxSliderValue) {
		return new NumValue(getParameter(MAX_X_PARAM));
	}
	
	protected DataSet getData() {
		DataSet data = super.getData();
		
		int probDecimals = xSlider.getParameter().decimals;
		TailAreaVariable cumProb = new TailAreaVariable("Cum prob", data, "z", "zDistn",
																								TailAreaVariable.LOWER_TAIL, probDecimals);
		
		data.addVariable("cumProb", cumProb);
		
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.6, 10, ProportionLayout.VERTICAL));
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new BorderLayout(0, 10));
			
				XPanel zPanel = new XPanel();
				zPanel.setLayout(new AxisLayout());
		
					HorizAxis theHorizAxis = new HorizAxis(this);
					theHorizAxis.readNumLabels(kStdNormalAxis);
					theHorizAxis.setAxisName(translate("z-score"));
					theHorizAxis.setFont(getBigFont());
				zPanel.add("Bottom", theHorizAxis);
				
					zView = creatStdNormView(data, theHorizAxis);
				zPanel.add("Center", zView);
		
			bottomPanel.add("Center", zPanel);
			
				XPanel zScorePanel = new XPanel();
				zScorePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 5));
				zScorePanel.add(zScorePanel(data));
				zScorePanel.add(new ImageCanvas("greenDownArrow.png", this));
			
			bottomPanel.add("South", zScorePanel);
			
		thePanel.add(ProportionLayout.TOP, bottomPanel);
		
		thePanel.add(ProportionLayout.BOTTOM, super.displayPanel(data));
		
		return thePanel;
	}
	
	protected XPanel zScorePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			XPanel innerPanel = new InsetPanel(10, 4);
			innerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				FormulaContext bigContext = new FormulaContext(Color.black, getBigFont(), this);
				ZInvProbFormulaPanel formula = new ZInvProbFormulaPanel(data, "z", "cumProb", bigContext);
				formula.setFont(getBigFont());
			innerPanel.add(formula);
		
			innerPanel.lockBackground(kFormulaBackground);
		thePanel.add(innerPanel);
		return thePanel;
	}
	
	protected XPanel formulaPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			XPanel innerPanel = new InsetPanel(10, 4);
			innerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				FormulaContext bigContext = new FormulaContext(Color.black, getBigFont(), this);
				ZInvFormulaPanel formula = new ZInvFormulaPanel(data, "x", "z", maxX, mean, sd,
																																					bigContext);
			innerPanel.add(formula);
		
			innerPanel.lockBackground(kFormulaBackground);
		thePanel.add(innerPanel);
		return thePanel;
	}
	
	protected void setZValue(double z) {
		super.setZValue(z);
		if (zView != null)
			zView.setTopZValue(z);
	}
	
	protected void setValuesFromSlider(ParameterSlider xSlider) {
		double prob = xSlider.getParameter().toDouble();		//	really slider for probability
		
		double z = NormalTable.quantile(prob);
		double x = mean.toDouble() + z * sd.toDouble();
		
		setZValue(z);
		
		NumVariable xVar = (NumVariable)data.getVariable("x");
		NumValue x0Value = (NumValue)xVar.valueAt(0);
		x0Value.setValue(x);
		
		data.valueChanged(0);
	}
}