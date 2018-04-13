package exper2Prog;

import java.awt.*;
import java.util.*;

import dataView.*;

import exper2.*;


public class RandomiseIncompleteApplet extends RandomiseLatinApplet {
	static final private String BLOCK_SIZE_PARAM = "blockSize";
	static final private String BASE_DESIGN_PARAM = "baseDesign";
	
	
	protected String permRowsButtonName() {
		return translate("Permute") + " " + translate("blocks");
	}
	
	protected String permColsButtonName() {
		return null;
	}
	
	protected String permTreatsButtonName() {
		return translate("Permute") + " " + translate("treatments");
	}
	
	protected XPanel rowColPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
			LabelValue treatName[] = readLevels(TREATMENT_NAMES_PARAM);
			LabelValue rowName[] = readLevels(ROW_NAMES_PARAM);
			int nCols = Integer.parseInt(getParameter(BLOCK_SIZE_PARAM));
			
			int[][] baseDesign = new int[rowName.length][nCols];
			StringTokenizer st = new StringTokenizer(getParameter(BASE_DESIGN_PARAM), "#");
			for (int i=0 ; i<rowName.length ; i++) {
				StringTokenizer st2 = new StringTokenizer(st.nextToken());
				for (int j=0 ; j<nCols ; j++)
					baseDesign[i][j] = Integer.parseInt(st2.nextToken());
			}
			
			designTable = new IncompleteDesignView(rowName, nCols, baseDesign, treatName, this);
			designTable.setFont(getBigBoldFont());
			
		thePanel.add("Center", designTable);
		
		return thePanel;
	}
}