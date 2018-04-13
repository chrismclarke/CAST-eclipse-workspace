package catProg;

import java.awt.*;

import dataView.*;
import axis.*;
import utils.*;
import valueList.ProportionView;
import cat.*;


public class PieBarApplet extends PieChartApplet {
	static final private String COUNT_INFO_PARAM = "countAxis";
	static final private String PROPN_INFO_PARAM = "propnAxis";
//	static final private String ORIENTATION_PARAM = "orientation";
	
	static final protected Color kPropnBackground = new Color(0xDDDDEE);
	
	private PieView thePieView;
	private CatBarView theBarView;
	
	private XButton clearSelectionButton;
	
/*
	public void setupApplet() {
		DataSet data = readData();
		
		setLayout(new BorderLayout(0, 0));
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new ProportionLayout(0.45, 0, ProportionLayout.VERTICAL));
		
				thePieView = new PieView(data, this, "y", CatDataView.DRAG_CUMULATIVE);
				thePieView.setRetainLastSelection(true);
			
			mainPanel.add(ProportionLayout.TOP, thePieView);
			mainPanel.add(ProportionLayout.BOTTOM, barChartPanel(data));
		
		add("Center", mainPanel);
		
		add("East", controlPanel(data));
	}
*/
	
	public void setupApplet() {
		DataSet data = readData();
		
		setLayout(new ProportionLayout(0.55, 0, ProportionLayout.VERTICAL));
		
		add(ProportionLayout.TOP, barChartPanel(data));
		
			XPanel piePanel = new XPanel();
			piePanel.setLayout(new BorderLayout(0, 0));
		
				thePieView = new PieView(data, this, "y", CatDataView.DRAG_CUMULATIVE);
				thePieView.setRetainLastSelection(true);
			
			piePanel.add("East", controlPanel(data));
			piePanel.add("Center", thePieView);
		
		add(ProportionLayout.BOTTOM, piePanel);
	}
	
	protected XPanel barChartPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		XPanel labelPanel = new XPanel();
		labelPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		labelPanel.add(new XLabel(translate("Propn"), XLabel.RIGHT, this));
		thePanel.add("North", labelPanel);
		
		XPanel barPanel = new XPanel();
		barPanel.setLayout(new AxisLayout());
		
		MultiVertAxis countPropnAxis = new MultiVertAxis(this, 2);
		String labelInfo = getParameter(COUNT_INFO_PARAM);
		countPropnAxis.readNumLabels(labelInfo);
		labelInfo = getParameter(PROPN_INFO_PARAM);
		countPropnAxis.readExtraNumLabels(labelInfo);
		countPropnAxis.setStartAlternate(1);
		barPanel.add("Left", countPropnAxis);
		
		HorizAxis catAxis = new HorizAxis(this);
		CatVariable catVariable = data.getCatVariable();
		catAxis.setCatLabels(catVariable);
		barPanel.add("Bottom", catAxis);
		
		theBarView = new CatBarView(data, this, "y", CatDataView.DRAG_CUMULATIVE, catAxis, countPropnAxis);
		theBarView.setRetainLastSelection(true);
		barPanel.add("Center", theBarView);
		theBarView.lockBackground(Color.white);
		
		thePanel.add("Center", barPanel);
		return thePanel;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 40));
		
			XPanel propnPanel = new InsetPanel(5, 7);
			propnPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 3));
				
				XLabel propnLabel = new XLabel(translate("Proportion"), XLabel.CENTER, this);
				propnLabel.setFont(getBigFont());
				
			propnPanel.add(propnLabel);
				
				ProportionView pView = new ProportionView(data, "y", this);
				pView.setLabel(null);
				pView.setDecimals(proportionDecs);
				pView.setFont(getBigFont());
				pView.setHighlight(true);
			
			propnPanel.add(pView);
			propnPanel.lockBackground(kPropnBackground);
		
		thePanel.add(propnPanel);
		
			clearSelectionButton = new XButton(translate("Clear"), this);
		thePanel.add(clearSelectionButton);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == clearSelectionButton) {
			data.clearSelection();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}