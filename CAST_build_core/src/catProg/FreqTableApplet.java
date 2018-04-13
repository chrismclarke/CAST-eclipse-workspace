package catProg;

import java.awt.*;

import dataView.*;
import utils.*;
import cat.*;


public class FreqTableApplet extends PieChartApplet {
	static final private String COUNT_NAME_PARAM = "countName";
	static final private String SHORT_COUNT_NAME_PARAM = "shortCountName";
	
	private FreqTableView theView;
	private XChoice relFreqChoice;
	private int relFreqChoiceItem;
	
	public void setupApplet() {
		DataSet data = readData();
		
		setLayout(new VerticalLayout(VerticalLayout.CENTER,
															VerticalLayout.VERT_CENTER, 0));
		add(freqTable(data));
		add(controlPanel(data));
	}
	
	protected FreqTableView freqTable(DataSet data) {
		String countTitle = getParameter(COUNT_NAME_PARAM);
		theView = new FreqTableView(data, this, "y", CatDataView.SELECT_ONE, proportionDecs,
					FreqTableView.LONG_HEADINGS, FreqTableView.NO_RELFREQ, countTitle, false);
		theView.setFont(getBigFont());
		return theView;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		relFreqChoice = new XChoice(this);
			String shortCountName = getParameter(SHORT_COUNT_NAME_PARAM);
			if (shortCountName == null)
				shortCountName = translate("Count");
		relFreqChoice.addItem(shortCountName + " " + translate("only"));
		relFreqChoice.addItem(shortCountName + " & " + translate("proportion"));
		relFreqChoice.addItem(shortCountName + " & " + translate("percentage"));
		relFreqChoiceItem = 0;
		thePanel.add(relFreqChoice);
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == relFreqChoice) {
			int newChoice = relFreqChoice.getSelectedIndex();
			if (newChoice != relFreqChoiceItem) {
				theView.setRelFreqDisplay(newChoice);
				relFreqChoiceItem = newChoice;
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