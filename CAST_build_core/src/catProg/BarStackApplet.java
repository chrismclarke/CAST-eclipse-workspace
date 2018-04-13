package catProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;

import cat.*;


public class BarStackApplet extends XApplet {
	static final private String COUNT_INFO_PARAM = "countAxis";
	static final private String PROPN_INFO_PARAM = "propnAxis";
	
	private XSlider animationSlider;
	private DataView theView;
	private HorizAxis catAxis;
	private CatKey3View key;
	
	private int currentFrame = 0;
	
	public void setupApplet() {
		DataSet data = readData();
		
		setLayout(new BorderLayout(40, 0));
		
			XPanel barPanel = new XPanel();
			barPanel.setLayout(new BorderLayout());
			barPanel.add("Center", displayPanel(data));
				animationSlider = new XNoValueSlider(translate("Bar chart"), translate("Stacked bar chart"), null, 0,
																										BarStackView.kTransitions, 0, this);
				animationSlider.setFont(getStandardBoldFont());
			barPanel.add("South", animationSlider);
		
		add("Center", barPanel);
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
		
			XPanel labelPanel = new XPanel();
			labelPanel.setLayout(new BorderLayout());
			labelPanel.add("West", new XLabel(translate("Count"), XLabel.LEFT, this));
			labelPanel.add("East", new XLabel(translate("Proportion"), XLabel.RIGHT, this));
		thePanel.add("North", labelPanel);
		
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
			
				catAxis = new HorizAxis(this);
				CatVariable catVariable = data.getCatVariable();
				catAxis.setCatLabels(catVariable);
			barPanel.add("Bottom", catAxis);
			
				theView = new BarStackView(data, this, "y", catAxis, propnAxis);
			barPanel.add("Center", theView);
				theView.lockBackground(Color.white);
		
		thePanel.add("Center", barPanel);
		return thePanel;
	}
	
	private XPanel rightPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			key = new CatKey3View(data, this, "y");
			key.setFont(getStandardBoldFont());
			key.show(false);
			key.setReverseOrder();
		thePanel.add(key);
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == animationSlider) {
			int newFrame = animationSlider.getValue();
			theView.setFrame(newFrame);
			if (newFrame == 0 && currentFrame != 0) {
				key.show(false);
				catAxis.show(true);
			}
			else if (newFrame != 0 && currentFrame == 0) {
				key.show(true);
				catAxis.show(false);
			}
			currentFrame = newFrame;
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}