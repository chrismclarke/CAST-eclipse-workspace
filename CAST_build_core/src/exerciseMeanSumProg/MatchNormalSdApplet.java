package exerciseMeanSumProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import axis.*;
import utils.*;
import exercise2.*;
import distn.*;

import exerciseMeanSum.*;


public class MatchNormalSdApplet extends CoreMatchApplet {
	static final private String[] kYKeys = {"y0", "y1", "y2", "y3"};
	
	static final private int kNSampleSizes = 4;
	
	private MultipleNormalDistnView densities, sampleSizes;
	private HorizAxis axis;
	
	private Random random01;
	
	protected void createDisplay() {
		random01 = new Random(nextSeed());
		
		setLayout(new BorderLayout(0, 10));
		
			questionPanel = new QuestionPanel(this);
		add("North", questionPanel);
		
			XPanel localWorkingPanel = getWorkingPanels(data);		//	CoreMatchApplet has variable workingPanel
		add("Center", localWorkingPanel);
				
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
		registerParameter("mean-sum", "choice");
		registerParameter("axis", "string");
		registerParameter("baseN", "int");
	}
	
	private boolean isMeanNotSum() {
		return getIntParam("mean-sum") == 0;
	}
	
	private String getAxisInfo() {
		return getStringParam("axis");
	}
	
	private int getBaseN() {
		return getIntParam("baseN");
	}
	
	
//-----------------------------------------------------------
	
	protected int noOfItems() {
		return kNSampleSizes;
	}
	
	protected boolean retainFirstItems() {
		return false;
	}
	
	protected int getDragMatchHeight() {
		return densities.getSize().height;
	}
	
	protected void setWorkingPanelLayout(XPanel thePanel) {
		thePanel.setLayout(new BorderLayout(40, 0));
	}
	
	protected XPanel addLeftItems(XPanel thePanel, int[] leftOrder) {
			XPanel sampleSizesPanel = new InsetPanel(0, 0, 0, 1);
			sampleSizesPanel.setLayout(new BorderLayout(0, 0));
			
				sampleSizes = new MultipleNormalDistnView(data, this, null, kYKeys,
																			leftOrder, MultipleNormalDistnView.SAMPLE_SIZES);
				sampleSizes.setFixedTopDistn(false);
				sampleSizes.setDrawGrid(false);
				registerStatusItem("sampleSizePerm", sampleSizes);
				
			sampleSizesPanel.add("Center", sampleSizes);
			
				XPanel spacerPanel = new XPanel();
				spacerPanel.setLayout(new FixedSizeLayout(0, 50));
				
				spacerPanel.add(new XPanel());
				
			sampleSizesPanel.add("South", spacerPanel);
				
		thePanel.add("West", sampleSizesPanel);
		
		return sampleSizesPanel;
	}
	
	protected XPanel addRightItems(XPanel thePanel, int[] rightOrder) {
			XPanel densitiesPanel = new InsetPanel(0, 0, 0, 1);
			densitiesPanel.setLayout(new AxisLayout());
			
				axis = new HorizAxis(this);
			densitiesPanel.add("Bottom", axis);
				
				densities = new MultipleNormalDistnView(data, this, axis, kYKeys,
																	rightOrder, MultipleNormalDistnView.DENSITY);
				densities.lockBackground(Color.white);
				densities.setFixedTopDistn(false);
				densities.setDrawGrid(true);
				registerStatusItem("densityPerm", densities);
				
			densitiesPanel.add("Center", densities);
			
		thePanel.add("Center", densitiesPanel);
		
		return densitiesPanel;
	}
	
	
//-----------------------------------------------------------

	
	protected void setDisplayForQuestion() {
		super.setDisplayForQuestion();
		
		axis.readNumLabels(getAxisInfo());
		axis.setAxisName(isMeanNotSum() ? "Sample mean" : "Sum of sample values");
		axis.invalidate();
		
		densities.resetDisplay();
		densities.repaint();
		
		sampleSizes.invalidate();
		sampleSizes.repaint();
	}
	
	protected void setDataForQuestion() {
		StringTokenizer st = new StringTokenizer(getAxisInfo());
		double axisMin = Double.parseDouble(st.nextToken());
		double axisMax = Double.parseDouble(st.nextToken());
		double mean = (axisMax + axisMin) / 2;
		double baseSd = (axisMax - axisMin) / 6;
		int baseN = getBaseN();
		
		for (int i=0 ; i<kYKeys.length ; i++) {
			NormalDistnVariable yVar = (NormalDistnVariable)data.getVariable(kYKeys[i]);
			
			int n = meanSampleSize(i, baseN);
			yVar.name = isMeanNotSum() ? ("Mean of " + n) : ("Total of " + n);
			
			int generatorN = meanSampleSize(isMeanNotSum() ? i : (kYKeys.length - i - 1), 1);
			
			double sd = baseSd / Math.sqrt(generatorN);
			
			yVar.setMean(mean);
			yVar.setSD(sd);
		}
		
		super.setDataForQuestion();
	}
	
	private int meanSampleSize(int index, int baseN) {
		int n = baseN;
		for (int i=0 ; i<index ; i++)
			n *= 2;
		return n;
	}
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Reorder the densities by dragging so that their shapes are the distributions of sample means from the sample sizes on the left.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("The correct matching of densities and sample sizes is shown.");
				messagePanel.insertText("\nWhen the sample size becomes bigger, the spread of the ");
				messagePanel.insertText(isMeanNotSum() ? "sample mean's distribution becomes smaller." : "sum of the sample values becomes bigger.");
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have correctly matched up the densities with the sample sizes.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertText("The red arrows indicate errors.");
				messagePanel.insertText("\nWhen the sample size becomes bigger, the spread of the ");
				messagePanel.insertText(isMeanNotSum() ? "sample mean's distribution becomes smaller." : "sum of the sample values becomes bigger.");
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 120;
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		for (int i=0 ; i<kYKeys.length ; i++)
			data.addVariable(kYKeys[i], new NormalDistnVariable(""));
				
		return data;
	}
	
	public void setFixedQuestionSeed(long seed) {
		super.setFixedQuestionSeed(seed);
		random01.setSeed(nextSeed());
	}
	
	protected void showCorrectWorking() {
		super.showCorrectWorking();
		
		densities.repaint();
	}
	
}