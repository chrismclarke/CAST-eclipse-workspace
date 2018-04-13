package catProg;

import java.awt.*;

import dataView.*;
import utils.*;
import imageUtils.*;

import cat.*;


public class CreateFreqTableApplet extends XApplet {
	static final private String NO_OF_COLS_PARAM = "noOfCols";
	
	protected CatValueListView theList;
	
	protected XButton resetButton, completeButton;
	
	public void setupApplet() {
		DataSet data = readData();
		
		setLayout(new BorderLayout(10, 0));
		
			XPanel freqTablePanel = new XPanel();
			freqTablePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				CoreCreateTableView freqTable = createTableView(data);
			freqTablePanel.add(freqTable);
			
		add("East", freqTablePanel);
		
		add("West", valueListPanel(data, freqTable));
			
			XPanel arrowPanel = new XPanel();
			arrowPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			String arrowGif = "redArrowRight.png";
			arrowPanel.add(new ImageCanvas(arrowGif, this));
			
		add("Center", arrowPanel);
		
		add("South", controlPanel());
	}
	
	protected String getMainCatVariableKey() {
		return "y";
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		data.addCatVariable("y", getParameter(CAT_NAME_PARAM), getParameter(CAT_VALUES_PARAM),
											getParameter(CAT_LABELS_PARAM));
		return data;
	}
	
	protected CoreCreateTableView createTableView(DataSet data) {
		return new CreateFreqTableView(data, this, getMainCatVariableKey());
	}
	
	protected XPanel valueListPanel(DataSet data, CoreCreateTableView freqTable) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
			
			CatVariable catVar = (CatVariable)data.getVariable(getMainCatVariableKey());
			XLabel varName = new XLabel(catVar.name, XLabel.CENTER, this);
			varName.setFont(getStandardBoldFont());
		
		thePanel.add(varName);
		
			int nCols = Integer.parseInt(getParameter(NO_OF_COLS_PARAM));
			theList = new CatValueListView(data, this, getMainCatVariableKey(), freqTable, nCols);
			theList.lockBackground(Color.white);
		
		thePanel.add(theList);
		return thePanel;
	}
	
	private XPanel controlPanel() {
		XPanel thePanel = new InsetPanel(0, 10, 0, 0);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 0));
		
			resetButton = new XButton(translate("Reset"), this);
			resetButton.disable();
		thePanel.add(resetButton);
		
			completeButton = new XButton(translate("Complete table"), this);
			completeButton.disable();
		thePanel.add(completeButton);
		
		return thePanel;
	}
	
	protected int numberCompleted() {
		return theList.numberCompleted();
	}
	
	public void notifyDataChange(DataView theView) {
		int nCompleted = numberCompleted();
		if (nCompleted == 0)
			resetButton.disable();
		else
			resetButton.enable();
		if (nCompleted < 5)
			completeButton.disable();
		else
			completeButton.enable();
	}
	
	protected void doReset() {
		if (theList != null)
			theList.resetList();
		resetButton.disable();
		completeButton.disable();
	}
	
	protected void doCompleteTable() {
		if (theList != null)
			theList.completeTable();
		completeButton.disable();
	}
	
	private boolean localAction(Object target) {
		if (target == resetButton) {
			doReset();
			return true;
		}
		else if (target == completeButton) {
			doCompleteTable();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}