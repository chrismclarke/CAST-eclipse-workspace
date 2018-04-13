package boxPlotProg;

import java.awt.*;

import axis.*;
import dataView.*;
import boxPlot.*;
import utils.*;

public class GroupedBoxApplet extends XApplet {
	static final protected String AXIS_INFO_PARAM = "horizAxis";
	static final private String LABEL_AXIS_PARAM = "labelAxis";
	static final private String DRAW_GRID_PARAM = "drawGrid";
	
	private XChoice dotOrBoxChoice;
	
	protected GroupedBoxView theView;
	protected DataSet data;
	
	public void setupApplet() {
		setupData();
		
		setLayout(new BorderLayout());
		add("Center", displayPanel(data));
		add("South", controlPanel(data));
	}
	
	protected void setupData() {
		data = new DataSet();
		synchronized (data) {
			data.addCatVariable("group", getParameter(CAT_NAME_PARAM),
									getParameter(CAT_VALUES_PARAM), getParameter(CAT_LABELS_PARAM));
			
			data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		}
	}
	
	protected GroupedBoxView createView(DataSet data, HorizAxis theHorizAxis,
																							VertAxis theVertAxis) {
		return new GroupedBoxView(data, this, theHorizAxis, theVertAxis);
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			XPanel mainPanel = new XPanel();
			mainPanel.setLayout(new AxisLayout());
			
				HorizAxis theHorizAxis = new HorizAxis(this);
				String labelInfo = getParameter(AXIS_INFO_PARAM);
				theHorizAxis.readNumLabels(labelInfo);
			mainPanel.add("Bottom", theHorizAxis);
			
				VertAxis theVertAxis = new VertAxis(this);
				CatVariable groupVariable = data.getCatVariable();
				theVertAxis.setCatLabels(groupVariable);
			mainPanel.add("Left", theVertAxis);
			
			String labelAxesString = getParameter(LABEL_AXIS_PARAM);
			boolean labelAxes = (labelAxesString != null) && labelAxesString.equals("true");
			if (labelAxes) {
				String yName = data.getVariable("y").name;
				String groupName = data.getVariable("group").name;
				theHorizAxis.setAxisName(yName);
				thePanel.add("North", new XLabel(groupName, XLabel.LEFT, this));
			}
			
				theView = createView(data, theHorizAxis, theVertAxis);
				String drawGridString = getParameter(DRAW_GRID_PARAM);
				if (drawGridString != null && drawGridString.equals("true"))
					theView.setDrawGrid(true);
				theView.lockBackground(Color.white);
				
			mainPanel.add("Center", theView);
		
		thePanel.add("Center", mainPanel);
		
		return thePanel;
	}
	
	protected XChoice createDotBoxChoice() {
		dotOrBoxChoice = new XChoice(this);
		dotOrBoxChoice.addItem(translate("Dot plots"));
		dotOrBoxChoice.addItem(translate("Box plots"));
		dotOrBoxChoice.select(0);
		return dotOrBoxChoice;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 4));
		
		thePanel.add(createDotBoxChoice());
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == dotOrBoxChoice) {
			int newPlotType = dotOrBoxChoice.getSelectedIndex();
			if (newPlotType != theView.getPlotType())
				theView.setPlotType(newPlotType);
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}