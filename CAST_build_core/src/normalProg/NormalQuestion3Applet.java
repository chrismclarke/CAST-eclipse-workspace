package normalProg;

import java.awt.*;
import javax.swing.*;

import dataView.*;
import utils.*;
import normal.*;


public class NormalQuestion3Applet extends NormalQuestionApplet {
	protected int probDirection() {
		return NormalCalcView.BETWEEN;
	}
	
	protected XPanel rightPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 10));
		
		thePanel.add("Center", displayPanel(data));
		
		XPanel zPanel = new XPanel();
		zPanel.setLayout(new VerticalLayout(VerticalLayout.FILL));
		
		ZCalcCanvas zCalc = new ZCalcCanvas(data, this, (JTextField)meanEdit,
															(JTextField)sdEdit, (JTextField)xEdit, ZCalcCanvas.SUB_1);
		zCalc.setFont(calculationFont);
		zPanel.add(zCalc);
		
		ZCalcCanvas z2Calc = new ZCalcCanvas(data, this, (JTextField)meanEdit,
															(JTextField)sdEdit, (JTextField)x2Edit, ZCalcCanvas.SUB_2);
		z2Calc.setFont(calculationFont);
		zPanel.add(z2Calc);
		
		thePanel.add("North", zPanel);
		
		return thePanel;
	}
	
	protected void checkEditValues() {
		double z1 = getZ(xEdit);
		double z2 = getZ(x2Edit);
		double zMin = Math.min(z1, z2);
		double zMax = Math.max(z1, z2);
		data.setSelection("distn", zMin, zMax);
	}
}