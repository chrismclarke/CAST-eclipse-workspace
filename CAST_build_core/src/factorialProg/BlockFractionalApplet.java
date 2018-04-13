package factorialProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;

import factorial.*;


public class BlockFractionalApplet extends XApplet {
	static final private String ALL_TERMS_PARAM = "allTerms";
	static final private String N_COMPLETE_PARAM = "nComplete";
	static final private String HIGH_LEVELS_PARAM = "highLevels";
	static final private String BLOCK_NAMES_PARAM = "blockNames";
	
	static final private Color kTermBackground = new Color(0xCCFFFF);
	
/*
	public void setupApplet() {
		StringTokenizer st = new StringTokenizer(getParameter(ALL_TERMS_PARAM));
		String varName[] = new String[st.countTokens()];
		for (int i=0 ; i<varName.length ;i++)
			varName[i] = st.nextToken();
		
		DataSet data = new DataSet();
			int nComplete = Integer.parseInt(getParameter(N_COMPLETE_PARAM));
			int nRows = (int)Math.pow(2, nComplete);
			double zeros[] = new double[nRows];
			NumVariable dummyVar = new NumVariable("Dummy");	//	to allow selections of rows
			dummyVar.setValues(zeros);
		data.addVariable("dummy", dummyVar);
		
		setLayout(new BorderLayout(10, 0));
			
			FractionalDesignMatrix designMatrix = new FractionalDesignMatrix(data, varName,
																																			nComplete, this);
			designMatrix.setShowBlocks(true);
			designMatrix.lockBackground(kTermBackground);
			designMatrix.setFont(getBigFont());
		add("Center", designMatrix);
			
			st = new StringTokenizer(getParameter(HIGH_LEVELS_PARAM));
			String highLevelNames[] = new String[st.countTokens()];
			for (int i=0 ; i<highLevelNames.length ; i++)
				highLevelNames[i] = st.nextToken();
			
			st = new StringTokenizer(getParameter(BLOCK_NAMES_PARAM), "#");
			String blockNames[] = new String[st.countTokens()];
			for (int i=0 ; i<blockNames.length ; i++)
				blockNames[i] = st.nextToken();
			FractionalBlockView blockView = new FractionalBlockView(data, highLevelNames,
																													blockNames, designMatrix, this);
//			blockView.lockBackground(Color.white);
			blockView.setFont(getBigFont());
		add("East", blockView);
	}
*/
	
	public void setupApplet() {
		StringTokenizer st = new StringTokenizer(getParameter(ALL_TERMS_PARAM));
		String varName[] = new String[st.countTokens()];
		for (int i=0 ; i<varName.length ;i++)
			varName[i] = st.nextToken();
		
		int nComplete = Integer.parseInt(getParameter(N_COMPLETE_PARAM));
		int nRows = (int)Math.pow(2, nComplete);
		double zeros[] = new double[nRows];
		
		st = new StringTokenizer(getParameter(HIGH_LEVELS_PARAM));
		String highLevelNames[] = new String[st.countTokens()];
		for (int i=0 ; i<highLevelNames.length ; i++)
			highLevelNames[i] = st.nextToken();
	
		st = new StringTokenizer(getParameter(BLOCK_NAMES_PARAM), "#");
		int nReps = st.countTokens();
		
		if (nReps == 1) {
			setLayout(new BorderLayout(0, 0));
			String blockNames = st.nextToken();
			add("Center", replicatePanel(varName, highLevelNames, nComplete, blockNames, zeros, 0));
		}
		else {
			setLayout(new ProportionLayout(0.5, 10, ProportionLayout.VERTICAL));
			String blockNames = st.nextToken();
			add(ProportionLayout.TOP, replicatePanel(varName, highLevelNames, nComplete,
																																	blockNames, zeros, 0));
			int nBlocks = 1;
			for (int i=0 ; i<varName.length-nComplete ; i++)
				nBlocks *= 2;
			blockNames = st.nextToken();
			add(ProportionLayout.BOTTOM, replicatePanel(varName, highLevelNames, nComplete,
																																	blockNames, zeros, nBlocks));
		}
	}
	
	private XPanel replicatePanel(String[] varName, String[] highLevelNames, int nComplete,
																			String blockNamesString, double[] zeros, int blockColorOffset) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(10, 0));
		
		DataSet data = new DataSet();
			NumVariable dummyVar = new NumVariable("Dummy");	//	to allow selections of rows
			dummyVar.setValues(zeros);
		data.addVariable("dummy", dummyVar);
		
			
			FractionalDesignMatrix designMatrix = new FractionalDesignMatrix(data, varName,
																																			nComplete, this);
			designMatrix.setShowBlocks(true);
			designMatrix.setBlockColorOffset(blockColorOffset);
			designMatrix.lockBackground(kTermBackground);
			designMatrix.setFont(getBigFont());
		thePanel.add("Center", designMatrix);
			
			StringTokenizer st = new StringTokenizer(blockNamesString, "*");
			String blockNames[] = new String[st.countTokens()];
			for (int i=0 ; i<blockNames.length ; i++)
				blockNames[i] = st.nextToken();
			FractionalBlockView blockView = new FractionalBlockView(data, highLevelNames,
																													blockNames, designMatrix, this);
			blockView.setBlockColorOffset(blockColorOffset);
			blockView.setFont(getBigFont());
		thePanel.add("East", blockView);
		
		return thePanel;
	}
}