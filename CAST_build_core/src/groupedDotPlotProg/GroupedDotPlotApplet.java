package groupedDotPlotProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;

import dotPlot.*;


public class GroupedDotPlotApplet extends ColouredDotPlotApplet {
	
	private XButton groupButton;
	protected XSlider animateSlider;
	
	protected XPanel infoPanel(DataSet data) {
		return null;
	}
	
	protected XPanel dotPlotPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
		HorizAxis theHorizAxis = new HorizAxis(this);
		String labelInfo = getParameter(AXIS_INFO_PARAM);
		theHorizAxis.readNumLabels(labelInfo);
		theHorizAxis.setAxisName(data.getVariable("y").name);
		thePanel.add("Bottom", theHorizAxis);
		
		VertAxis theVertAxis = new VertAxis(this);
		CatVariable groupVariable = data.getCatVariable();
		theVertAxis.setCatLabels(groupVariable);
		thePanel.add("Left", theVertAxis);
		
		theDotPlot = new GroupedDotPlotView(data, this, theHorizAxis, theVertAxis);
		thePanel.add("Center", theDotPlot);
		theDotPlot.setRetainLastSelection(true);
		theDotPlot.lockBackground(Color.white);
		return thePanel;
	}
	
	protected XPanel controlPanel() {
//		ScrollImages.loadScroll(this);
		XPanel thePanel = new XPanel();
		
		thePanel.setLayout(new BorderLayout(10, 0));
		
			XPanel buttonPanel = new XPanel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 2));
				groupButton = new XButton(translate("Animate Grouping"), this);
			buttonPanel.add(groupButton);
		thePanel.add("West", buttonPanel);
		
			animateSlider = new XNoValueSlider(translate("combined"), translate("grouped"), null, 0, GroupedDotPlotView.kEndFrame,
																						0, this);
		thePanel.add("Center", animateSlider);
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == animateSlider) {
			theDotPlot.setFrame(animateSlider.getValue());
			return true;
		}
		else if (target == groupButton) {
			((GroupedDotPlotView)theDotPlot).doGroupingAnimation(animateSlider);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean  action(Event  evt, Object  what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
}