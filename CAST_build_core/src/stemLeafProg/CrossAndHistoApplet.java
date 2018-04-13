package stemLeafProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;

import stemLeaf.*;


public class CrossAndHistoApplet extends XApplet {
	static final private String CLASS_INFO_PARAM = "classInfo";
	static final private String AXIS_INFO_PARAM = "axisInfo";
	static final private String DENSITY_CURVE_GIF_PARAM = "densityCurveGif";		//		can be missing
	
	private XNoValueSlider crossHistoSlider;
	private XChoice displayChoice;
	private int currentDisplay = CrossAndHistoView.DATA_ONLY;
	
	private CrossAndHistoView theView;
	
	public void setupApplet() {
		DataSet data = getData();
		
		setLayout(new BorderLayout(0, 10));
		
		add("Center", displayPanel(data));
		add("South", controlPanel(data));
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		return data;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis horizAxis = new HorizAxis(this);
			String labelInfo = getParameter(AXIS_INFO_PARAM);
			horizAxis.readNumLabels(labelInfo);
			horizAxis.setAxisName(data.getVariable("y").name);
			
		thePanel.add("Bottom", horizAxis);
		
			theView = new CrossAndHistoView(data, this, horizAxis, getParameter(CLASS_INFO_PARAM),
																						getParameter(DENSITY_CURVE_GIF_PARAM), currentDisplay);
			theView.lockBackground(Color.white);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data) {
		String densityGifString = getParameter(DENSITY_CURVE_GIF_PARAM);
		if (densityGifString == null)
			return noDensityControlPanel(data);
		else
			return densityControlPanel(data);
	}
	
	private XPanel noDensityControlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.25, 0));
		
		thePanel.add(ProportionLayout.LEFT, new XPanel());
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new ProportionLayout(0.667, 0));
		
			rightPanel.add(ProportionLayout.RIGHT, new XPanel());
		
				crossHistoSlider = new XNoValueSlider(translate("Dot plot"), translate("Histogram"), null, 0,
															CrossAndHistoView.kHistoIndex, 0, this);
				crossHistoSlider.setFont(getStandardBoldFont());
			rightPanel.add(ProportionLayout.LEFT, crossHistoSlider);
		
		thePanel.add(ProportionLayout.RIGHT, rightPanel);
		return thePanel;
	}
	
	private XPanel densityControlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(20, 0));
		
			XPanel leftPanel = new XPanel();
			leftPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
			
				crossHistoSlider = new XNoValueSlider(translate("Dot plot"), translate("Histogram"), null, 0,
																							CrossAndHistoView.kHistoIndex, 0, this);
				crossHistoSlider.setFont(getStandardBoldFont());
			leftPanel.add(crossHistoSlider);
		
		thePanel.add("Center", leftPanel);
		
			XPanel rightPanel = new XPanel();
			rightPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_CENTER, 0));
			
				displayChoice = new XChoice(translate("Display") + ":", XChoice.HORIZONTAL, this);
				displayChoice.addItem(translate("Data only"));
				displayChoice.addItem(translate("Data and density"));
				displayChoice.addItem(translate("Density only"));
				displayChoice.setFont(getStandardBoldFont());
				displayChoice.select(currentDisplay);
			rightPanel.add(displayChoice);
		
		thePanel.add("East", rightPanel);
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == crossHistoSlider) {
			theView.setFrame(crossHistoSlider.getValue());
			return true;
		}
		else if (target == displayChoice) {
			int newChoice = displayChoice.getSelectedIndex();
			if (newChoice != currentDisplay) {
				currentDisplay = newChoice;
				theView.setDisplayType(newChoice);
			}
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}