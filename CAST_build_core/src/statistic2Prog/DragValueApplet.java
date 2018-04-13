package statistic2Prog;

import java.awt.*;
import java.util.*;

import axis.*;
import dataView.*;
import utils.*;

import statistic2.*;


public class DragValueApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String DECIMALS_PARAM = "decimals";
	static final private String ALLOW_DRAG_PARAM = "allowDrag";			//		default true
	static final private String SHOW_VAR_NAME_PARAM = "showVarName";	//		default false
	static final private String DRAG_MESSAGES_PARAM = "dragMessages";
	static final protected String DATA_NAMES_PARAM = "dataNames";
	
	private String dragInstruction, draggedMessage;
	
	private DataSet data;
	private String dataNames[] = null;
	
	private boolean allowDrag;
	
	private HorizAxis theHorizAxis;
	private DragValueMeanMedianView theView;
	
	private XButton resetButton;
	private XLabel valuesChangedLabel;
	
	private XChoice dataSetChoice;
	private int currentDataSetIndex = 0;
	
	private boolean valuesChanged = false;
	
	public void setupApplet() {
		data = getData();
		
		String allowDragString = getParameter(ALLOW_DRAG_PARAM);
		allowDrag = (allowDragString == null) || allowDragString.equals("true");
		
		setLayout(new BorderLayout(0, 10));
		
			XPanel dataChoicePanel = getTopPanel();
			if (dataChoicePanel != null)
				add("North", dataChoicePanel);
		
		add("Center", displayPanel(data));
		if (allowDrag)
			add("South", controlPanel());
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
		data.variableChanged("y");		//	to make sure that selection (i.e. Flags) uses correct number of values
		
		return data;
	}
	
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
	
	private XPanel controlPanel() {
		XPanel thePanel = new InsetPanel(20, 0);
		thePanel.setLayout(new BorderLayout(0, 0));
		
			resetButton = new XButton(translate("Reset"), this);
			resetButton.disable();
		thePanel.add("East", resetButton);
		
			StringTokenizer st = new StringTokenizer(getParameter(DRAG_MESSAGES_PARAM), "*");
			dragInstruction = st.nextToken();
			draggedMessage = st.nextToken();
			
			valuesChangedLabel = new XLabel(dragInstruction, XLabel.LEFT, this);
			valuesChangedLabel.setFont(getBigBoldFont());
			valuesChangedLabel.setForeground(Color.gray);
		thePanel.add("Center", valuesChangedLabel);
		
		return thePanel;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			NumVariable yVar = (NumVariable)data.getVariable("y");
			theHorizAxis = new HorizAxis(this);
			String labelInfo = getParameter(AXIS_INFO_PARAM);
			theHorizAxis.readNumLabels(labelInfo);
			String showVarNameString = getParameter(SHOW_VAR_NAME_PARAM);
			boolean showVarName = (showVarNameString != null) && showVarNameString.equals("true");
			if (showVarName)
				theHorizAxis.setAxisName(yVar.name);
		thePanel.add("Bottom", theHorizAxis);
		
			int decimals = Integer.parseInt(getParameter(DECIMALS_PARAM));
			theView = new DragValueMeanMedianView(data, this, theHorizAxis, decimals);
			theView.setActiveNumVariable("y");
			theView.lockBackground(Color.white);
			if (yVar.noOfValues() > 100)
				theView.setCrossSize(DataView.SMALL_CROSS);
			if (!allowDrag)
				theView.setAllowDrag(false);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	public void notifyDataChange(DataView theView) {
		if (!valuesChanged) {
			valuesChanged = true;
			resetButton.enable();
			valuesChangedLabel.setText(draggedMessage);
		}
	}

	
	private boolean localAction(Object target) {
		if (target == resetButton) {
			valuesChanged = false;
			
			resetButton.disable();
			valuesChangedLabel.setText(dragInstruction);
			
			String yKey = "y";
			String valuesParam = VALUES_PARAM;
			if (currentDataSetIndex > 0) {
				yKey += (currentDataSetIndex + 1);
				valuesParam += (currentDataSetIndex + 1);
			}
			NumVariable yVar = (NumVariable)data.getVariable(yKey);
			yVar.readValues(getParameter(valuesParam));
			theView.resetValues();
			data.variableChanged(yKey);
			
			return true;
		}
		else if (target == dataSetChoice) {
			int newChoice = dataSetChoice.getSelectedIndex();
			if (newChoice != currentDataSetIndex) {
				currentDataSetIndex = newChoice;
				
				String yKey = "y";
				String axisParam = AXIS_INFO_PARAM;
				String decimalsParam = DECIMALS_PARAM;
				if (newChoice > 0) {
					yKey += (newChoice + 1);
					axisParam += (newChoice + 1);
					decimalsParam += (newChoice + 1);
				}
				
				theHorizAxis.readNumLabels(getParameter(axisParam));
				NumVariable yVar = (NumVariable)data.getVariable(yKey);
				theHorizAxis.setAxisName(yVar.name);
				
				int decimals = Integer.parseInt(getParameter(decimalsParam));
				theView.setMeanMedianDecimals(decimals);
				theView.setActiveNumVariable(yKey);
				
				data.variableChanged(yKey);
				
				resetButton.disable();
				valuesChangedLabel.setText(dragInstruction);
				
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