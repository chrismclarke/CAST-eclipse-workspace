package catProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;

import cat.*;


public class ReorderBarApplet extends XApplet {
	static final private String COUNT_INFO_PARAM = "countAxis";
	static final private String PROPN_INFO_PARAM = "propnAxis";
	static final private String PROPN_DECS_PARAM = "propnDecs";
	
	protected int proportionDecs;
	
	public void setupApplet() {
		DataSet data = readData();
		
		setLayout(new BorderLayout(40, 0));
		add("Center", displayPanel(data));
		add("East", freqTablePanel(data));
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
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
		
		XPanel barPanel = new XPanel();
		barPanel.setLayout(new AxisLayout());
		
		VertAxis countAxis = new VertAxis(this);
		String labelInfo = getParameter(COUNT_INFO_PARAM);
		countAxis.readNumLabels(labelInfo);
		barPanel.add("Left", countAxis);
		
		VertAxis propnAxis = new VertAxis(this);
		labelInfo = getParameter(PROPN_INFO_PARAM);
		propnAxis.readNumLabels(labelInfo);
		barPanel.add("Right", propnAxis);
		
		HorizAxis catAxis = new HorizAxis(this);
		CatVariable catVariable = data.getCatVariable();
		catAxis.setCatLabels(catVariable);
		barPanel.add("Bottom", catAxis);
		
		DataView theView = new CatBarView(data, this, "y", CatDataView.DRAG_REORDER, catAxis, countAxis);
		barPanel.add("Center", theView);
		theView.lockBackground(Color.white);
		
		thePanel.add("Center", barPanel);
		
			XPanel labelPanel = new XPanel();
			labelPanel.setLayout(new BorderLayout());
			labelPanel.add("West", new XLabel(translate("Frequency"), XLabel.LEFT, this));
			labelPanel.add("East", new XLabel(translate("Proportion"), XLabel.RIGHT, this));
		thePanel.add("North", labelPanel);
		
		return thePanel;
	}
	
	protected XPanel freqTablePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
															VerticalLayout.VERT_CENTER, 0));
		FreqTableView theTable = new FreqTableView(data, this, "y", CatDataView.DRAG_REORDER, proportionDecs,
					FreqTableView.SHORT_HEADINGS, FreqTableView.PROPN, null, true);
					
		thePanel.add(theTable);
		return thePanel;
	}
}