package boxPlotProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;

import boxPlot.*;


public class QuartileApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	
	private QuartileDotPlotView theView;
	
	private XChoice nQuantilesChoice;
	private int currentChoice = 0;
	
	public void setupApplet() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		
		setLayout(new BorderLayout());
		add("Center", displayPanel(data));
		add("South", controlPanel(data));
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			String labelInfo = getParameter(AXIS_INFO_PARAM);
			theHorizAxis.readNumLabels(labelInfo);
		thePanel.add("Bottom", theHorizAxis);
		
			theView = new QuartileDotPlotView(data, this, "y", theHorizAxis, 4);
		thePanel.add("Center", theView);
			theView.lockBackground(Color.white);
		return thePanel;
	}
	
	private XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
		
			nQuantilesChoice = new XChoice(this);
			nQuantilesChoice.addItem(translate("Quartiles"));
			nQuantilesChoice.addItem(translate("Deciles"));
			
		thePanel.add(nQuantilesChoice);
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == nQuantilesChoice) {
			if (nQuantilesChoice.getSelectedIndex() != currentChoice) {
				currentChoice = nQuantilesChoice.getSelectedIndex();
				theView.setNoOfTiles(currentChoice == 0 ? 4 : 10);
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