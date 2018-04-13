package exper2Prog;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;

import exper2.*;


public class BlockTransitionApplet extends XApplet {
	static final private String BLOCK_NAMES_PARAM = "blockNames";
	static final private String FACTOR_NAMES_PARAM = "factorNames";
	
	private BlockTransitionView theView;
	
	private int noOfSteps;
	private XNoValueSlider transitionSlider;
	
	private LabelValue[] readNames(String paramName) {
		StringTokenizer st = new StringTokenizer(getParameter(paramName));
		int n = st.countTokens();
		LabelValue[] names = new LabelValue[n];
		for (int i=0 ; i<n ; i++)
			names[i] = new LabelValue(st.nextToken());
		return names;
	}
	
	private void generatePermutation(int[] values, int startIndex, int nItems, Random rand01) {
		values[startIndex] = 0;				//	does not use previous values in array
		for (int i=1 ; i<nItems ; i++) {
			int j = (int)Math.round((i + 1) * rand01.nextDouble() - 0.5);
			if (i == j)
				values[startIndex + i] = i;
			else {
				values[startIndex + i] = values[startIndex + j];
				values[startIndex + j] = i;
			}
		}
	}
	
	private void setConstants(int[] values, int startIndex, int nItems, int newValue) {
		for (int i=0 ; i<nItems ; i++)
			values[startIndex + i] = newValue;
	}
	
	public void setupApplet() {
		Random rand01 = new Random();
		
		LabelValue[] blockNames = readNames(BLOCK_NAMES_PARAM);
		int nBlocks = blockNames.length;
		LabelValue[] factorNames = readNames(FACTOR_NAMES_PARAM);
		int nLevels = factorNames.length;
		if (nBlocks % nLevels != 0)
			throw new RuntimeException("The number of blocks must be a multiple of the number of factor levels.");
		
		int startLevels[] = new int[nBlocks * nLevels];
		for (int i=0 ; i<nBlocks ; i++)
			generatePermutation(startLevels, i * nLevels, nLevels, rand01);
		
		int endLevels[] = new int[nBlocks * nLevels];
		int blockPerm[] = new int[nBlocks];
		generatePermutation(blockPerm, 0, nBlocks, rand01);
		for (int i=0 ; i<nBlocks ; i++) {
			int block = blockPerm[i];
			setConstants(endLevels, block * nLevels, nLevels, i % nLevels);
		}
		
		setLayout(new BorderLayout(0, 10));
			Font f = getBigBoldFont();
			
			XPanel topPanel = new XPanel();
			topPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				XLabel blockLabel = new XLabel(translate("Blocks"), XLabel.LEFT, this);
				blockLabel.setFont(f);
			topPanel.add(blockLabel);
			
		add("North", topPanel);
			
			theView = new BlockTransitionView(startLevels, endLevels, blockNames, factorNames);
			theView.setFont(f);
			theView.setFactorFont(new Font(f.getName(), f.getStyle(), f.getSize() * 4 / 3));
			noOfSteps = theView.noOfSteps();
		add("Center", theView);
		
		add("South", controlPanel());
	}
	
	private XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
		transitionSlider = new XNoValueSlider(translate("Orthogonal"), translate("Confounded"),
																translate("Treatments and blocks are") + "...", 0, noOfSteps, 0, this);
		thePanel.add(transitionSlider);
		
		return thePanel;
	}

	
	private boolean localAction(Object target) {
		if (target == transitionSlider) {
			theView.showStep(transitionSlider.getValue());
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		return localAction(evt.target);
	}
}