package statisticProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import statistic.*;


abstract public class DragAxisApplet extends XApplet {
	static final protected String AXIS_INFO_PARAM = "horizAxis";
	static final protected String VALUE_1_PARAM = "value1";
	static final protected String VALUE_2_PARAM = "value2";
	
	protected DataSet data;
	
	protected DragValAxis theHorizAxis;
	protected Statistic lowStatistic, highStatistic, totalStatistic;
	protected DragLocationStatView theView;
	
	private XButton setValueButton[]= new XButton[2];
	private NumValue bestVal[] = new NumValue[2];
//	private XCheckbox showGraphCheck;
	
	public void setupApplet() {
		data = getData();
		
		setLayout(new BorderLayout());
			XPanel topPanel = getTopPanel();
		if (topPanel != null)
			add("North", topPanel);
		add("Center", displayPanel(data));
		add("South", controlPanel(data));
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		return data;
	}
	
	protected XPanel getTopPanel() {
		return null;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		theHorizAxis = new DragValAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		
		thePanel.add("Bottom", theHorizAxis);
		
		lowStatistic = getLowStatistic();
		highStatistic = getHighStatistic();
		totalStatistic = getTotalStatistic();
		
		theView = new DragLocationStatView(data, this, theHorizAxis, lowStatistic,
																														highStatistic, totalStatistic);
		thePanel.add("Center", theView);
		theView.lockBackground(Color.white);
		return thePanel;
	}
	
	protected void createButton(XPanel thePanel, String paramName, int index) {
		String buttonInfo = getParameter(paramName);
		if (buttonInfo == null)
			return;
		try {
			LabelEnumeration theValues = new LabelEnumeration(buttonInfo);
			String buttonName = (String)theValues.nextElement();
			bestVal[index] = new NumValue((String)theValues.nextElement());
			
			setValueButton[index] = new XButton(buttonName, this);
			thePanel.add(setValueButton[index]);
		} catch (Exception e) {
		}
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
		createButton(thePanel, VALUE_1_PARAM, 0);
		createButton(thePanel, VALUE_2_PARAM, 1);
		return thePanel;
	}
	
	abstract protected Statistic getLowStatistic();
	
	abstract protected Statistic getHighStatistic();
	
	abstract protected Statistic getTotalStatistic();

	
	private boolean localAction(Object target) {
		for (int i=0 ; i<setValueButton.length ; i++)
			if (setValueButton[i] != null && setValueButton[i] == target) {
				try {
					theHorizAxis.setAxisVal(bestVal[i]);
				} catch (AxisException e) {
					System.err.println("Value not inside axis.");
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