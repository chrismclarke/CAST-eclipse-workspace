package exper2Prog;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;

import exper2.*;
import formula.*;


public class RandomiseBlockApplet extends XApplet {
	static final private String BLOCKS_PARAM = "blocks";
	static final private String TABLE_COLS_PARAM = "tableCols";
	static final private String TREATMENT_NAMES_PARAM = "treatmentNames";
	static final private String BLOCK_NAMES_PARAM = "blockNames";
	
	private DataSet data;
	
	private RandomiseBlockView theView;
	
	private XButton randomiseButton;
	private XChoice designChoice;
	private int currentDesign = 0;
	private XChoice blocksChoice;
	private int currentBlocks = 0;
	
	public void setupApplet() {
		data = new DataSet();
		
		setLayout(new BorderLayout(0, 30));
		
		add("Center", displayPanel(data));
		add("South", controlPanel());
	}
	
	private XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 3));
		
			XPanel designPanel = new XPanel();
			designPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 00));
			
				designChoice = new XChoice(translate("Design") + " =", XChoice.HORIZONTAL, this);
				designChoice.addItem(translate("Completely randomised"));
				designChoice.addItem(translate("Randomised block"));
			
			designPanel.add(designChoice);
			
				blocksChoice = new XChoice(this);
				StringTokenizer st = new StringTokenizer(getParameter(BLOCK_NAMES_PARAM), "#");
				while (st.hasMoreTokens())
					blocksChoice.addItem(st.nextToken());
			designPanel.add(blocksChoice);
			
		thePanel.add(designPanel);
		
			randomiseButton = new XButton(translate("Allocate treatments"), this);
		
		thePanel.add(randomiseButton);
		
		return thePanel;
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			StringTokenizer st = new StringTokenizer(getParameter(TREATMENT_NAMES_PARAM), "*");
			Value treat[] = new Value[st.countTokens()];
			for (int i=0 ; i<treat.length ; i++)
				treat[i] = new LabelValue(MText.expandText(st.nextToken()));
			
			st = new StringTokenizer(getParameter(BLOCKS_PARAM + 1));
			int nBlocks = Integer.parseInt(st.nextToken());
			int blockRows = Integer.parseInt(st.nextToken());
			int nCols = Integer.parseInt(getParameter(TABLE_COLS_PARAM));
																																		
			theView = new RandomiseBlockView(data, this, treat, nBlocks, blockRows, nCols);
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	private boolean localAction(Object target) {
		if (target == randomiseButton) {
			theView.animatePermutation();
			return true;
		}
		else if (target == designChoice) {
			int newChoice = designChoice.getSelectedIndex();
			if (newChoice != currentDesign) {
				currentDesign = newChoice;
				theView.setBlockPerm(newChoice == 1);
				theView.setFrame(0);
			}
			return true;
		}
		else if (target == blocksChoice) {
			int newChoice = blocksChoice.getSelectedIndex();
			if (newChoice != currentBlocks) {
				currentBlocks = newChoice;
				
				StringTokenizer st = new StringTokenizer(getParameter(BLOCKS_PARAM + (newChoice + 1)));
				int nBlocks = Integer.parseInt(st.nextToken());
				int blockRows = Integer.parseInt(st.nextToken());
				
				theView.setBlocks(nBlocks, blockRows);
				theView.setFrame(0);
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