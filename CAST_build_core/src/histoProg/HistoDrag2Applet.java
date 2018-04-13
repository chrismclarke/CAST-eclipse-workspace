package histoProg;

import java.awt.*;

import dataView.*;
import valueList.ProportionView;
import axis.*;

import histo.*;


public class HistoDrag2Applet extends VariableClass2Applet {
	
	protected VariableClassHistoView createHistoView(DataSet data, HorizAxis theHorizAxis,
										DensityAxis2 densityAxis, double class0Start, double classWidth) {
		DragClassHistoView theView = new DragClassHistoView(data, this, theHorizAxis, densityAxis, class0Start, classWidth);
		theView.setFont(getBigBoldFont());
		return theView;
	}
	
	protected XPanel createControls(DataSet data) {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 2));
		
		ProportionView proportion = new ProportionView(data, "y", this);
		proportion.setFont(getBigFont());
		
		controlPanel.add(proportion);
		return controlPanel;
	}
}