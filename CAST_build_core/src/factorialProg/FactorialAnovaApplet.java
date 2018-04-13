package factorialProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import graphics3D.*;

import factorial.*;


public class FactorialAnovaApplet extends DragFactorialApplet {
	static final protected String MAX_SSQ_PARAM = "maxSsq";
	
	protected XPanel eastPanel(DataSet data) {
		XPanel thePanel = RotateButton.createRotationPanel(theView, this, RotateButton.VERTICAL);
			rotateButton = new XButton(translate("Spin"), this);
		thePanel.add(rotateButton);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 6, 0, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
		
		StringTokenizer st = new StringTokenizer(getParameter(MAX_SSQ_PARAM));
		NumValue maxSsq = new NumValue(st.nextToken());
		@SuppressWarnings("unused")
		int maxDF = Integer.parseInt(st.nextToken());			//	not used but we need to pass over token
		FactorialAnovaTableView anovaTable;
		
		if (st.hasMoreTokens()) {
			NumValue maxMsq = new NumValue(st.nextToken());
			NumValue maxF = new NumValue(st.nextToken());
		
			anovaTable = new FactorialAnovaTableView(data, this, maxSsq, maxMsq, maxF,
											FactorialAnovaTableView.SSQ_F_PVALUE, "model", "y");
		}
		else
			anovaTable = new FactorialAnovaTableView(data, this, maxSsq, null, null,
											FactorialAnovaTableView.SSQ_AND_DF, "model", "y");
		thePanel.add("Center", anovaTable);
		
		return thePanel;
	}
}