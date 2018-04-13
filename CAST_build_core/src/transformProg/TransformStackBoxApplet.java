package transformProg;

import java.awt.*;

import dataView.*;
import utils.*;
import axis.*;
import dotPlot.*;


public class TransformStackBoxApplet extends TransformDotApplet {
	
	private AlternateDataView theView;
	
	private XChoice displayChoice;
	private int currentDisplay = 0;
	
	protected DataView coreView(DataSet data, HorizAxis theHorizAxis) {
		
		StackingDotPlotView stackView = new StackingDotPlotView(data, this, theHorizAxis);
		stackView.initialiseToFinalFrame();
		Insets stackInsets = stackView.getViewBorder();
		
		DataView boxView = super.coreView(data, theHorizAxis);
		Insets boxInsets = boxView.getViewBorder();
		
		Insets maxInsets = new Insets(Math.max(boxInsets.top, stackInsets.top), Math.max(boxInsets.left, stackInsets.left),
							Math.max(boxInsets.bottom, stackInsets.bottom), Math.max(boxInsets.right, stackInsets.right));
		
		theView = new AlternateDataView(data, this, maxInsets);
		
		theView.addDataView(boxView, "boxplot");
		theView.addDataView(stackView, "stacked");
		
		return theView;
	}
	
	protected XPanel createControls(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 10, 0, 0);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
			displayChoice = new XChoice(translate("Display as"), XChoice.HORIZONTAL, this);
			displayChoice.addItem(translate("Box plot"));
			displayChoice.addItem(translate("Stacked dot plot"));
		thePanel.add(displayChoice);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == displayChoice) {
			int newChoice = displayChoice.getSelectedIndex();
			if (newChoice != currentDisplay) {
				currentDisplay = newChoice;
				theView.showView((newChoice == 0) ? "boxplot" : "stacked");
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