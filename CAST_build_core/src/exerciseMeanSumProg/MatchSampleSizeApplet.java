package exerciseMeanSumProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import exercise2.*;
import distn.*;

import exerciseMeanSum.*;


public class MatchSampleSizeApplet extends CoreMatchApplet {
	static final private String[] kDistnKey = {"rect", "gamma", "rectMix", "normalMix", "expNormMix"};
	
	static final private int kNSampleSizes = 4;
	
	private MultipleMeanDistnView densities, sampleSizes;
	private XLabel axisLabel;
	
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
		registerParameter("shape", "choice");
		registerParameter("mean-sum", "choice");
	}
	
	protected int getShapeCode() {
		return getIntParam("shape");
	}
	
	protected boolean isMeanNotSum() {
		return getIntParam("mean-sum") == 0;
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
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
		thePanel.setLayout(new BorderLayout(0, 0));
		
		thePanel.add("Center", super.getWorkingPanels(data));
		
			axisLabel = new XLabel("", XLabel.RIGHT, this);
		thePanel.add("South", axisLabel);
		
		return thePanel;
	}
	
	protected void setWorkingPanelLayout(XPanel thePanel) {
		thePanel.setLayout(new BorderLayout(40, 0));
	}
	
	protected XPanel addLeftItems(XPanel thePanel, int[] leftOrder) {
			XPanel sampleSizesPanel = new InsetPanel(0, 0, 0, 1);
			sampleSizesPanel.setLayout(new BorderLayout(0, 0));
			
				sampleSizes = new MultipleMeanDistnView(data, this, 0.0, 1.0, null,
																			leftOrder, MultipleMeanDistnView.SAMPLE_SIZES);
				sampleSizes.setFixedTopDistn(false);
				registerStatusItem("sampleSizePerm", sampleSizes);
				
			sampleSizesPanel.add("Center", sampleSizes);
				
		thePanel.add("West", sampleSizesPanel);
		
		return sampleSizesPanel;
	}
	
	protected XPanel addRightItems(XPanel thePanel, int[] rightOrder) {
			XPanel densitiesPanel = new InsetPanel(0, 0, 0, 1);
			densitiesPanel.setLayout(new BorderLayout(0, 0));
				
				densities = new MultipleMeanDistnView(data, this, 0.0, 1.0, null,
																	rightOrder, MultipleMeanDistnView.DENSITY);
				densities.lockBackground(Color.white);
				densities.setFixedTopDistn(false);
				registerStatusItem("densityPerm", densities);
				
			densitiesPanel.add("Center", densities);
			
		thePanel.add("Center", densitiesPanel);
		
		return densitiesPanel;
	}
	
	
//-----------------------------------------------------------

	
	protected void setDisplayForQuestion() {
		super.setDisplayForQuestion();
		
		int shape = getShapeCode();
		densities.setYKey(kDistnKey[shape]);
		densities.repaint();
		
		sampleSizes.repaint();
		
		axisLabel.setText(isMeanNotSum() ? "Distn of mean" : "Distn of sum");
	}
	
	protected void setDataForQuestion() {
		int shape = getShapeCode();
		switch (shape) {
			case 0:		//	rectangular
				break;
			case 1:		//	gamma
				{
					GammaDistnVariable gammaDistn = (GammaDistnVariable)data.getVariable(kDistnKey[shape]);
					gammaDistn.setShape(1.0 + random01.nextDouble() * 0.5);
				}
				break;
			case 2:		//	mixture of rectangulars
				RectangularDistnVariable r1Distn = (RectangularDistnVariable)data.getVariable("r1");
				RectangularDistnVariable r2Distn = (RectangularDistnVariable)data.getVariable("r2");
				MixtureDistnVariable rectMix = (MixtureDistnVariable)data.getVariable(kDistnKey[shape]);
				
				if (random01.nextDouble() < 0.5) {		//	two clusters
					double width1 = 0.1 + 0.5 * random01.nextDouble();
					double width2 = 0.7 - width1;
					r1Distn.setLimits(0.05, 0.05 + width1);
					r2Distn.setLimits(0.95 - width2, 0.95);
					
					double p1 = 0.3 + 0.5 * random01.nextDouble();
					rectMix.setPropn(p1);
				}
				else {		//	touching rects
					double width1 = 0.05 + 0.15 * random01.nextDouble();
					if (random01.nextDouble() < 0.5)
						width1 = 0.8 - width1;
					
					r1Distn.setLimits(0.1, 0.1 + width1);
					r2Distn.setLimits(0.1 + width1, 0.9);
					
					rectMix.setPropn(0.5);
				}
				break;
			case 3:		//	mixture of normals
				{
					NormalDistnVariable n1Distn = (NormalDistnVariable)data.getVariable("n1");
					double range1 = 0.3 + 0.3 * random01.nextDouble();
					double sd1 = range1 / 5;
					n1Distn.setMean(range1 / 2);
					n1Distn.setSD(sd1);
					
					NormalDistnVariable n2Distn = (NormalDistnVariable)data.getVariable("n2");
					double range2 = 0.3 + 0.3 * random01.nextDouble();
					double sd2 = range2 / 5;
					n2Distn.setMean(1 - range2 / 2);
					n2Distn.setSD(sd2);
					
					MixtureDistnVariable normalMix = (MixtureDistnVariable)data.getVariable(kDistnKey[shape]);
					normalMix.setPropn(0.2 + 0.6 * random01.nextDouble());
				}
				break;
			case 4:		//	gamma & normal mixture
				{
					GammaDistnVariable gammaDistn = (GammaDistnVariable)data.getVariable("gamma");
					gammaDistn.setShape(1.0 + random01.nextDouble() * 0.5);
					
					NormalDistnVariable n1Distn = (NormalDistnVariable)data.getVariable("n1");
					double range1 = 0.1 + 0.2 * random01.nextDouble();
					double sd1 = range1 / 5;
					n1Distn.setMean(1 - range1 / 2);
					n1Distn.setSD(sd1);
					
					MixtureDistnVariable expNormMix = (MixtureDistnVariable)data.getVariable(kDistnKey[shape]);
					expNormMix.setPropn(0.8 + 0.1 * random01.nextDouble());
				}
				break;
		}
		
		super.setDataForQuestion();
	}
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Reorder the densities by dragging so that their shapes are the distributions of sample means from the sample sizes on the left.");
				messagePanel.insertBoldRedText("\nThe bigger the sample size, the smaller the spread, but the four distributions on the right have been rescaled so you must match them by their shape, not their spreads.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("The correct matching of densities and sample sizes is shown.");
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have correctly matched up the densities with the sample sizes.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertText("The red arrows indicate errors.");
				messagePanel.insertText("\nWhen the sample size becomes bigger, the distribution of the sample mean becomes closer to a normal distribution.");
				break;
		}
	}
	
	protected int getMessageHeight() {
		return 120;
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			RectangularDistnVariable rectDistn = new RectangularDistnVariable("Rect");
			rectDistn.setLimits(0.1, 0.9);
		data.addVariable("rect", rectDistn);
		
			GammaDistnVariable gammaDistn = new GammaDistnVariable("Gamma");
			gammaDistn.setScale(0.15);
		data.addVariable("gamma", gammaDistn);
		
			RectangularDistnVariable r1Distn = new RectangularDistnVariable("Rect1");
		data.addVariable("r1", r1Distn);
		
			RectangularDistnVariable r2Distn = new RectangularDistnVariable("Rect2");
		data.addVariable("r2", r2Distn);
		
			MixtureDistnVariable rectMixDistn = new MixtureDistnVariable("RectMix", r1Distn, r2Distn);
		data.addVariable("rectMix", rectMixDistn);
		
			NormalDistnVariable n1Distn = new NormalDistnVariable("Normal1");
		data.addVariable("n1", n1Distn);
		
			NormalDistnVariable n2Distn = new NormalDistnVariable("Normal2");
		data.addVariable("n2", n2Distn);
		
			MixtureDistnVariable normalMixDistn = new MixtureDistnVariable("NormalMix", n1Distn, n2Distn);
		data.addVariable("normalMix", normalMixDistn);
		
			MixtureDistnVariable expNormMixDistn = new MixtureDistnVariable("expNormMix", gammaDistn, n1Distn);
		data.addVariable("expNormMix", expNormMixDistn);
				
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