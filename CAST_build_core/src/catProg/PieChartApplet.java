package catProg;

import java.awt.*;

import dataView.*;
import utils.*;
import cat.*;


public class PieChartApplet extends XApplet {
	static final private String PROPN_DECS_PARAM = "propnDecs";
	static final private String COUNT_NAME_PARAM = "countName";
	
	protected int proportionDecs;
	protected DataSet data;
	
	public void setupApplet() {
		DataSet data = readData();
		
		setLayout(new BorderLayout(40, 0));
		add("Center", displayPanel(data));
		add("West", freqTablePanel(data));
	}
	
	protected DataSet readData() {
		data = new DataSet();
		CatVariable v = new CatVariable(getParameter(CAT_NAME_PARAM), Variable.USES_REPEATS);
		v.readLabels(getParameter(CAT_LABELS_PARAM));
		v.readValues(getParameter(CAT_VALUES_PARAM));
		data.addVariable("y", v);
		
		proportionDecs = Integer.parseInt(getParameter(PROPN_DECS_PARAM));
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		thePanel.add("Center", new PieView(data, this, "y", CatDataView.SELECT_ONE));
		return thePanel;
	}
	
	protected XPanel freqTablePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
															VerticalLayout.VERT_CENTER, 0));
		String countNameString = getParameter(COUNT_NAME_PARAM);
		FreqTableView theTable = null;
		if (countNameString != null)
			theTable = new FreqTableView(data, this, "y", CatDataView.SELECT_ONE, proportionDecs,
					FreqTableView.LONG_HEADINGS, FreqTableView.PERCENT, countNameString, true);
		else
			theTable = new FreqTableView(data, this, "y", CatDataView.SELECT_ONE, proportionDecs,
					FreqTableView.SHORT_HEADINGS, FreqTableView.PROPN, null, true);
					
		thePanel.add(theTable);
		return thePanel;
	}
}