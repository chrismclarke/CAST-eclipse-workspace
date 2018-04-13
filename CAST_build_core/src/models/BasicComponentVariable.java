package models;

import java.awt.*;

import dataView.*;

public class BasicComponentVariable extends CoreComponentVariable {
	static final public int TOTAL = 0;
	static final public int EXPLAINED = 1;
	static final public int RESIDUAL = 2;
	
	static final public Color kTotalColor = new Color(0x006600);		//	dark green
	static final public Color kExplainedColor = Color.red;
	static final public Color kResidColor = Color.blue;
	
	static final public int kComponentType[] = {TOTAL, EXPLAINED, RESIDUAL};
	static final public String kComponentKey[] = {"total", "explained", "resid"};
	static final public Color kComponentColor[] = {kTotalColor, kExplainedColor, kResidColor};
	
	static final private NumValue kNanValue = new NumValue("?");
	
	private int componentType;
	private String xKey[];
	
	public BasicComponentVariable(String theName, DataSet data, String xKey, String yKey,
													String modelKey, int componentType, int decimals) {
		this(theName, data, makeArray(xKey), yKey, modelKey, componentType, decimals);
	}
	
	public BasicComponentVariable(String theName, DataSet data, String[] xKey, String yKey,
													String modelKey, int componentType, int decimals) {
		super(theName, data, yKey, modelKey, decimals);
		this.componentType = componentType;
		this.xKey = xKey;
	}

//--------------------------------------------------------
	
	public int noOfValues() {
		return Math.min(((Variable)data.getVariable(xKey[0])).noOfValues(),
									super.noOfValues());
	}
	
	public boolean noteVariableChange(String key) {
		boolean changed = super.noteVariableChange(key);
		
		for (int i=0 ; i<xKey.length ; i++)
			if (xKey[i].equals(key))
				changed = true;
		return changed;
	}
	
	public Value valueAt(int index) {
		NumVariable yVar = (NumVariable)data.getVariable(yKey);
		if (!foundYMean && componentType != RESIDUAL)
			getYMean();
		double y = yVar.doubleValueAt(index);
		if (Double.isNaN(y))
			return kNanValue;
		double component;
		if (componentType == TOTAL)
			component = y - yMean;
		else {
			Value x[] = new Value[xKey.length];
			for (int i=0 ; i<xKey.length ; i++)
				x[i] = ((Variable)data.getVariable(xKey[i])).valueAt(index);
			CoreModelVariable theModel = (CoreModelVariable)data.getVariable(modelKey);
			double fit = theModel.evaluateMean(x);
			component = (componentType == EXPLAINED) ? (fit - yMean) : (y - fit);
		}
		return new NumValue(component, decimals);
	}
	
	public int getDF() {
		NumVariable yVar = (NumVariable)data.getVariable(yKey);
//		int n = yVar.noOfValues();
		int n = 0;
		for (int i=0 ; i<yVar.noOfValues() ; i++)
			if (!Double.isNaN(yVar.doubleValueAt(i)))
				n ++;
		if (componentType == TOTAL)
			return n - 1;
		else {
			CoreModelVariable theModel = (CoreModelVariable)data.getVariable(modelKey);
			int noOfParams = theModel.noOfParameters() - theModel.noOfConstrainedParameters();
			return (componentType == EXPLAINED) ? noOfParams - 1
																					: n - noOfParams;
		}
	}
}
