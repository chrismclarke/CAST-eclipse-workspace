package samplingProg;

import java.awt.*;

import dataView.*;
import coreGraphics.*;

import normal.*;

public class StdNormalProbApplet extends StdNormalApplet {
	
	protected XPanel bottomPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 2));
		
		NumValue biggestX = new NumValue(getParameter(BIGGEST_X_PARAM));
		prob = new NormStdProbView(data, this, "distn", theHorizAxis, biggestX);
		thePanel.add(prob);
		
		return thePanel;
	}
	
	protected int dragType() {
		return DistnDensityView.MAX_DRAG;
	}
}