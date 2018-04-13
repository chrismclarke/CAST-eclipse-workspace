package corrProg;

import java.awt.*;

import dataView.*;
import coreGraphics.*;

import corr.*;

//import scatterProg.*;


public class CorrelTransApplet extends ScatterApplet {
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 3));
		
		CorrelTransView theCorr = new CorrelTransView(data, "x", "y", CorrelationView.NO_FORMULA,
																			theHorizAxis, theVertAxis, this);
		thePanel.add(theCorr);
		return thePanel;
	}
}