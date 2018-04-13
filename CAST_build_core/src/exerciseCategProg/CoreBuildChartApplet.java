package exerciseCategProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import random.*;
import exercise2.*;

import exerciseCateg.*;


abstract public class CoreBuildChartApplet extends ExerciseApplet {
	protected FrequencyTableView freqTable;
	
	
//================================================
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 10));
		
			questionPanel = new QuestionPanel(this);
		add("North", questionPanel);
		
		add("Center", getWorkingPanels(data));
				
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 4));
			
			bottomPanel.add(createMarkingPanel(NO_HINTS));
			
				XPanel messagePanel = new XPanel();
				messagePanel.setLayout(new FixedSizeLayout(100, getMessageHeight()));
					
					message = new ExerciseMessagePanel(this);
				messagePanel.add(message);
			bottomPanel.add(messagePanel);
		
		add("South", bottomPanel);
	}
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
//		registerParameter("index", "int");					//	always registered
		registerParameter("varName", "string");
		registerParameter("categories", "string");
		registerParameter("counts", "string");
		registerParameter("probs", "string");
		registerParameter("nValues", "int");
	}
	
	protected String getVarName() {
		return getStringParam("varName");
	}
	
	protected Value[] getCategories() {
		StringTokenizer st = new StringTokenizer(getStringParam("categories"));
		Value label[] = new Value[st.countTokens()];
		for (int i=0 ; i<label.length ; i++) {
			String labelString = st.nextToken().replace('_', ' ');
			label[i] = new LabelValue(labelString);
		}
		
		return label;
	}
	
	protected int[] getCounts() {
		if (getObjectParam("counts") == null)
			return null;
			
		StringTokenizer st = new StringTokenizer(getStringParam("counts"));
		int counts[] = new int[st.countTokens()];
		for (int i=0 ; i<counts.length ; i++)
			counts[i] = Integer.parseInt(st.nextToken());
		
		return counts;
	}
	
	protected double[] getProbs() {
		if (getObjectParam("probs") == null)
			return null;
			
		StringTokenizer st = new StringTokenizer(getStringParam("probs"));
		double probs[] = new double[st.countTokens()];
		for (int i=0 ; i<probs.length ; i++)
			probs[i] = Double.parseDouble(st.nextToken());
		
		return probs;
	}
	
	protected int getNValues() {
		return getIntParam("nValues");
	}
	
	
//-----------------------------------------------------------
	
	abstract protected void setDisplayForQuestion();
	
	protected void setDataForQuestion() {
		CatVariable yVar = (CatVariable)data.getVariable("y");
		yVar.name = getVarName();
		yVar.setLabels(getCategories());
		int counts[] = getCounts();
		if (counts == null) {
			double probs[] = getProbs();
			int n = getNValues();
			RandomMultinomial generator = new RandomMultinomial(n, probs);
			generator.setSeed(nextSeed());
			
			double maxAllowedCount = getMaxAllowedCount();
			int maxCount;
			int iter = 0;
			do {
				counts = generator.generate();
				maxCount = 0;
				for (int i=0 ; i<counts.length ; i++)
					maxCount = Math.max(maxCount, counts[i]);
				iter ++;
			} while (maxCount > maxAllowedCount && iter < 10);
		}
		
		yVar.setCounts(counts);
	}
	
	protected double getMaxAllowedCount() {
		return Double.POSITIVE_INFINITY;
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			CatVariable yVar = new CatVariable("");
		data.addVariable("y", yVar);
		
		return data;
	}
	
//-----------------------------------------------------------
	
	public boolean noteChangedWorking() {
		boolean changed = super.noteChangedWorking();
		if (changed) {
			freqTable.clearSelection();
			freqTable.repaint();
		}
		return changed;
	}
	
}