package percentileProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import formula.*;


import percentile.*;


public class GeomMeanApplet extends DragCumPercentApplet {
	
	static final private Color kPredictBackground = new Color(0xDDDDFF);
	
	private SimplePropnFormulaPanel meanFormula;
	private GeomMean2FormulaPanel meanFormula2;
	private GeomMean3FormulaPanel meanFormula3;
	
	protected XPanel topPanel(DataSet data) {
		return propnSidePanel(data);
	}
	
	protected VertAxis getVertAxis() {
		VertAxis vertAxis = new VertAxis(this);
		vertAxis.readNumLabels(kZeroOneAxis);
		vertAxis.setForeground(Color.red);
		return vertAxis;
	}
	
	protected XPanel propnCalcPanel(DataSet data, DataSet referenceData) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 5));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				NumValue maxY = new NumValue(getParameter(MAX_VALUE_PARAM));
				FormulaContext boldContext = new FormulaContext(Color.black, getStandardBoldFont(), this);
				formula = new PropnFormulaPanel(data, "y", "ref", referenceData, maxY,
																							PropnRangeView.LESS_THAN, boldContext);
			topPanel.add(formula);
		
		thePanel.add("North", topPanel);
		
			XPanel centerPanel = new InsetPanel(6, 6);
			centerPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																												VerticalLayout.VERT_CENTER, 10));
				
				meanFormula = new GeomMeanFormulaPanel(data, "y", "ref", referenceData, maxY,
																									PropnRangeView.LESS_THAN, boldContext);
			centerPanel.add(meanFormula);
				
				XPanel bottomPanel = new InsetPanel(6, 2);
				bottomPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
																														VerticalLayout.VERT_CENTER, 3));
					
					meanFormula2 = new GeomMean2FormulaPanel(data, "y", "ref", referenceData, maxY,
																									PropnRangeView.LESS_THAN, boldContext);
				bottomPanel.add(meanFormula2);
					
					meanFormula3 = new GeomMean3FormulaPanel(data, "y", "ref", referenceData, maxY,
																									PropnRangeView.LESS_THAN, boldContext);
				bottomPanel.add(meanFormula3);
				
				bottomPanel.lockBackground(Color.white);
			centerPanel.add(bottomPanel);
			
			centerPanel.lockBackground(kPredictBackground);
		thePanel.add("Center", centerPanel);
		return thePanel;
	}
	
	protected void changeInequality(int inequality) {
		super.changeInequality(inequality);
		meanFormula.setInequality(inequality);
		meanFormula2.setInequality(inequality);
		meanFormula3.setInequality(inequality);
	}
}