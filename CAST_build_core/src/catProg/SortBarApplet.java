package catProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;

import cat.*;


public class SortBarApplet extends XApplet {
	static final private String COUNT_INFO_PARAM = "countAxis";
	static final private String PROPN_INFO_PARAM = "propnAxis";
	
	private SortBarView theView;
	
	private XChoice orderChoice;
	private int currentOrderIndex;
	
	public void setupApplet() {
		DataSet data = readData();
		
		setLayout(new BorderLayout(0, 20));
		add("Center", displayPanel(data));
		add("South", controlPanel());
	}
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		CatVariable v = new CatVariable(getParameter(CAT_NAME_PARAM), Variable.USES_REPEATS);
		v.readLabels(getParameter(CAT_LABELS_PARAM));
		v.readValues(getParameter(CAT_VALUES_PARAM));
		data.addVariable("y", v);
		return data;
	}
	
	private XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			orderChoice = new XChoice(translate("Category order") + ":", XChoice.HORIZONTAL, this);
			orderChoice.addItem(translate("Alphabetical"));
			orderChoice.addItem(translate("Decreasing frequencies"));
		thePanel.add(orderChoice);
		
		return thePanel;
	}
	
	private XPanel displayPanel(DataSet data) {
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
			
				theView = new SortBarView(data, this, "y", catAxis, countAxis);
				theView.lockBackground(Color.white);
			barPanel.add("Center", theView);
		
		thePanel.add("Center", barPanel);
		
			XPanel labelPanel = new XPanel();
			labelPanel.setLayout(new BorderLayout());
			labelPanel.add("West", new XLabel(translate("Frequency"), XLabel.LEFT, this));
			labelPanel.add("East", new XLabel(translate("Proportion"), XLabel.RIGHT, this));
		thePanel.add("North", labelPanel);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == orderChoice) {
			int newChoice = orderChoice.getSelectedIndex();
			if (newChoice != currentOrderIndex) {
				currentOrderIndex = newChoice;
				theView.doAnimation(newChoice == 0 ? SortBarView.TO_ALPHABETIC
																									: SortBarView.TO_SORTED);
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