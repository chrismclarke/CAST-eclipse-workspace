package exerciseBivarProg;

import dataView.*;

import coreVariables.*;
import random.*;
import exercise2.*;

import exerciseBivar.*;



public class MatchOddCorrApplet extends InterpretScatterApplet {
	static final private StringArray kZeroOneAxis = new StringArray("0_1_0_0.2");
	
	static final private int CORR_00 = 0;
	static final private int CORR_60 = 1;
	static final private int CORR_90 = 2;
	static final private int CORR_98 = 3;
	static final private double kMinCorr[] = {-.1, 0.57, 0.89, 0.975};
	static final private double kMaxCorr[] = {0.1, 0.63, 0.91, 0.985};
	
	static final private double kXMean_Cluster[][][] = {{{0.3, 0.7}, {0.15, 0.85}},
																										{{0.25, 0.74}, {0.2, 0.8}},
																										{{0.2, 0.8}, {0.25, 0.75}},
																										{{0.2, 0.8}, {0.15, 0.85}}};
	static final private double kXSd_Cluster[][][] = {{{0.15, 0.15}, {0.07, 0.07}},
																									{{0.125, 0.13}, {0.1, 0.1}},
																									{{0.1, 0.1}, {0.1, 0.1}},
																									{{0.1, 0.1}, {0.05, 0.05}}};
	static final private double kYMean_Cluster[][][] = {{{0.39, 0.61}, {0.5, 0.5}},
																										{{0.3, 0.7}, {0.36, 0.64}},
																										{{0.2, 0.8}, {0.2, 0.8}},
																										{{0.25, 0.8}, {0.15, 0.85}}};
	static final private double kYSd_Cluster[][][] = {{{0.18, 0.18}, {0.15, 0.15}},
																									{{0.125, 0.13}, {0.17, 0.17}},
																									{{0.08, 0.08}, {0.1, 0.1}},
																									{{0.1, 0.1}, {0.05, 0.05}}};
	static final private double kR_Cluster[][][] = {{{-0.94, -0.94}, {0.0, 0.0}},
																									{{-0.9, -0.9}, {0.0, 0.0}},
																									{{-0.5, -0.5}, {0.0, 0.0}},
																									{{0.8, 0.8}, {0.0, 0.0}}};
	static final private int kN_Cluster[] = {25, 25};
	
	static final private double kXMean_Outlier[][][] = {{{0.24, 0.95}, {0.2, 0.95}},
																											{{0.15, 0.95}, {0.18, 0.95}},
																											{{0.2, 0.95}, {0.1, 0.95}},
																											{{0.08, 0.9}, {0.04, 0.95}}};
	static final private double kXSd_Outlier[][][] = {{{0.1, 0}, {0.1, 0}},
																										{{0.07, 0.02}, {0.09, 0}},
																										{{0.1, 0.02}, {0.05, 0.02}},
																										{{0.04, 0.05}, {0.02, 0.02}}};
	static final private double kYMean_Outlier[][][] = {{{0.6, 0.05}, {0.5, 0.5}},
																											{{0.13, 0.95}, {0.2, 0.95}},
																											{{0.2, 0.9}, {0.1, 0.95}},
																											{{0.08, 0.84}, {0.04, 0.95}}};
	static final private double kYSd_Outlier[][][] = {{{0.15, 0.02}, {0.15, 0.15}},
																										{{0.06, 0.02}, {0.09, 0.02}},
																										{{0.1, 0.05}, {0.035, 0.02}},
																										{{0.04, 0.07}, {0.02, 0.02}}};
	static final private double kR_Outlier[][][] = {{{0.6, 0.0}, {0.0, 0.0}},
																									{{-0.9, 0.0}, {0.0, 0.0}},
																									{{0.78, 0.0}, {0.0, 0.0}},
																									{{0.8, 0.0}, {0.0, 0.0}}};
	static final private int kN_Outlier[] = {49, 1};
	
	static final private double kBeta0[][] = {{0.8, 0.2}, {0.46, 0.2}, {0.15, 0.2}, {0.1, 0.1}};
	static final private double kBeta1[][] = {{-2.6, 2.6}, {-1.43, 1.62}, {1.93, -0.17}, {0.2, 1.4}};
	static final private double kBeta2[][] = {{2.6, -2.6}, {1.82, -1.24}, {-1.26, 0.82}, {0.6, -0.6}};
	static final private double kErrorSd[][] = {{0.07, 0.015}, {0.05, 0.1}, {0.015, 0.07}, {0.01, 0.02}};

	static final private int kNValues = 50;
	
	private RandomRectangular xGenerator;
	private RandomNormal errorGenerator;
	
	private int rType[] = {CORR_00, CORR_60, CORR_90, CORR_98};
	
	protected StringArray getXAxes() {
		return kZeroOneAxis;
	}
	
	protected StringArray getYAxes() {
		return kZeroOneAxis;
	}
	
	protected XPanel getWorkingPanels(DataSet data) {
		dragPanel = new Drag4LabelPanel(10, 4, 0, this);
		registerStatusItem("corrPerm", dragPanel);
		
		type = new int[4];
		for (int i=0 ; i<4 ; i++)
			type[i] = i;
		typePermGenerator = new RandomInteger(0, 3, 4);
		typePermGenerator.setSeed(nextSeed());
		
		messagePermutation = new int[4];
		for (int i=0 ; i<4 ; i++)
			messagePermutation[i] = i;
		messagePermGenerator = new RandomInteger(0, 3, 4);
		messagePermGenerator.setSeed(nextSeed());
		
		for (int i=0 ; i<4 ; i++) {
			CorrelViewPanel rPanelView = new CorrelViewPanel(data, kXKeys[0], kYKeys[0], this);
			dragPanel.add(rPanelView, Drag4LabelPanel.LABEL_COMPONENT, i);	//	z-order front
		}
		
		for (int i=0 ; i<4 ; i++)
			dragPanel.add(Drag4LabelPanel.ITEM_COMPONENT, scatterPanel(data, type[i], i));
		
		return dragPanel;
	}
	
	protected void setupLabels() {
		String xKey[] = new String[4];
		String yKey[] = new String[4];
		for (int i=0 ; i<4 ; i++) {
			int displayIndex = type[i];
			xKey[i] = kXKeys[displayIndex];
			yKey[i] = kYKeys[displayIndex];
		}
		dragPanel.setCorrelKeys(xKey, yKey, messagePermutation);
	}
	
	private void resampleBivarNormal(int n, double rMin, double rMax) {
		String xKey = kXKeys[0];
		String yKey = kYKeys[0];
		NumSampleVariable xCoreVar = (NumSampleVariable)data.getVariable(xKey + "_Base");
		NumSampleVariable yCoreVar = (NumSampleVariable)data.getVariable(yKey + "_Base");
		
		xCoreVar.setSampleSize(n);
		xCoreVar.generateNextSample();
		
		yCoreVar.setSampleSize(n);
		yCoreVar.generateNextSample();
		
		ScaledVariable xVar = (ScaledVariable)data.getVariable(xKey);
		xVar.setScale(0.5, 0.22, 9);
		
		CorrelatedVariable yVar = (CorrelatedVariable)data.getVariable(yKey);
		RandomRectangular rGenerator = new RandomRectangular(1, rMin, rMax);
		rGenerator.setSeed(nextSeed());
		double r = rGenerator.generateOne();
		yVar.setMeanSdCorr(0.5, 0.18, r, 9);
		
		data.variableChanged(xKey);
		data.variableChanged(yKey);
	}
	
	private void resampleQuadratic(String xKey, String yKey, int n, double beta0,
																											double beta1, double beta2, double errorSd) {
		NumSampleVariable xCoreVar = (NumSampleVariable)data.getVariable(xKey + "_Base");
		NumSampleVariable errorVar = (NumSampleVariable)data.getVariable(yKey + "_Error");
		
		xCoreVar.setSampleSize(n);
		xCoreVar.generateNextSample();
		
		errorVar.setSampleSize(n);
		RandomNormal errorGenerator = (RandomNormal)errorVar.getGenerator();
		errorGenerator.setSD(errorSd);
		errorVar.generateNextSample();
		
		QuadraticVariable yMeanVar = (QuadraticVariable)data.getVariable(yKey + "_Mean");
		yMeanVar.changeParameters(beta0, beta1, beta2);
		
		data.variableChanged(xKey);
		data.variableChanged(yKey);
	}
	
	private AlmostRandomInteger clusterVersionGenerator, outlierVersionGenerator, quadraticVersionGenerator;
	
	protected void setDataForQuestion() {
		xAxisInfo = setupAxis(getXAxes());
		yAxisInfo = setupAxis(getYAxes());
		
//		int rType[] = {CORR_90, CORR_90, CORR_90, CORR_90};			//#####################
		
		RandomInteger rPermGenerator = new RandomInteger(0, 3, 4);
		rPermGenerator.setSeed(nextSeed());
		permute(rType, rPermGenerator);
		
		resampleBivarNormal(50, kMinCorr[rType[0]], kMaxCorr[rType[0]]);
		
			if (clusterVersionGenerator ==  null)
				clusterVersionGenerator = new AlmostRandomInteger(0, 1, nextSeed());
			int vers = clusterVersionGenerator.generateOne();
			double xMean[] = kXMean_Cluster[rType[1]][vers];
			double xSd[] = kXSd_Cluster[rType[1]][vers];
			double yMean[] = kYMean_Cluster[rType[1]][vers];
			double ySd[] = kYSd_Cluster[rType[1]][vers];
			double r[] = kR_Cluster[rType[1]][vers];
		resampleTwoClusters(kXKeys[1], kYKeys[1], kN_Cluster, xMean, xSd, yMean, ySd, r);
		
			if (outlierVersionGenerator ==  null)
				outlierVersionGenerator = new AlmostRandomInteger(0, 1, nextSeed());
			vers = outlierVersionGenerator.generateOne();
			xMean = kXMean_Outlier[rType[2]][vers];
			xSd = kXSd_Outlier[rType[2]][vers];
			yMean = kYMean_Outlier[rType[2]][vers];
			ySd = kYSd_Outlier[rType[2]][vers];
			r = kR_Outlier[rType[2]][vers];
		resampleTwoClusters(kXKeys[2], kYKeys[2], kN_Outlier, xMean, xSd, yMean, ySd, r);
		
			if (quadraticVersionGenerator ==  null)
				quadraticVersionGenerator = new AlmostRandomInteger(0, 1, nextSeed());
			vers = quadraticVersionGenerator.generateOne();
			double beta0 = kBeta0[rType[3]][vers];
			double beta1 = kBeta1[rType[3]][vers];
			double beta2 = kBeta2[rType[3]][vers];
			double errorSd = kErrorSd[rType[3]][vers];
		resampleQuadratic(kXKeys[3], kYKeys[3], kNValues, beta0, beta1, beta2, errorSd);
	}
	
//-----------------------------------------------------------
	
	private void addQuadratic(String xKey, String yKey, DataSet data) {
			xGenerator = new RandomRectangular(10, 0.0, 1.0);
			xGenerator.setNeatening(0.5);
			xGenerator.setSeed(nextSeed());
			NumSampleVariable xBaseVar = new NumSampleVariable("Z_" + xKey, xGenerator, 9);
			xBaseVar.generateNextSample();
			String xBaseKey = xKey + "_Base";
		data.addVariable(xBaseKey, xBaseVar);
		
			ScaledVariable xVar = new ScaledVariable(xKey, xBaseVar, xBaseKey, 0.0, 1.0, 9);
		data.addVariable(xKey, xVar);
		
			errorGenerator = new RandomNormal(10, 0.0, 1.0, 2.0);		//	+/- 2 SD
			errorGenerator.setSeed(nextSeed());
			NumSampleVariable errorVar = new NumSampleVariable("ERROR_" + yKey, errorGenerator, 9);
			errorVar.generateNextSample();
			String errorKey = yKey + "_Error";
		data.addVariable(errorKey, errorVar);
		
			QuadraticVariable yMeanVar = new QuadraticVariable("Mean_" + yKey, xVar, 0, 0, 0, 9);
			String yMeanKey = yKey + "_Mean";
		data.addVariable(yMeanKey, yMeanVar);
			
			SumDiffVariable yVar = new SumDiffVariable(yKey, data, errorKey, yMeanKey,
																																		SumDiffVariable.SUM);
		data.addVariable(yKey, yVar);
	}
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
		addOneCluster(kXKeys[0], kYKeys[0], data);
		
		for (int i=1 ; i<3 ; i++)
			addTwoClusters(kXKeys[i], kYKeys[i], data);
		
		addQuadratic(kXKeys[3], kYKeys[3], data);
		
		return data;
	}
	
	public void setFixedQuestionSeed(long seed) {
		super.setFixedQuestionSeed(seed);
		typePermGenerator.setSeed(nextSeed());
		messagePermGenerator.setSeed(nextSeed());
		if (clusterVersionGenerator !=  null)
			clusterVersionGenerator.setSeed(nextSeed());
		if (outlierVersionGenerator !=  null)
			outlierVersionGenerator.setSeed(nextSeed());
		if (quadraticVersionGenerator !=  null)
			quadraticVersionGenerator.setSeed(nextSeed());
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Drag the four correlation coefficients onto the scatterplots that they describe.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer");
				boolean allWrong[] = {false, false, false, false};		//	to show all messages
				addCorrDescriptions(allWrong, messagePanel);
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("You have correctly matched the correlations to the scatterplots.");
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertRedText("The correlation coefficients outlined in red do not match the scatterplots above them.");
				boolean correct[] = dragPanel.checkCorrectMessages();
				addCorrDescriptions(correct, messagePanel);
				
				break;
		}
	}
	
	private void addCorrDescriptions(boolean[] correct, MessagePanel messagePanel) {
		for (int i=0 ; i<4 ; i++)
			if (!correct[i]) {
				switch (type[i]) {
					case 0:
						messagePanel.insertBoldText("\nElliptical scatter: ");
						break;
					case 1:
						messagePanel.insertBoldText("\nTwo clusters: ");
						messagePanel.insertText("The overall r can be different from r within the two clusters, and ");
						break;
					case 2:
						messagePanel.insertBoldText("\nOutlier: ");
						messagePanel.insertText("One point with an extreme x-values strongly influences r, and ");
						break;
					case 3:
						messagePanel.insertBoldText("\nCurvature: ");
						messagePanel.insertText("r is lower than would occur with the same strength of linear relationship, so ");
						break;
				}
				switch (rType[type[i]]) {
					case CORR_00:
						messagePanel.insertText("there is little overall linear relationship.");
						break;
					case CORR_60:
						messagePanel.insertText("the overall linear relationship is weak.");
						break;
					case CORR_90:
						messagePanel.insertText("there is a fairly strong overall linear relationship.");
						break;
					case CORR_98:
						messagePanel.insertText("there is a very strong overall linear relationship.");
						break;
				}
			}
	}
}