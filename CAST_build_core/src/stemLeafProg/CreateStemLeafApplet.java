package stemLeafProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;

import stemLeaf.*;


public class CreateStemLeafApplet extends XApplet {
	static final private String ROW_COL_INFO_PARAM = "rowCol";
	
	private CreateStemLeafView stemLeaf;
	private SLValueListView theList;
	
	private XButton resetButton, sortLeavesButton;
	
	public void setupApplet() {
		DataSet data = getData();
		
		setLayout(new BorderLayout(20, 0));
		
		add("North", headingPanel());
		add("Center", displayPanel(data));
		add("East", valuePanel(data));
		add("South", controlPanel());
	}
	
	private DataSet getData() {
		DataSet data = new DataSet();
		data.addNumVariable("y", getParameter(VAR_NAME_PARAM), getParameter(VALUES_PARAM));
		return data;
	}
	
	private XPanel headingPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			XLabel stemLeafLabel = new XLabel(translate("Stem and leaf plot"), XLabel.LEFT, this);
			stemLeafLabel.setFont(getStandardBoldFont());
		thePanel.add("Center", stemLeafLabel);
		
		
			XLabel dataLabel = new XLabel(translate("Data values"), XLabel.LEFT, this);
			dataLabel.setFont(getStandardBoldFont());
		thePanel.add("East", dataLabel);
		
		return thePanel;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			stemLeaf = new CreateStemLeafView(data, this, getParameter(STEM_AXIS_PARAM));
			stemLeaf.lockBackground(Color.white);
		thePanel.add("Center", stemLeaf);
		
		return thePanel;
	}
	
	private XPanel valuePanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
			
			StringTokenizer st = new StringTokenizer(getParameter(ROW_COL_INFO_PARAM));
			int nRow = Integer.parseInt(st.nextToken());
			int nCol = Integer.parseInt(st.nextToken());
			theList = new SLValueListView(data, this, stemLeaf, nRow, nCol);
			theList.lockBackground(Color.white);
		thePanel.add("Center", theList);
		
		return thePanel;
	}
	
	private XPanel controlPanel() {
		XPanel thePanel = new InsetPanel(0, 10, 0, 0);
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
			sortLeavesButton = new XButton(translate("Sort Leaves"), this);
		thePanel.add(sortLeavesButton);
		
			resetButton = new XButton(translate("Reset"), this);
		thePanel.add(resetButton);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == resetButton) {
			theList.resetList();
			return true;
		}
		else if (target == sortLeavesButton) {
			theList.selectValue(-1);
			stemLeaf.sortLeaves();
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}