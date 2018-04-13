package mixtureProg;

import java.awt.*;

import dataView.*;

import mixture.*;


public class Triangle2DDesignApplet extends TriangleDesignsApplet {
							//		Not 3-dimensional at all (unlike TriangleDesignsApplet)
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
			
			EquiTriangleView localView = new EquiTriangleView(data, this, "x", "y", "z");
		localView.setCrossSize(DataView.LARGE_CROSS);
		
		thePanel.add("Center", localView);		//	superclass's theView is still null
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		return thePanel;
	}
	
}