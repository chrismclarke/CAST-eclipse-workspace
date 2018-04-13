package bivarCatProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;

import bivarCat.*;
import cat.CountPropnAxis;


public class Bar2WayApplet extends Core2WayApplet {
	static final private String COUNT_AXIS_INFO_PARAM = "countAxis";
	static final private String PROPN_AXIS_INFO_PARAM = "propnAxis";
	
	static final private String STACKED_PARAM = "stackable";
	static final private String MAIN_GROUP_PARAM = "mainGroup";
																//	0 = groupByX, 1 = groupByY

	static final private String BOTTOM_PANEL_PARAM = "bottomPanel";
	
	protected TwoWayView barView;
	protected ContinTableView tableView = null;
	
	private boolean canStack = false;
	private boolean canGroupByX = false;
	private boolean canGroupByY = false;
	
	private boolean hasBottomPanel = false;
	protected int initialMainGrouping;
	
	protected Axis horizAxis;
	private XChoice mainGroupingChoice;
	private XCheckbox stackedCheck;
	
	
	protected void addDisplayComponents(DataSet data) {
		setLayout(new BorderLayout(10, 0));
		
		add("Center", displayPanel(data));
		
		if (barView != null)
			barView.setDisplayType(initialMainGrouping, initialVertScale, false);
		
		add("East", controlPanel(data));
		if (hasBottomPanel)
			add("South", bottomPanel(data));
		
		if (tableView != null)
			tableView.setDisplayType(initialMainGrouping, initialVertScale, false);
		
	}
	
	protected void readOptions() {
		super.readOptions();
		
		String stackingString = getParameter(STACKED_PARAM);
		canStack = stackingString != null && stackingString.equals("true");
		
		StringTokenizer mainGroupInfo = new StringTokenizer(getParameter(MAIN_GROUP_PARAM));
		while (mainGroupInfo.hasMoreTokens()) {
			int index = Integer.parseInt(mainGroupInfo.nextToken());
			if (index == 0)
				canGroupByX = true;
			else if (index == 1)
				canGroupByY = true;
		}
		initialMainGrouping = canGroupByX ? TwoWayView.XMAIN : TwoWayView.YMAIN;
		
		String bottomString = getParameter(BOTTOM_PANEL_PARAM);
		hasBottomPanel = bottomString != null && bottomString.equals("true");
	}
	
	protected CountPropnAxis createCountPropnAxis() {
		StringTokenizer countAxisInfo = new StringTokenizer(getParameter(COUNT_AXIS_INFO_PARAM));
		int maxCount = Integer.parseInt(countAxisInfo.nextToken());
		int labelStep = Integer.parseInt(countAxisInfo.nextToken());
		
		StringTokenizer propnAxisInfo = new StringTokenizer(getParameter(PROPN_AXIS_INFO_PARAM));
		String maxPropn = propnAxisInfo.nextToken();
		String propnStep = propnAxisInfo.nextToken();
		
		CountPropnAxis vertAxis = new CountPropnAxis(this);
		vertAxis.setUpAxes(maxCount, labelStep, maxPropn, propnStep);
		
		return vertAxis;
	}
	
	protected XPanel barchartPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new AxisLayout());
		
			CountPropnAxis vertAxis = createCountPropnAxis();
		thePanel.add("Left", vertAxis);
		
			MultiHorizAxis multiCatAxis = new MultiHorizAxis(this, 2);
			multiCatAxis.setCatLabels((CatVariable)xVariable);
			multiCatAxis.readExtraCatLabels((CatVariable)yVariable);
			horizAxis = multiCatAxis;
		thePanel.add("Bottom", multiCatAxis);
		
			barView = new Bar2WayView(data, this, vertAxis, horizAxis, "x", "y");
			barView.lockBackground(Color.white);
		thePanel.add("Center", barView);
		return thePanel;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
		thePanel.add("Center", barchartPanel(data));
		thePanel.add("North", vertScalePanel(data, null));
		thePanel.add("South", mainGroupingPanel(data));
		
		return thePanel;
	}
	
	protected XPanel bottomPanel(DataSet data) {
		XPanel thePanel = new InsetPanel(0, 15, 0, 0);
		thePanel.setLayout(new BorderLayout(0, 8));
//		thePanel.add("North", new Separator(0.8));
		
		String decimalString = getParameter(DECIMALS_PARAM);
		int decimals = (decimalString == null) ? 3 : Integer.parseInt(decimalString);
		
		XPanel tablePanel = new XPanel();
		tablePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		tableView = new ContinTableView(data, this, "x", "y", decimals, true, true);
		tablePanel.add(tableView);
		
		thePanel.add("Center", tablePanel);
		return thePanel;
	}
	
	protected XPanel mainGroupingPanel(DataSet data) {
		XPanel mainChoicePanel = new XPanel();
		mainChoicePanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		if (canGroupByX && canGroupByY) {
			
//			mainChoicePanel.add(new XLabel("Cluster bars by:", XLabel.LEFT, this));
			
			mainGroupingChoice = new XChoice(this);
			mainGroupingChoice.addItem(xVariable.name);
			mainGroupingChoice.addItem(yVariable.name);
			mainGroupingChoice.select(0);
			mainChoicePanel.add(mainGroupingChoice);
		}
		else {
			XLabel horizLabel = new XLabel(canGroupByX ? xVariable.name : yVariable.name,
																		XLabel.RIGHT, this);
			horizLabel.setFont(getBigFont());
			mainChoicePanel.add(horizLabel);
		}
		return mainChoicePanel;
	}
	
	protected XPanel stackPanel(DataSet data) {
		if (canStack) {
			XPanel checkPanel = new XPanel();
			checkPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
			stackedCheck = new XCheckbox(translate("Stacked"), this);
			checkPanel.add(stackedCheck);
			return checkPanel;
		}
		else
			return null;
	}
	
	protected XPanel controlPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
		XPanel stack = stackPanel(data);
		if (stack != null)
			thePanel.add(stack);
		
		return thePanel;
	}
	
	private boolean canStack(int mainGrouping, int vertScale) {
		return (mainGrouping != TwoWayView.XMAIN
						|| (vertScale != TwoWayView.PROPN_IN_Y && vertScale != TwoWayView.PERCENT_IN_Y))
				&& (mainGrouping != TwoWayView.YMAIN
						|| (vertScale != TwoWayView.PROPN_IN_X && vertScale != TwoWayView.PERCENT_IN_X));
	}
	
	private void setCheckHilite(boolean stackable) {
		if (stackedCheck != null) {
			if (stackable)
				stackedCheck.enable();
			else {
				stackedCheck.setState(false);
				stackedCheck.disable();
			}
		}
	}
	
	protected void changeVertScale(int newVertScale) {
//		if (!canScaleCount)
//			newVertScale ++;
//		if (!canScalePropnInX && newVertScale == 1)
//			newVertScale ++;
		if (tableView != null && newVertScale != tableView.getVertScale())
			tableView.setDisplayType(tableView.getMainGrouping(), newVertScale, false);
		
		if (barView != null && newVertScale != barView.getVertScale()) {
			boolean stackable = canStack(barView.getMainGrouping(), newVertScale);
			barView.setDisplayType(barView.getMainGrouping(), newVertScale,
																			stackable && barView.getStacked());
			setCheckHilite(stackable);
		}
	}
	
	private void checkTableMarginHilite() {
		if (tableView == null)
			return;
		boolean stacked = stackedCheck != null && stackedCheck.getState();
		boolean counts = barView.getVertScale() == TwoWayView.COUNT;
		boolean xMainGrouping = barView.getMainGrouping() == TwoWayView.XMAIN;
		
		if (!stacked || !counts)
			tableView.setMarginHilite(ContinTableView.NO_HILITE);
		else
			tableView.setMarginHilite(xMainGrouping ? ContinTableView.X_HILITE : ContinTableView.Y_HILITE);
	}
	
	private boolean localAction(Object target) {
		if (target == stackedCheck && barView != null) {
			barView.setDisplayType(barView.getMainGrouping(), barView.getVertScale(),
					stackedCheck.getState());
			checkTableMarginHilite();
			return true;
		}
		else if (target == mainGroupingChoice && barView != null) {
			int newMainGrouping = mainGroupingChoice.getSelectedIndex();
			if (newMainGrouping != barView.getMainGrouping()) {
				boolean stackable = canStack(newMainGrouping, barView.getVertScale());
				barView.setDisplayType(newMainGrouping, barView.getVertScale(),
						stackable && barView.getStacked());
				if (horizAxis != null) {
					if (((MultiHorizAxis)horizAxis).setAlternateLabels(newMainGrouping))
						horizAxis.repaint();
				}
				setCheckHilite(stackable);
				checkTableMarginHilite();
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event  evt, Object  what) {
		if (super.action(evt, what)) {
			checkTableMarginHilite();
			return true;
		}
		else
			return localAction(evt.target);
	}
}