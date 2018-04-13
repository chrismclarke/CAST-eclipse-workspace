package multivarProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import coreGraphics.*;

import multivar.*;


public class ZSymbolApplet extends ScatterApplet {
	static final protected String Z_VAR_NAME_PARAM = "zVarName";
	static final protected String Z_VALUES_PARAM = "zValues";
	
	static final protected String Z_KEY_PARAM = "zKey";
	
	private XChoice symbolChoice;
	private int selectedChoice;
	
	private ZSymbolScatterView theView;
	private SymbolKey theKey;
	
	protected DataSet readData() {
		DataSet data = super.readData();
		data.addNumVariable("z", getParameter(Z_VAR_NAME_PARAM), getParameter(Z_VALUES_PARAM));
		return data;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		symbolChoice = new XChoice(this);
		symbolChoice.addItem(translate("Size"));
		symbolChoice.addItem(translate("Colour"));
		symbolChoice.addItem(translate("Angle"));
		symbolChoice.select(0);
		selectedChoice = 0;
		thePanel.add(symbolChoice);
		return thePanel;
	}
	
	protected DataView createDataView(DataSet data, HorizAxis theHorizAxis,
																	VertAxis theVertAxis) {
		theView = new ZSymbolScatterView(data, this, theHorizAxis, theVertAxis, "x", "y", "z",
																																		ZSymbolScatterView.CIRCLES);
		return theView;
	}
	
	protected XPanel rightPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
		theKey = new SymbolKey(data, "z", getParameter(Z_KEY_PARAM), theView, this);
		theKey.setFont(getStandardFont());
		theKey.setForeground(Color.blue);
		thePanel.add(theKey);
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == symbolChoice) {
			if (selectedChoice != symbolChoice.getSelectedIndex()) {
				selectedChoice = symbolChoice.getSelectedIndex();
				theView.setZSymbol(selectedChoice == 0 ? ZSymbolScatterView.CIRCLES
									: selectedChoice == 1 ? ZSymbolScatterView.COLOURS
									: ZSymbolScatterView.ANGLES);
				theKey.repaint();
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