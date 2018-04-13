package stdErrorProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import distn.*;

import distribution.*;
import stdError.*;


public class PropnBiasSeApplet extends XApplet {
	static final private String N_PARAM = "n";
	static final private String P_PARAM = "p";
	static final private String COUNT_NAME_PARAM = "countName";
	static final private String PROPN_NAME_PARAM = "propnName";
	static final private String ERROR_NAME_PARAM = "errorName";
	static final private String COUNT_AXIS_PARAM = "countAxis";
	static final private String PROPN_AXIS_PARAM = "propnAxis";
	static final private String ERROR_AXIS_PARAM = "errorAxis";
	static final private String COUNT_DECIMALS_PARAM = "countDecimals";
	static final private String PROPN_DECIMALS_PARAM = "propnDecimals";
	static final private String SHOW_MEAN_SD_PARAM = "showMeanSd";
	
	private int n;
	private double p;
	
	private boolean showCounts;
	
	private MultiHorizAxis countProbAxis;
	private ErrorPropnMeanSdView meanSDView;
	
	private XChoice axisChoice;
	private int currentAxisChoice = 0;
	
	public void setupApplet() {
		DataSet data = createData();
		
		showCounts = getParameter(COUNT_NAME_PARAM) != null;
		
		setLayout(new BorderLayout(20, 0));
		add("Center", barchartPanel(data));
		
			String showMeanSEString = getParameter(SHOW_MEAN_SD_PARAM);
			if (showMeanSEString.equals("true")) {
				XPanel meanSDPanel = new XPanel();
				meanSDPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				
					int countDecimals = Integer.parseInt(getParameter(COUNT_DECIMALS_PARAM));
					int propnDecimals = Integer.parseInt(getParameter(PROPN_DECIMALS_PARAM));
					meanSDView = new ErrorPropnMeanSdView(data, this, "distn",
									showCounts ? ErrorPropnMeanSdView.COUNT : ErrorPropnMeanSdView.PROPN, countDecimals,
									propnDecimals);
					meanSDView.setForeground(Color.blue);
					meanSDView.setFont(getBigBoldFont());
					
				meanSDPanel.add(meanSDView);
				
				add("East", meanSDPanel);
			}
	}
	
	private DataSet createData() {
		DataSet data = new DataSet();
		
		BinomialDistnVariable yDistn = new BinomialDistnVariable("y");
		n = Integer.parseInt(getParameter(N_PARAM));
		yDistn.setCount(n);
		p = Double.parseDouble(getParameter(P_PARAM));
		yDistn.setProb(p);
		yDistn.setMinSelection(n * p - 0.5);
		yDistn.setMaxSelection(n * p + 0.5);
		data.addVariable("distn", yDistn);
		
		return data;
	}
	
	
	private XPanel barchartPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XPanel barPanel = new XPanel();
			barPanel.setLayout(new AxisLayout());
			
				countProbAxis = new MultiHorizAxis(this, 3);
				countProbAxis.setChangeMinMax(false);
				countProbAxis.readNumLabels(getParameter(COUNT_AXIS_PARAM));
				countProbAxis.readExtraNumLabels(getParameter(PROPN_AXIS_PARAM));
				countProbAxis.readExtraNumLabels(getParameter(ERROR_AXIS_PARAM));
				if (!showCounts)
					countProbAxis.setStartAlternate(1);
			barPanel.add("Bottom", countProbAxis);
			
				DiscreteProbView barChart = new DiscreteProbView(data, this, "distn", null, null,
																												countProbAxis, DiscreteProbView.NO_DRAG);
				barChart.lockBackground(Color.white);
			barPanel.add("Center", barChart);
		
		thePanel.add("Center", barPanel);
		
			XPanel choicePanel = new XPanel();
			choicePanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
			
				axisChoice = new XChoice(this);
				if (showCounts)
					axisChoice.addItem(getParameter(COUNT_NAME_PARAM));
				axisChoice.addItem(getParameter(PROPN_NAME_PARAM));
				axisChoice.addItem(getParameter(ERROR_NAME_PARAM));
				
			choicePanel.add(axisChoice);
			
		thePanel.add("South", choicePanel);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == axisChoice) {
			int newChoice = axisChoice.getSelectedIndex();
			if (!showCounts)
				newChoice ++;
			if (newChoice != currentAxisChoice) {
				currentAxisChoice = newChoice;
				countProbAxis.setAlternateLabels(newChoice);
				countProbAxis.repaint();
				
				if (meanSDView != null) {
					meanSDView.setStatisticType(newChoice);
					meanSDView.repaint();
				}
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}