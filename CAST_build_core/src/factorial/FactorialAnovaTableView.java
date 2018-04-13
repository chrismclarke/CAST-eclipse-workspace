package factorial;

import java.awt.*;

import dataView.*;
import models.*;

import ssq.*;
import factorialProg.*;


public class FactorialAnovaTableView extends CoreAnovaTableView {
//	static final public String FACTORIAL_ANOVA_TABLE = "factorialAnovaTable";
	
	static final private NumValue kNoDf = new NumValue(0, 0);
	
	static final private Color kLightGray = new Color(0xDDDDDD);
	static final private Color kDimCheckColor = new Color(0x666666);
	static final private Color kDarkBlue = new Color(0x000099);
	
	static final private int kCheckWidth = 12;
	
	private String yKey, modelKey;
	private int ssqDecimals;
	
	private int dragIndex = -1;
	
	public FactorialAnovaTableView(DataSet data, XApplet applet,
											NumValue maxSsq, NumValue maxMsq, NumValue maxF, int tableDisplayType,
											String modelKey, String yKey) {
		super(data, applet, maxSsq, maxMsq, maxF, tableDisplayType);
		this.yKey = yKey;
		this.modelKey = modelKey;
		ssqDecimals = maxSsq.decimals;
		setComponentNames(findFactorialNames(applet));
		setComponentColors(findFactorialColors());
	}
	
	private String[] findFactorialNames(XApplet applet) {
		MultiFactorModel model = (MultiFactorModel)getVariable(modelKey);
		String[][] allKeys = model.getTermKeys();
		int nTerms = countAnovaComponents();
		
		String[] componentNames = new String[nTerms];
		componentNames[0] = applet.translate("Total");
		componentNames[nTerms - 1] = applet.translate("Residual");
		
		int componentIndex = 1;
		for (int i=0 ; i<allKeys.length ; i++)
			for (int j=0 ; j<allKeys[i].length ; j++)
				componentNames[componentIndex ++] = getVariable(allKeys[i][j]).name;
		
		if (hasCentrePoints(model))
			componentNames[componentIndex ++] = applet.translate("Nonlinear");
		
		return componentNames;
	}
	
	private Color[] findFactorialColors() {
		Color[] componentColors = new Color[3];
		componentColors[0] = kDarkBlue;		//	Total
		componentColors[1] = Color.black;		//	Explained
		componentColors[2] = Color.red;		//	Residual
		
		return componentColors;
	}
	
	private boolean hasCentrePoints(MultiFactorModel model) {
		return model instanceof CentrePointFactorialModel;
	}

//-----------------------------------------------------------------------------------
	
	protected int leftMargin() {
		return kCheckWidth + 4;
	}
	
	protected void drawComponentBackground(int explainedIndex, int tableLeft, int tableWidth,
																			int componentTop, int componentHeight, Graphics g) {
		int componentState = findComponentState(explainedIndex);
		if (componentState < 0)
			return;
		
		boolean enabledCheck = (componentState == FactorialTerms.ON_ENABLED) || (componentState == FactorialTerms.OFF_ENABLED);
		boolean onCheck = (componentState == FactorialTerms.ON_ENABLED) || (componentState == FactorialTerms.ON_DISABLED);
		drawCheck(g, tableLeft - kCheckWidth - 3, componentTop + (componentHeight - kCheckWidth) / 2,
														enabledCheck, onCheck, explainedIndex == dragIndex);
		
		if (componentState != FactorialTerms.ON_ENABLED) {
			g.setColor(kLightGray);
			g.fillRect(tableLeft, componentTop, tableWidth, componentHeight);
		}
	}
	
	private int findComponentState(int componentIndex) {
		MultiFactorModel model = (MultiFactorModel)getVariable(modelKey);
		int[][] activeKeys = model.getActiveKeys();
		for (int i=0 ; i<activeKeys.length ; i++)
			if (componentIndex < activeKeys[i].length)
				return activeKeys[i][componentIndex];
			else
				componentIndex -= activeKeys[i].length;
		
		return -1;		//	nonlinear component for centre points
	}
	
	private void drawCheck(Graphics g, int left, int top, boolean enabled, boolean onNotOff,
																																				boolean highlight) {
		if (enabled) {
			g.setColor(Color.white);
			g.fillRect(left + 1, top + 1, kCheckWidth - 1, kCheckWidth - 1);
		}
		g.setColor(enabled ? Color.black : kDimCheckColor);
		g.drawRect(left, top, kCheckWidth, kCheckWidth);
		if (highlight)
			g.drawRect(left + 1, top + 1, kCheckWidth - 2, kCheckWidth - 2);
		
		if (onNotOff) {
			g.drawLine(left, top, left + kCheckWidth, top + kCheckWidth);
			g.drawLine(left, top + kCheckWidth, left + kCheckWidth, top);
		}
	}

//-----------------------------------------------------------------------------------
	
	protected int countAnovaComponents() {
		MultiFactorModel model = (MultiFactorModel)getVariable(modelKey);
		int[][] activeKeys = model.getActiveKeys();
		int nKeys = 0;
		for (int i=0 ; i<activeKeys.length ; i++)
			nKeys += activeKeys[i].length;
		
		if (hasCentrePoints(model))
			nKeys ++;
		return nKeys + 2;
	}
	
	protected boolean canDrawTable() {
		return true;
	}
	
	protected void findComponentSsqs(NumValue[] ssq, NumValue[] df) {
																								//	0th is total, last is residual
		MultiFactorModel model = (MultiFactorModel)getVariable(modelKey);
		SSComponent[] ssComp = model.getBestSsqComponents(yKey);
																								//	0th is resid, 1st is for mean
		ssq[ssq.length - 1] = new NumValue(ssComp[0].ssq, ssqDecimals);
		df[ssq.length - 1] = new NumValue(ssComp[0].df, 0);
		
		double totalSsq = ssComp[0].ssq;
		int totalDf = ssComp[0].df;
		for (int i=2 ; i<ssComp.length ; i++) {
			totalSsq += ssComp[i].ssq;
			totalDf += ssComp[i].df;
		}
		ssq[0] = new NumValue(totalSsq, ssqDecimals);
		df[0] = new NumValue(totalDf, 0);
		
		int[][] activeKeys = model.getActiveKeys();
		
		int activeSsqIndex = 2;
		int overallSsqIndex = 1;
		for (int i=0 ; i<activeKeys.length ; i++)
			for (int j=0 ; j<activeKeys[i].length ; j++) {
				if (activeKeys[i][j] >= FactorialTerms.ON_DISABLED) {
					ssq[overallSsqIndex] = new NumValue(ssComp[activeSsqIndex].ssq, ssqDecimals);
					df[overallSsqIndex] = new NumValue(ssComp[activeSsqIndex].df, 0);
					activeSsqIndex ++;
				}
				else {
					ssq[overallSsqIndex] = null;
					df[overallSsqIndex] = kNoDf;
				}
				overallSsqIndex ++;
			}
		
		if (hasCentrePoints(model)) {
			ssq[overallSsqIndex] = new NumValue(ssComp[activeSsqIndex].ssq, ssqDecimals);
			df[overallSsqIndex] = new NumValue(ssComp[activeSsqIndex].df, 0);
		}
	}

//-----------------------------------------------------------------------------------
	
	protected boolean needsHitToDrag() {
		return false;
	}
	
	protected boolean canDrag() {
		return true;
	}
	
	protected PositionInfo getPosition(int x, int y) {
		AnovaLayoutInfo anovaLayout = getAnovaLayoutInfo();
		int componentIndex = anovaLayout.hitComponentInMargin(x, y);
		if (componentIndex < 0)
			return null;
		
		int componentState = findComponentState(componentIndex);
		if (componentState == FactorialTerms.ON_ENABLED
																		|| componentState == FactorialTerms.OFF_ENABLED)
			return new IndexPosInfo(componentIndex);
		else
			return null;
	}
	
	protected boolean startDrag(PositionInfo startInfo) {
		if (startInfo instanceof IndexPosInfo) {
			IndexPosInfo posInfo = (IndexPosInfo)startInfo;
			
			dragIndex = posInfo.itemIndex;
			
			repaint();
			return true;
		}
		else
			return false;
	}
	
	protected void doDrag(PositionInfo fromPos, PositionInfo toPos) {
		if (toPos == null) {
			dragIndex = -1;
			repaint();
		}
		else
			startDrag(toPos);
	}
	
	protected void endDrag(PositionInfo startPos, PositionInfo endPos) {
		if (dragIndex >= 0) {
			MultiFactorModel model = (MultiFactorModel)getVariable(modelKey);
			int[][] activeKeys = model.getActiveKeys();
			int termIndex = dragIndex;
			for (int i=0 ; i<activeKeys.length ; i++)
				if (termIndex < activeKeys[i].length) {
					boolean wasActive = activeKeys[i][termIndex] >= FactorialTerms.ON_DISABLED;
					model.activateTerm(i, termIndex, !wasActive);
					model.updateLSParams(yKey);
					getData().variableChanged(modelKey);
					if (getApplet() instanceof HalfNormalPlotApplet)
						((HalfNormalPlotApplet)getApplet()).modelChanged();
					break;
				}
				else
					termIndex -= activeKeys[i].length;
			
			dragIndex = -1;
			repaint();
		}
	}

}