package catProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;

import cat.*;


public class CatBarApplet extends XApplet {
	static final private String COUNT_INFO_PARAM = "countAxis";
	static final private String PROPN_INFO_PARAM = "propnAxis";
	static final private String COUNT_INFO2_PARAM = "countAxis2";
	static final private String COUNT_NAME_PARAM = "countAxisName";
	static final private String SHORT_CAT_LABEL_PARAM = "shortCatLabels";
	static final private String PROPN_DECS_PARAM = "propnDecs";
	
	protected int proportionDecs;
	protected DataSet data;
	
	public void setupApplet() {
		DataSet data = readData();
		
		setLayout(new BorderLayout(0, 30));
		add("North", freqTablePanel(data));
		add("Center", displayPanel(data));
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
	
	protected CatBarView getBarView(DataSet data, HorizAxis catAxis, VertAxis countAxis) {
		return new CatBarView(data, this, "y", CatDataView.SELECT_ONE, catAxis, countAxis);
	}
	
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		XPanel barPanel = new XPanel();
		barPanel.setLayout(new AxisLayout());
		
			VertAxis countAxis;
			String labelInfo;
			String countInfo2 = getParameter(COUNT_INFO2_PARAM);
			if (countInfo2 == null) {
				countAxis = new VertAxis(this);
				labelInfo = getParameter(COUNT_INFO_PARAM);
				countAxis.readNumLabels(labelInfo);
			}
			else {		//	allows counts such as 4,200,000 to be displayed as 4.2
				MultiVertAxis tempAxis = new MultiVertAxis(this, 2);
				labelInfo = getParameter(COUNT_INFO_PARAM);
				tempAxis.readNumLabels(labelInfo);
				tempAxis.readExtraNumLabels(countInfo2);
				tempAxis.setStartAlternate(1);
				countAxis = tempAxis;
			}
			countAxis.setForeground(CatBarView.kFreqColor);
		barPanel.add("Left", countAxis);
		
			VertAxis propnAxis = new VertAxis(this);
			labelInfo = getParameter(PROPN_INFO_PARAM);
			propnAxis.readNumLabels(labelInfo);
			propnAxis.setForeground(CatBarView.kPropnColor);
		barPanel.add("Right", propnAxis);
		
			HorizAxis catAxis = new HorizAxis(this);
			String shortCatLabels = getParameter(SHORT_CAT_LABEL_PARAM);
			CatVariable catVariable;
			if (shortCatLabels == null)
				catVariable = data.getCatVariable();
			else {
				catVariable = new CatVariable("temp");
				catVariable.readLabels(shortCatLabels);
			}
			catAxis.setCatLabels(catVariable);
		barPanel.add("Bottom", catAxis);
		
			CatBarView theView = getBarView(data, catAxis, countAxis);
			theView.lockBackground(Color.white);
		barPanel.add("Center", theView);
		
		thePanel.add("Center", barPanel);
		
			XPanel labelPanel = new XPanel();
				labelPanel.setLayout(new BorderLayout());
					String axisLabel = getParameter(COUNT_NAME_PARAM);
				XLabel countLabel = new XLabel((axisLabel == null) ? translate("Frequency") : axisLabel, XLabel.LEFT, this);
				countLabel.setForeground(CatBarView.kFreqColor);
			labelPanel.add("West", countLabel);
			
				XLabel propnLabel = new XLabel(translate("Proportion"), XLabel.RIGHT, this);
				propnLabel.setForeground(CatBarView.kPropnColor);
			labelPanel.add("East", propnLabel);
		thePanel.add("North", labelPanel);
		
		return thePanel;
	}
	
	protected XPanel freqTablePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER,
															VerticalLayout.VERT_CENTER, 0));
//		String countNameString = getParameter(COUNT_NAME_PARAM);
		FreqTableView theTable = new FreqTableView(data, this, "y", CatDataView.SELECT_ONE, proportionDecs,
					FreqTableView.SHORT_HEADINGS, FreqTableView.PROPN, null, true);
		
		theTable.setFont(getBigFont());
		thePanel.add(theTable);
		return thePanel;
	}
}