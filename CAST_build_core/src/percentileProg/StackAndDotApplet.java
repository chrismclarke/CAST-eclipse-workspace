package percentileProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import valueList.*;
import coreGraphics.*;


public class StackAndDotApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	static final private String SEQUENCE_NAME_PARAM = "sequenceName";
	static final private String SEQUENCE_VALUES_PARAM = "sequenceValues";
	static final private String LONG_VAR_NAME_PARAM = "longVarName";
	static final private String UNITS_PARAM = "units";
	
	static final private Color kHeadingColor = new Color(0x000099);
	
	public void setupApplet() {
		DataSet data = getData();
		
		setLayout(new BorderLayout(0, 10));
			
			XPanel displayPanel = new XPanel();
			displayPanel.setLayout(new ProportionLayout(0.5, 5, ProportionLayout.VERTICAL));
			
			displayPanel.add(ProportionLayout.TOP, stackedPanel(data));
			displayPanel.add(ProportionLayout.BOTTOM, jitteredPanel(data));
			
		add("Center", displayPanel);
		
		add("South", bottomPanel(data));
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		
			NumVariable sequenceVar = new NumVariable(getParameter(SEQUENCE_NAME_PARAM));
			sequenceVar.readSequence(getParameter(SEQUENCE_VALUES_PARAM));
		data.addVariable("label", sequenceVar);
		return data;
	}
	
	private XPanel bottomPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 40, 0));
			OneValueView yView = new OneValueView(data, "y", this);
			yView.addEqualsSign();
			yView.setUnitsString(getParameter(UNITS_PARAM));
		thePanel.add(yView);
		
			OneValueView labelView = new OneValueView(data, "label", this);
			labelView.addEqualsSign();
		thePanel.add(labelView);
		return thePanel;
	}
	
	private XPanel stackedPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 3));
			
			XLabel heading = new XLabel("Stacked dot plot", XLabel.LEFT, this);
			heading.setFont(getBigBoldFont());
			heading.setForeground(kHeadingColor);
		thePanel.add("North", heading);
		
			XPanel innerPanel = new XPanel();
			innerPanel.setLayout(new AxisLayout());
			
				HorizAxis horizAxis = new HorizAxis(this);
				horizAxis.readNumLabels(getParameter(AXIS_INFO_PARAM));
				horizAxis.setAxisName(getParameter(LONG_VAR_NAME_PARAM));
			innerPanel.add("Bottom", horizAxis);
			
				StackedDotPlotView view = new StackedDotPlotView(data, this, horizAxis);
				view.lockBackground(Color.white);
				view.setRetainLastSelection(true);
			innerPanel.add("Center", view);
		
		thePanel.add("Center", innerPanel);
		return thePanel;
	}
	
	private XPanel jitteredPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 3));
			
			XLabel heading = new XLabel("Jittered dot plot", XLabel.LEFT, this);
			heading.setFont(getBigBoldFont());
			heading.setForeground(kHeadingColor);
		thePanel.add("North", heading);
		
			XPanel innerPanel = new XPanel();
			innerPanel.setLayout(new AxisLayout());
			
				HorizAxis horizAxis = new HorizAxis(this);
				horizAxis.readNumLabels(getParameter(AXIS_INFO_PARAM));
				horizAxis.setAxisName(getParameter(LONG_VAR_NAME_PARAM));
			innerPanel.add("Bottom", horizAxis);
			
				DotPlotView view = new DotPlotView(data, this, horizAxis, 1.0);
				view.lockBackground(Color.white);
				view.setRetainLastSelection(true);
			innerPanel.add("Center", view);
		
		thePanel.add("Center", innerPanel);
		return thePanel;
	}
}