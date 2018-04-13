package factorialProg;

import java.awt.*;
import java.util.*;

import dataView.*;

import factorial.*;


public class AddFractionalApplet extends XApplet {
	static final private String ALL_TERMS_PARAM = "allTerms";
	static final private String N_COMPLETE_PARAM = "nComplete";
	static final private String HIGH_LEVELS_PARAM = "highLevels";
	static final private String COLS_ROWS_PARAM = "colsRows";
	static final private String SHOW_TREATS_PARAM = "showTreats";
	
	static final private Color kTermBackground = new Color(0xCCFFFF);
	
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
		
		setLayout(new BorderLayout(0, 10));
			
			FractionalDesignMatrix designMatrix = new FractionalDesignMatrix(data, varName, nComplete,
																																							this);
			designMatrix.lockBackground(kTermBackground);
			designMatrix.setFont(getBigFont());
		add("Center", designMatrix);
			
		String showTreatsString = getParameter(SHOW_TREATS_PARAM);
		if (showTreatsString != null && showTreatsString.equals("true")) {
			XPanel treatmentsPanel = new XPanel();
			treatmentsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
			
				st = new StringTokenizer(getParameter(HIGH_LEVELS_PARAM));
				String highLevelNames[] = new String[st.countTokens()];
				for (int i=0 ; i<highLevelNames.length ; i++)
					highLevelNames[i] = st.nextToken();
					
				st = new StringTokenizer(getParameter(COLS_ROWS_PARAM));
				int listCols = Integer.parseInt(st.nextToken());
				int listRows = Integer.parseInt(st.nextToken());
				
				TreatmentsView treatView = new TreatmentsView(data, highLevelNames, designMatrix,
																																		listCols, listRows, this);
				treatView.lockBackground(kTermBackground);
				treatView.setFont(getBigFont());
			
			treatmentsPanel.add(treatView);
		
			add("South", treatmentsPanel);
		}
	}
}