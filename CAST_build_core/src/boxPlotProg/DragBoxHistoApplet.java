package boxPlotProg;

import java.awt.*;

import axis.*;
import dataView.*;
import utils.*;
import boxPlot.*;


public class DragBoxHistoApplet extends XApplet {
	static final private String AXIS_INFO_PARAM = "horizAxis";
	
	static final private Color kBoxColor = new Color(0xAA0000);
	static final private Color kOtherBackgroundColor = new Color(0xFAFAFF);
	static final private Color kBoxBackgroundColor = new Color(0xFFFFEE);
	
	private XChoice distnShapeChoice;
	private int currentDistnShape;
	
	protected DragDistnShapeView theView;
	
	public void setupApplet() {
		DataSet data = new DataSet();
		
		setLayout(new BorderLayout(0, 10));
		add("Center", viewPanel(data));
		add("South", controlPanel(data));
	}
	
	protected XPanel viewPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			HorizAxis theHorizAxis = new HorizAxis(this);
			String labelInfo = getParameter(AXIS_INFO_PARAM);
			theHorizAxis.readNumLabels(labelInfo);
		thePanel.add("Bottom", theHorizAxis);
		
			theView = new DragDistnShapeView(data, this, theHorizAxis);
			theView.lockBackground(Color.white);
			theView.setBoxColor(kBoxColor);
			theView.setBackground(kOtherBackgroundColor, kBoxBackgroundColor);
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
		
		distnShapeChoice = new XChoice(this);
		distnShapeChoice.addItem(translate("Symmetric"));
		distnShapeChoice.addItem(translate("Skew - long high tail"));
		distnShapeChoice.addItem(translate("Skew - long low tail"));
		distnShapeChoice.addItem(translate("Symmetric - both tails long"));
		distnShapeChoice.addItem(translate("Dragging"));
		distnShapeChoice.select(0);
		currentDistnShape = 0;
		thePanel.add(distnShapeChoice);
		
		return thePanel;
	}
	
	public void notifyStartDrag() {
		distnShapeChoice.select(4);
		currentDistnShape = 4;
	}
	
	private boolean localAction(Object target) {
		if (target == distnShapeChoice) {
			if (distnShapeChoice.getSelectedIndex() != currentDistnShape) {
				currentDistnShape = distnShapeChoice.getSelectedIndex();
				theView.setupBox(currentDistnShape);
				theView.repaint();
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