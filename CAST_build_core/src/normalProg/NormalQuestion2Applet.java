package normalProg;

import java.awt.*;
import javax.swing.*;

import dataView.*;
import normal.*;


public class NormalQuestion2Applet extends NormalQuestionApplet {
	protected XPanel answerPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		NormalCalcView calc = new NormalCalcView(data, "distn", this,
										(JTextField)meanEdit, (JTextField)sdEdit, (JTextField)xEdit, null, NormalCalcView.ABOVE);
		calc.setFont(calculationFont);
		thePanel.add(calc);
		
		return thePanel;
	}
	
	protected void checkEditValues() {
		double z = getZ(xEdit);
		data.setSelection("distn", z, Double.POSITIVE_INFINITY);
	}
}