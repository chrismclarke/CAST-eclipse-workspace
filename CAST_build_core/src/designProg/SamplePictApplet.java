package designProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import survey.*;


public class SamplePictApplet extends SamplingApplet {
	private XChoice pictTypeChoice;
	private int currentPictType = 0;
	private long popnRandomSeed;
	
	protected DataSet readData() {
		DataSet data = new DataSet();
		
		CatVariable v = new CatVariable(getParameter(CAT_NAME_PARAM));
		v.readLabels(getParameter(CAT_LABELS_PARAM));
		
		StringTokenizer popnTok = new StringTokenizer(getParameter(POPN_INFO_PARAM));
		rows = Integer.parseInt(popnTok.nextToken());
		cols = Integer.parseInt(popnTok.nextToken());
		popnRandomSeed = Long.parseLong(popnTok.nextToken());
		int vals[] = new int[rows * cols];
		v.setValues(vals);
		
		data.addVariable("y", v);
		
		return data;
	}
	
	protected XPanel displayPanel(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout());
		
			StringTokenizer sampleTok = new StringTokenizer(getParameter(SAMPLING_PARAM));
			int sampleSize = Integer.parseInt(sampleTok.nextToken());
			long randomSeed = Long.parseLong(sampleTok.nextToken());
			
			theView = new CatPictSamplingView(data, this, sampleSize,
										randomSeed, popnRandomSeed, rows, cols);
		thePanel.add("Center", theView);
		return thePanel;
	}
	
	protected XPanel topControlPanel(DataSet data) {
		XPanel proportionPanel = new XPanel();
		
		pictTypeChoice = new XChoice(this);
		pictTypeChoice.addItem("People");
		pictTypeChoice.addItem("Boxes");
		pictTypeChoice.addItem("Flowers");
		proportionPanel.add(pictTypeChoice);
		
		return proportionPanel;
	}
	
	private boolean localAction(Object target) {
		if (target == pictTypeChoice) {
			int newChoice = pictTypeChoice.getSelectedIndex();
			if (currentPictType != newChoice) {
				((CatPictSamplingView)theView).setUnitType(newChoice);
//				setPopSamp(0);						//		show population
				currentPictType = newChoice;
			}
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (super.action(evt, what))
			return true;
		else
			return localAction(evt.target);
	}
}