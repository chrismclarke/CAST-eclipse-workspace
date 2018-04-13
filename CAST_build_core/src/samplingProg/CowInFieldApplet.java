package samplingProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import random.*;

import sampling.*;


public class CowInFieldApplet extends XApplet {
	static final private String NO_OF_COWS_PARAM = "noOfCows";
	
	static final private int[] gridSize = {5, 10, 30, -1};
	static final private int[] gridCircleRadius = {7, 4, 2, 0};
	
	static final private double kCowOffset = 0.015;
	
	static double[] createGrid(int nVals) {
		double[] grid = new double[nVals];
		for (int i=0 ; i<nVals ; i++)
			grid[i] = (i + 0.5) / nVals;
		return grid;
	}
	
	private DataSet data;
	private int nVals;
	private double[] grid = createGrid(gridSize[0]);
	private Random generator;
	
	private CowInFieldView theView;
	
	private XButton sampleButton;
	private XChoice gridChoice;
	private int currentGridIndex;
	
	public void setupApplet() {
		data = getData();
		doTakeSample();
		
		setLayout(new BorderLayout(0, 10));
			
		add("Center", dataPanel(data));
		add("South", controlPanel());
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		nVals = Integer.parseInt(getParameter(NO_OF_COWS_PARAM));
		generator = new Random();
		
		NumVariable xVar = new NumVariable("X");
		data.addVariable("x", xVar);
		
		NumVariable yVar = new NumVariable("Y");
		data.addVariable("y", yVar);
		
		return data;
	}
	
	private XPanel dataPanel(DataSet data) {
		theView = new CowInFieldView(data, this, "x", "y");
		theView.setGrid(grid, grid, gridCircleRadius[0]);
		return theView;
	}
	
	protected XPanel controlPanel() {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		
			sampleButton = new XButton(translate("Take sample"), this);
		thePanel.add(sampleButton);
			
			gridChoice = new XChoice(translate("Grid") + " =", XChoice.HORIZONTAL, this);
			gridChoice.addItem(translate("Coarse"));
			gridChoice.addItem(translate("Fine"));
			gridChoice.addItem(translate("Very fine"));
			gridChoice.addItem(translate("Infinite"));
		thePanel.add(gridChoice);
		
		return thePanel;
	}
	
	protected void doTakeSample() {
		double[] xVal = new double[nVals];
		double[] yVal = new double[nVals];
		
		if (grid == null)
			for (int i=0 ; i<nVals ; i++) {
				xVal[i] = generator.nextDouble();
				yVal[i] = generator.nextDouble();
			}
		else {
			int popnSize = grid.length * grid.length;
			
			int sample[] = new RandomInteger(0, popnSize - 1, nVals).generate();
			int[] repeats = new int[popnSize];
			for (int i=0 ; i<sample.length ; i++)
				repeats[sample[i]] ++;
			
			for (int i=0 ; i<sample.length ; i++) {
				int gridIndex = sample[i];
				
				xVal[i] = grid[gridIndex / grid.length];
				yVal[i] = grid[gridIndex % grid.length];
				
				if (repeats[gridIndex] > 1) {
					xVal[i] -= kCowOffset;
					yVal[i] -= kCowOffset;
					repeats[gridIndex] --;
				}
			}
		}
		
		NumVariable xVar = (NumVariable)data.getVariable("x");
		xVar.setValues(xVal);
		
		NumVariable yVar = (NumVariable)data.getVariable("y");
		yVar.setValues(yVal);
		
		data.variableChanged("x");
	}

	
	private boolean localAction(Object target) {
		if (target == sampleButton) {
			doTakeSample();
			return true;
		}
		else if (target == gridChoice) {
			int newChoice = gridChoice.getSelectedIndex();
			if (newChoice != currentGridIndex) {
				currentGridIndex = newChoice;
				int newSize = gridSize[newChoice];
				grid = (newSize < 0) ? null : createGrid(newSize);
				theView.setGrid(grid, grid, gridCircleRadius[newChoice]);
				doTakeSample();
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