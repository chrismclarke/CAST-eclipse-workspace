package statisticProg;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;

import statistic.*;


public class BalanceApplet extends DragAxisApplet {
	static final protected String DATA_NAMES_PARAM = "dataNames";
	
	private String dataNames[] = null;
	
	private BalanceView theView;
	private XButton setMeanButton;
	
	private XChoice dataSetChoice;
	private int currentDataSetIndex = 0;
	
	protected XPanel getTopPanel() {
		if (dataNames == null)
			return null;
		
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			dataSetChoice = new XChoice(translate("Data set") + ":", XChoice.HORIZONTAL, this);
			for (int i=0 ; i<dataNames.length ; i++)
				dataSetChoice.addItem(dataNames[i]);
		thePanel.add(dataSetChoice);
		
		return thePanel;
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		
		String dataNameString = getParameter(DATA_NAMES_PARAM);
		if (dataNameString != null) {
			StringTokenizer st = new StringTokenizer(dataNameString, "*");
			dataNames = new String[st.countTokens()];
			for (int i=0 ; i<dataNames.length ; i++)
				dataNames[i] = st.nextToken();
			
			for (int i=2 ; i<=dataNames.length ; i++)
				data.addNumVariable("y" + i, getParameter(VAR_NAME_PARAM + i), getParameter(VALUES_PARAM + i));
		}
		
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			theHorizAxis = new DragValAxis(this);
			String labelInfo = getParameter(AXIS_INFO_PARAM);
			theHorizAxis.readNumLabels(labelInfo);
			NumVariable yVar = (NumVariable)data.getVariable("y");
			theHorizAxis.setAxisName(yVar.name);
		
		thePanel.add("Bottom", theHorizAxis);
		
			theView = new BalanceView(data, this, theHorizAxis);
			theView.setActiveNumVariable("y");
			theView.lockBackground(Color.white);
			if (yVar.noOfValues() > 100)
				theView.setCrossSize(DataView.SMALL_CROSS);
			
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected Statistic getLowStatistic() {
		return null;
	}
	
	protected Statistic getHighStatistic() {
		return null;
	}
	
	protected Statistic getTotalStatistic() {
		return null;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 4));
			
			StringTokenizer st = new StringTokenizer(translate("Set k*mean"), "*");
			setMeanButton = new XButton(st.nextToken() + " = " + st.nextToken(), this);
			
		thePanel.add(setMeanButton);
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == setMeanButton) {
			String yKey = "y";
			if (currentDataSetIndex > 0)
				yKey += (currentDataSetIndex + 1);
			NumVariable y = (NumVariable)data.getVariable(yKey);
			
			ValueEnumeration ye = y.values();
			double sy = 0.0;
			while (ye.hasMoreValues())
				sy += ye.nextDouble();
			
			try {
				NumValue oldAxisVal = theHorizAxis.getAxisVal();
				theHorizAxis.setAxisVal(new NumValue(sy / y.noOfValues(), oldAxisVal.decimals));
			} catch (AxisException e) {
				System.err.println("Value not inside axis.");
			}
			return true;
		}
		else if (target == dataSetChoice) {
			int newChoice = dataSetChoice.getSelectedIndex();
			if (newChoice != currentDataSetIndex) {
				currentDataSetIndex = newChoice;
				
				String yKey = "y";
				String axisParam = AXIS_INFO_PARAM;
				if (newChoice > 0) {
					yKey += (newChoice + 1);
					axisParam += (newChoice + 1);
				}
				
				theHorizAxis.readNumLabels(getParameter(axisParam));
				NumVariable yVar = (NumVariable)data.getVariable(yKey);
				theHorizAxis.setAxisName(yVar.name);
				try {
					theHorizAxis.setAxisValPos(theHorizAxis.numValToRawPosition((theHorizAxis.maxOnAxis + theHorizAxis.minOnAxis * 2) / 3));
				} catch (AxisException e) {
					System.err.println("Value not inside axis.");
				}
				
				((BalanceView)theView).setActiveNumVariable(yKey);
				
				theHorizAxis.invalidate();
				theView.invalidate();
				validate();
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