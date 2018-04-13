package dynamicProg;

import java.awt.*;

import dataView.*;
import utils.*;

import dynamic.*;


public class CatColoursApplet extends XApplet {
//	static final private Color kGray1 = new Color(0x444444);
//	static final private Color kGray2 = new Color(0x888888);
//	static final private Color kGray3 = new Color(0xBBBBBB);
	
	static final private Color kColorScheme[][] =
			{{Color.red, Color.yellow, Color.blue, Color.green, Color.magenta, Color.gray, Color.cyan, Color.orange, Color.pink, Color.darkGray, Color.lightGray},
				{Color.black, Color.gray, Color.darkGray, Color.white, Color.black, Color.gray, Color.darkGray, Color.white, Color.black, Color.gray, Color.darkGray, Color.white},
				new Color[7]};
	static {
		for (int i=0 ; i<7 ;i++) {
			int shade = 255 * i / 6;
			kColorScheme[2][i] = new Color(shade, shade, shade);
		}
	}
	
	private DataSet data;
	
	private PieSizeView thePieChart;
	private SimpleTableView freqTable;
	
	private XChoice colorChoice;
	private int currentColorChoice;
	
	
	public void setupApplet() {
		data = readData();
		
		setLayout(new BorderLayout(20, 0));
		
		add("East", keyPanel(data));
		add("Center", displayPanel(data));
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		data.addCatVariable("key", getParameter(CAT_NAME_PARAM), getParameter(CAT_VALUES_PARAM),
																																getParameter(CAT_LABELS_PARAM));
		
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			thePieChart = new PieSizeView(data, this, "y", kColorScheme[0]);
		thePanel.add("Center", thePieChart);
		
		return thePanel;
	}
	
	protected XPanel keyPanel(DataSet data) {
			XPanel thePanel = new XPanel();
			thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			
				freqTable = new SimpleTableView(data, this, "y", "key", kColorScheme[0], 1);
				freqTable.setColorText(false);
				freqTable.setValueName(data.getVariable("y").name);
				
			thePanel.add(freqTable);
				
				XPanel choicePanel = new XPanel();
				choicePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
				
					XLabel chartLabel = new XLabel(translate("Colour scheme") + ":", XLabel.LEFT, this);
					chartLabel.setFont(getStandardBoldFont());
				choicePanel.add(chartLabel);
				
					colorChoice = new XChoice(this);
					colorChoice.addItem(translate("Colours"));
					colorChoice.addItem(translate("Grey 1"));
					colorChoice.addItem(translate("Grey 2"));
				choicePanel.add(colorChoice);
			
			thePanel.add(choicePanel);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == colorChoice) {
			int newChoice = colorChoice.getSelectedIndex();
			if (newChoice != currentColorChoice) {
				currentColorChoice = newChoice;
				thePieChart.setCatColors(kColorScheme[newChoice]);
				thePieChart.repaint();
				freqTable.setKeyColors(kColorScheme[newChoice]);
				freqTable.repaint();
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