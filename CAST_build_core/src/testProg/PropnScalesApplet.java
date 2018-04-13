package testProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import cat.*;
import test.*;
import qnUtils.*;


class CountSlider extends XSlider {
	public CountSlider(int lowCount, int highCount, int startCount, String successString,
																						XApplet applet) {
		super(null, null, applet.translate("Count") + " (" + successString + ") = ", lowCount, highCount, startCount, applet);
	}
	
	protected Value translateValue(int val) {
		return new NumValue(val, 0);
	}
	
	protected int getMaxValueWidth(Graphics g) {
		return g.getFontMetrics().stringWidth(String.valueOf(getMaxValue()));
	}
}


public class PropnScalesApplet extends MeanScalesApplet {
	static final private String COUNTS_PARAM = "counts";
	
	private int lowCount, highCount, startCount, totalCount;
	private CountSlider countSlider;
	
	protected int testParameter() {
		return HypothesisTest.PROPN_2;
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		CatVariable y = new CatVariable(getParameter(VAR_NAME_PARAM), Variable.USES_REPEATS);
		y.readLabels(getParameter(CAT_LABELS_PARAM));
		int counts[] = new int[y.noOfCategories()];
		counts[0] = startCount;
		counts[1] = totalCount - startCount;
		y.setCounts(counts);
		
		data.addVariable("y", y);
		
		return data;
	}
	
	protected void readShiftRange() {
		StringTokenizer st = new StringTokenizer(getParameter(COUNTS_PARAM));
		
		lowCount = Integer.parseInt(st.nextToken());
		highCount = Integer.parseInt(st.nextToken());
		startCount = Integer.parseInt(st.nextToken());
		totalCount = Integer.parseInt(st.nextToken());
	}
	
	protected XPanel dataPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		thePanel.add("Center", dataPlotPanel(data));
		
		CatVariable y = (CatVariable)data.getVariable("y");
		
		countSlider = new CountSlider(lowCount, highCount, startCount, y.getLabel(0).toString(), this);
		thePanel.add("South", countSlider);
		
		return thePanel;
	}
	
	private XPanel dataPlotPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		thePanel.add("West", dataTablePanel(data, "y"));
		thePanel.add("Center", new PieTestPropnView(data, this, "y", CatDataView.SELECT_ONE, test));
		
		return thePanel;
	}
	
	private XPanel dataTablePanel(DataSet data, String variableKey) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 2));
		
			CatVariable v = (CatVariable)data.getVariable(variableKey);
			XLabel varName = new XLabel(v.name, XLabel.CENTER, this);
			varName.setFont(getStandardBoldFont());
			
		thePanel.add(varName);
		
			XPanel tablePanel = new InsetPanel(5, 3);
			tablePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				FreqTableView tableView = new FreqTableView(data, this, variableKey, CatDataView.SELECT_ONE, 4);
				
			tablePanel.add(tableView);
		
			tablePanel.lockBackground(Color.white);
		thePanel.add(tablePanel);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == countSlider) {
			CatVariable yVariable = (CatVariable)data.getVariable("y");
			int count[] = new int[yVariable.noOfCategories()];
			count[0] = countSlider.getValue();
			count[1] = totalCount - count[0];
			yVariable.setCounts(count);
			data.variableChanged("y");
			if (axisLabelType == PValueAxis.P_VALUE_TEXT)
				axis.repaint();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
}