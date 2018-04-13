package catProg;

import java.awt.*;

import dataView.*;
import utils.*;
import cat.*;


public class BarPieApplet extends XApplet {
	
	private XSlider animationSlider;
	private DataView theView;
	
	public void setupApplet() {
		DataSet data = readData();
		
		setLayout(new BorderLayout(10, 0));
		
			XPanel piePanel = new XPanel();
			piePanel.setLayout(new BorderLayout());
			piePanel.add("Center", displayPanel(data));
				animationSlider = new XNoValueSlider(translate("Stacked bar chart"), translate("Pie chart"), null, 0,
																										BarPieView.kTransitions, 0, this);
				animationSlider.setFont(getStandardBoldFont());
			piePanel.add("South", animationSlider);
			
		add("Center", piePanel);
		add("East", rightPanel(data));
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		CatVariable v = new CatVariable(getParameter(CAT_NAME_PARAM), Variable.USES_REPEATS);
		v.readLabels(getParameter(CAT_LABELS_PARAM));
		v.readValues(getParameter(CAT_VALUES_PARAM));
		data.addVariable("y", v);
		return data;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
			theView = new BarPieView(data, this, "y");
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	private XPanel rightPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			CatKey3View key = new CatKey3View(data, this, "y");
			key.setFont(getStandardBoldFont());
			key.setReverseOrder();
		thePanel.add(key);
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == animationSlider) {
			theView.setFrame(animationSlider.getValue());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}