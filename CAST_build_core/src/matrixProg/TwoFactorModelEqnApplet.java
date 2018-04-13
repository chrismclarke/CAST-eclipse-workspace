package matrixProg;

import java.awt.*;

import dataView.*;

import matrix.*;


public class TwoFactorModelEqnApplet extends GenericModelEqnApplet {
	
	static final private Color kTableBackgroundColor = new Color(0xC8CCED);
	
	protected void addBottomPanel(XPanel thePanel) {
		CatSelectorView table = new CatSelectorView(data, this, "x1", "x2", kXColors[0], kXColors[1]);
		table.lockBackground(kTableBackgroundColor);
		
		thePanel.add(table);
	}
}