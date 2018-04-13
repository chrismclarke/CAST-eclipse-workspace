package histoProg;

import java.awt.*;

import dataView.*;
import valueList.ProportionView;
import axis.*;

import histo.*;


public class HistoDragApplet extends VariableClassHistoApplet {
	protected int initialDensityAxisLabel() {
		return DensityAxis.DENSITY_LABELS;
	}
	
	protected int initialHistoLines() {
		return HistoView.VERT_BARS;
	}
	
	protected VariableClassHistoView createHistoView(DataSet data, HorizAxis theHorizAxis,
										DensityAxis densityAxis, double class0Start, double classWidth) {
		return new DragClassHistoView(data, this, theHorizAxis, densityAxis, class0Start, classWidth);
	}
	
	protected XPanel createControls(DataSet data) {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 2));
		
		ProportionView proportion = new ProportionView(data, "y", this);
		
		controlPanel.add(proportion);
		return controlPanel;
	}
}