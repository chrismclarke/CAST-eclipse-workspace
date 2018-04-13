package factorial;

import java.awt.*;

import dataView.*;
import utils.*;

public class FactorialEffectPanel extends XPanel {
	static final public int VERTICAL = 0;
	static final public int COLUMNS = 1;
	
	private DataSet data;
	private MultiFactorModel model;
	private String yKey;
	private boolean alwaysLS;
	
	private XCheckbox[][] termCheck;
	
	public FactorialEffectPanel(DataSet data, String modelKey, boolean alwaysLS,
															String yKey, int orientation, String heading, XApplet applet) {
		this.data = data;
		model = (MultiFactorModel)data.getVariable(modelKey);
		String[][] allKeys = model.getTermKeys();
		this.yKey = yKey;
		this.alwaysLS = alwaysLS;
		
		termCheck = new XCheckbox[allKeys.length][];
		
		setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
		XPanel mainPanel = new XPanel();
		mainPanel.setLayout(new BorderLayout(0, 5));
		
		if (heading != null) {
			XLabel headingLabel = new XLabel(heading, XLabel.CENTER, applet);
			headingLabel.setFont(applet.getStandardBoldFont());
			headingLabel.setForeground(Color.blue);
			mainPanel.add("North", headingLabel);
		}
		
		XPanel addPanel = new XPanel();
		
		if (orientation == VERTICAL)
			addPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 2));
		else
			addPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
		
		mainPanel.add("Center", addPanel);
		
		for (int i=0 ; i<allKeys.length ; i++) {
			termCheck[i] = new XCheckbox[allKeys[i].length];
			for (int j=0 ; j<termCheck[i].length ; j++) {
				CatVariable term = (CatVariable)data.getVariable(allKeys[i][j]);
				termCheck[i][j] = new XCheckbox(term.name, applet);
			}
			if (orientation == VERTICAL)
				for (int j=0 ; j<termCheck[i].length ; j++)
					addPanel.add(termCheck[i][j]);
			else {
				XPanel columnPanel = new XPanel();
				columnPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 2));
				for (int j=0 ; j<termCheck[i].length ; j++)
					columnPanel.add(termCheck[i][j]);
				addPanel.add(columnPanel);
			}
		}
		add(mainPanel);
		
		enableKeys();
	}
	
	private void enableKeys() {
		int[][] activeKeys = model.getActiveKeys();
		for (int i=0 ; i<activeKeys.length ; i++)
			for (int j=0 ; j<activeKeys[i].length ; j++) {
				XCheckbox check = termCheck[i][j];
				if (activeKeys[i][j] <= FactorialTerms.OFF_ENABLED) {
					check.setState(false);
					if (activeKeys[i][j] == FactorialTerms.OFF_ENABLED)
						check.enable();
					else
						check.disable();
				}
				else {
					check.setState(true);
					if (activeKeys[i][j] == FactorialTerms.ON_ENABLED)
						check.enable();
					else
						check.disable();
				}
			}
	}
	
	private boolean localAction(Object target) {
		for (int i=0 ; i<termCheck.length ; i++)
			for (int j=0 ; j<termCheck[i].length ; j++)
				if (target == termCheck[i][j]) {
					model.activateTerm(i, j, termCheck[i][j].getState());
					if (alwaysLS)
						model.updateLSParams(yKey);
					enableKeys();
					data.variableChanged("model");
		
//					SSComponent[] comp = model.getBestSsqComponents(yKey);
//					for (int k=0 ; k<comp.length ; k++)
//						System.out.println("Comp " + k + ": ssq = " + comp[k].ssq + " (" + comp[k].df + "df)");
//					System.out.println("");
					return true;
				}
		
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}
