package graphicsProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import valueList.OneValueView;
import coreGraphics.*;

import cat.*;
import graphics.*;


public class TransformXApplet extends ScatterApplet {
	static final protected String N_AXES_PARAM = "nHorizAxes";
	static final protected String GROUP_VAR_NAME_PARAM = "groupVarName";
	static final protected String GROUP_VALUES_PARAM = "groupValues";
	static final protected String GROUP_LABELS_PARAM = "groupLabels";
	static final protected String SIZE_VAR_NAME_PARAM = "sizeVarName";
	static final protected String SIZE_VALUES_PARAM = "sizeValues";

	private ScatterCircleView scatterView;
	private int nAxes;
	
	private XNoValueSlider transformSlider;
	
	private XChoice displayTypeChoice;
	private int currentDisplayType = 0;
	
	protected DataSet readData() {
		DataSet data = super.readData();
		
		data.addCatVariable("group", getParameter(GROUP_VAR_NAME_PARAM),
												getParameter(GROUP_VALUES_PARAM), getParameter(GROUP_LABELS_PARAM));
		data.addNumVariable("size", getParameter(SIZE_VAR_NAME_PARAM),
																													getParameter(SIZE_VALUES_PARAM));
		
		return data;
	}
	
	protected XPanel rightPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 30));
		
			CatKey3View theKey = new CatKey3View(data, this, "group");
			theKey.setCatColour(ScatterCircleView.kGroupColor);
		thePanel.add(theKey);
				
			XPanel labelPanel = new XPanel();
			labelPanel.setLayout(new VerticalLayout(VerticalLayout.LEFT, VerticalLayout.VERT_CENTER, 0));
			
				XLabel labelLabel = new XLabel(data.getVariable("label").name, XLabel.LEFT, this);
				labelLabel.setFont(getStandardBoldFont());
			labelPanel.add(labelLabel);
				
				OneValueView labelView = new OneValueView(data, "label", this);
				labelView.setNameDraw(false);
			labelPanel.add(labelView);
			
		thePanel.add(labelPanel);
		
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new ProportionLayout(0.5, 0));
		
			transformSlider = new XNoValueSlider(translate("Original scale"), translate("Log scale"), null,
												0, 100, 0, this);
		
		thePanel.add(ProportionLayout.LEFT, transformSlider);
		
			XPanel choicePanel = new XPanel();
			choicePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
			
				displayTypeChoice = new XChoice(this);
				displayTypeChoice.addItem(translate("Display as crosses"));
				displayTypeChoice.addItem(translate("Display as circles"));
			choicePanel.add(displayTypeChoice);
			
		thePanel.add(ProportionLayout.RIGHT, choicePanel);
		
		return thePanel;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			nAxes = Integer.parseInt(getParameter(N_AXES_PARAM));
			MultiHorizAxis xAxis = new MultiHorizAxis(this, nAxes);
			xAxis.readNumLabels(getParameter(X_AXIS_INFO_PARAM));
			for (int i=1 ; i<nAxes ; i++)
				xAxis.readExtraNumLabels(getParameter(X_AXIS_INFO_PARAM + i));
			xAxis.setAxisName(data.getVariable("x").name);
			theHorizAxis = xAxis;
		thePanel.add("Bottom", xAxis);
		
			theVertAxis = createVertAxis(data);
		thePanel.add("Left", theVertAxis);
		
			scatterView = new ScatterCircleView(data, this, theHorizAxis, theVertAxis, "x", "y", "size", "group");
			scatterView.setRetainLastSelection(true);
			scatterView.setMaxRadius(ScatterCircleView.kMaxRadius * 2 / 3);
			scatterView.lockBackground(Color.white);
		thePanel.add("Center", scatterView);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == displayTypeChoice) {
			int newChoice = displayTypeChoice.getSelectedIndex();
			if (newChoice != currentDisplayType) {
				currentDisplayType = newChoice;
				
				scatterView.setDrawType(newChoice);
				scatterView.repaint();
			}
			return true;
		}
		else if (target == transformSlider) {
			int newTransformIndex = 300 - transformSlider.getValue();
			MultiHorizAxis xAxis = (MultiHorizAxis)theHorizAxis;
			int axisIndex = (transformSlider.getValue() - 1) * nAxes / 100;
			xAxis.setAlternateLabels(axisIndex);
			theHorizAxis.setPowerIndex(newTransformIndex);
			scatterView.repaint();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}