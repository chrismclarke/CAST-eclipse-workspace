package factorial;

import java.awt.*;

import dataView.*;
import axis.*;
import coreGraphics.*;
import models.*;
import distn.*;


public class HalfNormalPlotView extends DotPlotView {
//	static public final String HALF_NORMAL_PLOT = "halfNormalPlot";
	
	static final private String kEffectKey = "effect";
	static final private String kEffectNameKey = "effectName";
	static final private String kZKey = "z";
	
	static final private Color kLineColor = new Color(0xCCCCCC);
	static final private Color k99percentBackground = new Color(0xFFFFAA);
	static final private Color k95percentBackground = new Color(0xFFFFCC);
	
	static final DataSet createEffectData(String effectVarName) {
		DataSet effectData = new DataSet();
		
			NumVariable effectVar = new NumVariable(effectVarName);
		effectData.addVariable(kEffectKey, effectVar);
		
			LabelVariable effectNameVar = new LabelVariable(effectVarName);
		effectData.addVariable(kEffectNameKey, effectNameVar);
		
			NumVariable zVar = new NumVariable("Z quantile");
		effectData.addVariable(kZKey, zVar);
		
		return effectData;
	}
	
	private DataSet mainData;
	private String fullModelKey, modelKey, yKey;
	private NumCatAxis zAxis;
	
	private boolean initialised = false;
	
	public HalfNormalPlotView(DataSet mainData, XApplet applet,
									String yKey, String fullModelKey, String modelKey, NumCatAxis effectAxis,
									NumCatAxis zAxis) {
		super(createEffectData("Effect"), applet, effectAxis, 0.0);
		setActiveNumVariable(kEffectKey);
		this.mainData = mainData;
		this.yKey = yKey;
		this.fullModelKey = fullModelKey;
		this.modelKey = modelKey;
		this.zAxis = zAxis;
	}
	
	protected void doInitialisation(Graphics g) {
		MultiFactorModel model = (MultiFactorModel)mainData.getVariable(fullModelKey);
		
		String xKeys[] = model.getXKeys();
		SSComponent components[] = model.getBestSsqComponents(yKey);
		int nComp = components.length - 2;
		
		if (model instanceof CentrePointFactorialModel)
			nComp --;			//	don't include nonlinearity component
		
		double effect[] = new double[nComp];
		String effectName[] = new String[nComp];
		for (int i=0 ; i<nComp ; i++) {
			effect[i] = Math.sqrt(components[i + 2].ssq);
			effectName[i] = mainData.getVariable(xKeys[i]).name;
		}
		
		NumVariable effectVar = (NumVariable)getVariable(kEffectKey);
		effectVar.setValues(effect);
		
		LabelVariable effectNameVar = (LabelVariable)getVariable(kEffectNameKey);
		effectNameVar.setValues(effectName);
		
		double zQuantile[] = new double[nComp];
		NumVariable zQuantileVar = (NumVariable)getVariable(kZKey);
			for (int rank=0 ; rank<nComp ; rank++) {
				int valIndex = effectVar.rankToIndex(rank);
				zQuantile[valIndex] = NormalTable.quantile(0.5 + (rank + 0.5) / (2 * nComp));
			}
		zQuantileVar.setValues(zQuantile);
	}
	
	final protected boolean initialise(Graphics g) {
		if (initialised)
			return false;
		else {
			doInitialisation(g);
			initialised = true;
			return true;
		}
	}
	
	public void reset() {
		initialised = false;
		repaint();
	}

	
	protected Point getScreenPoint(int index, NumValue theVal, Point thePoint) {
		int effectPos = axis.numValToRawPosition(theVal.toDouble());
			NumVariable zQuantileVar = (NumVariable)getVariable(kZKey);
		int zPos = zAxis.numValToRawPosition(zQuantileVar.doubleValueAt(index));
		return translateToScreen(effectPos, zPos, thePoint);
	}

	
	protected void fiddleColor(Graphics g, int index) {
		if (isActive(index))
			g.setColor(getForeground());
		else
			g.setColor(Color.red);
	}
	
	private boolean isActive(int index) {
		MultiFactorModel model = (MultiFactorModel)mainData.getVariable(modelKey);
		int[][] activeKeys = model.getActiveKeys();
		for (int i=0 ; i<activeKeys.length ; i++)
			if (index < activeKeys[i].length)
				return activeKeys[i][index] >= FactorialTerms.ON_DISABLED;
			else
				index -= activeKeys[i].length;
		return false;
	}

	public void paintView(Graphics g) {
		initialise(g);
		
		drawBackground(g);
		super.paintView(g);
	}
	
	private void drawSigBand(Graphics g, double residSd, int df, double cumProb, Color c) {
		double sigLimit = residSd * TTable.quantile(cumProb, df);
		g.setColor(c);
		int sigPos = axis.numValToRawPosition(sigLimit);
		Point p = translateToScreen(sigPos, 0, null);
		g.fillRect(p.x, 0, getSize().width - p.x, getSize().height);
	}
	
	private void drawBackground(Graphics g) {
		MultiFactorModel model = (MultiFactorModel)mainData.getVariable(modelKey);
		SSComponent rssComp = model.getResidSsqComponent(yKey,  null);
		Point p = null;
		
		if (rssComp.df > 0) {
			double residSd = Math.sqrt(rssComp.ssq / rssComp.df);
			
			drawSigBand(g, residSd, rssComp.df, 0.975, k95percentBackground);
			drawSigBand(g, residSd, rssComp.df, 0.995, k99percentBackground);
			
			double zLow = - zAxis.maxOnAxis * 0.01;		//	min is zero
			double zHigh = zAxis.maxOnAxis - zLow;
			double effectLineLow = residSd * zLow;
			double effectLineHigh = residSd * zHigh;
			int zLowPos = zAxis.numValToRawPosition(zLow);
			int zHighPos = zAxis.numValToRawPosition(zHigh);
			int effectLowPos = axis.numValToRawPosition(effectLineLow);
			int effectHighPos = axis.numValToRawPosition(effectLineHigh);
			p = translateToScreen(effectLowPos, zLowPos, p);
			Point p2 = translateToScreen(effectHighPos, zHighPos, null);
			g.setColor(kLineColor);
			g.drawLine(p.x, p.y, p2.x, p2.y);
		}
		
		NumVariable effectVar = getNumVariable();
		boolean isActive[] = new boolean[effectVar.noOfValues()];
		int[][] activeKeys = model.getActiveKeys();
		int index = 0;
		for (int i=0 ; i<activeKeys.length ; i++)
			for (int j=0 ; j<activeKeys[i].length ; j++)
				isActive[index ++] = activeKeys[i][j] >= FactorialTerms.ON_DISABLED;
		
		g.setColor(Color.blue);
		int ascent = g.getFontMetrics().getAscent();
		ValueEnumeration e = effectVar.values();
		LabelVariable effectNameVar = (LabelVariable)getVariable(kEffectNameKey);
		ValueEnumeration le = effectNameVar.values();
		index = 0;
		while (e.hasMoreValues()) {
			NumValue nextVal = (NumValue)e.nextValue();
			LabelValue nextLabel = (LabelValue)le.nextValue();
			if (isActive[index]) {
				p = getScreenPoint(index, nextVal, p);
				nextLabel.drawRight(g, p.x + 6, p.y + ascent / 2);
			}
			index++;
		}
		g.setColor(getForeground());
	}
}