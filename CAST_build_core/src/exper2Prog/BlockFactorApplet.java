package exper2Prog;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;

import exper2.*;


public class BlockFactorApplet extends XApplet {
	static final private String NO_OF_REPS_PARAM = "nReps";
	static final private String NO_OF_DISPLAY_COLS_PARAM = "nDisplayCols";
	static final private String RANDOM_SEED_PARAM = "randomSeed";
	static final private String BLOCK_NAMES_PARAM = "blockNames";
	static final private String ICON_TYPE_PARAM = "iconType";
	
	private BlockFactorView theView;
	
	private XButton randomiseButton;
	private XChoice designChoice;
	private int currentDesign = 0;
	
	public void setupApplet() {
		DataSet data = new DataSet();
		
		setLayout(new BorderLayout(0, 10));
			
		add("Center", displayPanel(data));
			
		add("South", controlPanel());
	}
	
	private XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
			
			StringTokenizer st = new StringTokenizer(getParameter(BLOCK_NAMES_PARAM), "#");
			int nBlocks = st.countTokens();
			String blockNames[] = new String[nBlocks];
			for (int i=0 ; i<nBlocks ; i++)
				blockNames[i] = st.nextToken();
			int nReps = Integer.parseInt(getParameter(NO_OF_REPS_PARAM));
			int nDisplayCols = Integer.parseInt(getParameter(NO_OF_DISPLAY_COLS_PARAM));
			long randSeed = Long.parseLong(getParameter(RANDOM_SEED_PARAM));
			
			String iconTypeString = getParameter(ICON_TYPE_PARAM);
			int iconType = (iconTypeString != null && iconTypeString.equals("cornEmzyme"))
																					? BlockFactorView.CORN_EMZYME : BlockFactorView.GRASS_RAIN;
			
			theView = new BlockFactorView(data, this, blockNames, nReps, nDisplayCols, randSeed, iconType) ;
				theView.lockBackground(Color.white);
				theView.setFont(getBigBoldFont());
				
		thePanel.add("Center", theView);
		
		return thePanel;
	}
	
	
	protected XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));
		
			designChoice = new XChoice(translate("Design"), XChoice.VERTICAL_CENTER, this);
			designChoice.addItem(translate("Completely randomised"));
			designChoice.addItem(translate("Randomised block"));
		
		thePanel.add(designChoice);
		
			randomiseButton = new XButton(translate("Randomise treatments"), this);
		thePanel.add(randomiseButton);
		
		return thePanel;
	}
	
	protected void frameChanged(DataView theView) {
		if (theView.getCurrentFrame() == 0) {
			randomiseButton.enable();
			designChoice.enable();
		}
	}

	
	private boolean localAction(Object target) {
		if (target == randomiseButton) {
			theView.animateRandomise();
			if (theView.getCurrentFrame() != 0) {
				randomiseButton.disable();
				designChoice.disable();
			}
			return true;
		}
		if (target == designChoice) {
			int newChoice = designChoice.getSelectedIndex();
			if (newChoice != currentDesign) {
				currentDesign = newChoice;
				theView.animateShowBlocks(newChoice == 1);
				if (theView.getCurrentFrame() != 0) {
					randomiseButton.disable();
					designChoice.disable();
				}
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