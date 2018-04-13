package normalProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import coreVariables.*;
import formula.*;
import imageUtils.*;


import normal.*;


public class ZScoreProbApplet extends ZScoreApplet {
	static final private String kStdNormalAxis = "-3.5 3.5 -3 1";
	static final private int kProbDecimals = 4;
	
	private StdNormalView zView;
	
	protected int verticalGap() {
		return 5;
	}
	
	protected DataSet getData() {
		DataSet data = super.getData();
		
		TailAreaVariable cumProb = new TailAreaVariable("Cum prob", data, "z", "zDistn",
																								TailAreaVariable.LOWER_TAIL, kProbDecimals);
		
		data.addVariable("cumProb", cumProb);
		
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.37, 10, ProportionLayout.VERTICAL));
		
		thePanel.add(ProportionLayout.TOP, super.displayPanel(data));
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new BorderLayout(0, 10));
			
				XPanel zScorePanel = new XPanel();
				zScorePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 5));
				zScorePanel.add(zScoreFormulaPanel(data));
				zScorePanel.add(new ImageCanvas("greenDownArrow.png", this));
			
			bottomPanel.add("North", zScorePanel);
			
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
			
		thePanel.add(ProportionLayout.BOTTOM, bottomPanel);
		
		return thePanel;
	}
	
	protected XPanel zScoreFormulaPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			XPanel innerPanel = new InsetPanel(10, 4);
			innerPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				FormulaContext bigContext = new FormulaContext(Color.black, getBigFont(), this);
				ZFormulaPanel formula = new ZFormulaPanel(data, "x", "z", maxX, mean, sd, bigContext);
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
				ZProbFormulaPanel formula = new ZProbFormulaPanel(data, "x", "z", "cumProb", maxX, bigContext);
			innerPanel.add(formula);
		
			innerPanel.lockBackground(kFormulaBackground);
		thePanel.add(innerPanel);
		return thePanel;
	}
	
	protected void setZValue(double z) {
		super.setZValue(z);
		zView.setTopZValue(z);
	}
}