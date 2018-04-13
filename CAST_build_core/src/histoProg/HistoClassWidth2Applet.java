package histoProg;

import java.awt.*;

import dataView.*;
import histo.*;


public class HistoClassWidth2Applet extends ShiftClassHistoApplet {
	protected int initialBarType() {
		return HistoView.NO_BARS;
	}
	
	protected XPanel createControls() {
		XPanel controlPanel = new XPanel();
		controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		controlPanel.add(widthControls());
		return controlPanel;
	}
}