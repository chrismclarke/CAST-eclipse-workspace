package continProg;

import java.awt.*;

import dataView.*;
import utils.*;

import contin.*;


public class ConditCalcApplet extends XApplet {
	static final private String Y_VAR_NAME_PARAM = "yVarName";
	static final private String X_VAR_NAME_PARAM = "xVarName";
	
	static final private String Y_LABELS_PARAM = "yLabels";
	static final private String X_LABELS_PARAM = "xLabels";
	
	static final private String X_MARGIN_PARAM = "xMargin";
	static final private String Y_CONDIT_PARAM = "yCondit";
	
	static final private Color darkGreen = new Color(0x009900);
	
	static final private int kProbDecimals = 3;
	
	private YConditionalView yConditXTable;
	private XMarginalView xMarginTable;
	private JointView jointTable;
	private YMarginalView yMarginTable;
	private XConditionalView xConditYTable;
	
	private JointArrowView jointArrow;
	private MarginArrowView marginArrow;
	private ConditArrowView conditArrow;
	
	public void setupApplet() {
		DataSet data = readData();
		
		setLayout(new ProportionLayout(0.5, 5, ProportionLayout.VERTICAL, ProportionLayout.REMAINDER));
		
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new BorderLayout(0, 5));
			
				XPanel startPanel = new XPanel();
				startPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
				
					yConditXTable = new YConditionalView(data, this, "y", "x", kProbDecimals);
					yConditXTable.setForeground(Color.blue);
				startPanel.add(yConditXTable);
					
					xMarginTable = new XMarginalView(data, this, "y", "x", kProbDecimals);
					xMarginTable.setForeground(Color.blue);
				startPanel.add(xMarginTable);
			
			topPanel.add("North", startPanel);
			
				jointArrow = new JointArrowView(data, this, "y", "x", kProbDecimals);
				jointArrow.setLinkedTable(yConditXTable);
			topPanel.add("Center", jointArrow);
			
		add(ProportionLayout.TOP, topPanel);
		
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new BorderLayout(5, 0));
			
				XPanel bottomLeftPanel = new XPanel();
				bottomLeftPanel.setLayout(new BorderLayout(0, 5));
				
					jointTable = new JointView(data, this, "y", "x", kProbDecimals);
					jointTable.setForeground(darkGreen);
				bottomLeftPanel.add("North", jointTable);
					
					XPanel marginPanel = new XPanel();
					marginPanel.setLayout(new BorderLayout(5, 0));
						
						XPanel leftMarginPanel = new XPanel();
						leftMarginPanel.setLayout(new BorderLayout(0, 5));
						
							marginArrow = new MarginArrowView(data, this, "y", "x", kProbDecimals);
							marginArrow.setLinkedTable(jointTable);
						leftMarginPanel.add("Center", marginArrow);
						
							yMarginTable = new YMarginalView(data, this, "y", "x", kProbDecimals);
							yMarginTable.setForeground(Color.black);
							
						leftMarginPanel.add("South", yMarginTable);
						
					marginPanel.add("West", leftMarginPanel);
					
						conditArrow = new ConditArrowView(data, this, "y", "x", kProbDecimals, marginArrow);
						
					marginPanel.add("Center", conditArrow);
					
				bottomLeftPanel.add("Center", marginPanel);
			
			bottomPanel.add("Center", bottomLeftPanel);
			
				XPanel bottomRightPanel = new XPanel();
				bottomRightPanel.setLayout(new BorderLayout());
					
				bottomRightPanel.add("North", new XPanel());
				
					xConditYTable = new XConditionalView(data, this, "y", "x", kProbDecimals);
					xConditYTable.setForeground(Color.black);
				bottomRightPanel.add("South", xConditYTable);
				
			bottomPanel.add("East", bottomRightPanel);
			
		add(ProportionLayout.BOTTOM, bottomPanel);
	}
	
	private DataSet readData() {
		DataSet data = new DataSet();
		
		CatDistnVariable xVariable = new CatDistnVariable(getParameter(X_VAR_NAME_PARAM));
		xVariable.readLabels(getParameter(X_LABELS_PARAM));
		xVariable.setParams(getParameter(X_MARGIN_PARAM));
		data.addVariable("x", xVariable);
		
		ContinResponseVariable yVariable = new ContinResponseVariable(getParameter(Y_VAR_NAME_PARAM), data, "x");
		yVariable.readLabels(getParameter(Y_LABELS_PARAM));
		yVariable.setProbs(getParameter(Y_CONDIT_PARAM), ContinResponseVariable.CONDITIONAL);
		data.addVariable("y", yVariable);
		
		return data;
	}
	
	private boolean localAction(Object target, Object arg) {
		ContinCatInfo catInfo = (ContinCatInfo)arg;
		int newCol = (catInfo == null) ? -1 : catInfo.yIndex;
		int newRow = (catInfo == null) ? -1 : catInfo.xIndex;
		if (target == jointTable) {
			yConditXTable.setSelection(newRow, newCol);
			xMarginTable.setSelection(newRow, 0);
			jointArrow.setSelection(newRow, newCol);
			return true;
		}
		else if (target == yMarginTable) {
			jointTable.setSelection(CoreTableView.ALL_SELECTED, newCol);
			marginArrow.setSelection(-1, newCol);
			return true;
		}
		else if (target == xConditYTable) {
			yMarginTable.setSelection(0, newCol);
			jointTable.setSelection(newRow, newCol);
			conditArrow.setSelection(newRow, newCol);
			return true;
		}
		else
			return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target, evt.arg);
	}
}