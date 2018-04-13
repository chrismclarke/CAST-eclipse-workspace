package factorial;

import dataView.*;
import models.*;


public class MultiFactorModel extends MultipleRegnModel {
	static final private String[] kEmptyKeys = new String[0];
	
	private FactorialTerms terms;
	
	private String[] mainEffectKeys;
	
	private Object means;			// double[][]...
	private Object canDrag;		// boolean[][]...
	
	public MultiFactorModel(String name, DataSet data, String[][] termKey,
																														int[][] startModel) {
		super(name, data, kEmptyKeys);
		
		terms = new FactorialTerms(termKey);
		mainEffectKeys = termKey[0];
		
		means = createArray(data, termKey[0], 0, true);
		canDrag = createArray(data, termKey[0], 0, false);
		
		changeModel(startModel);
	}
	
	public void changeModel(int[][] newModel) {
		terms.changeModel(newModel);
		String[] linKeys = terms.lineariseKeys();
//		printStringArray("List of keys in model: ", linKeys);
		setXKey(linKeys);
		setAllDragFlags();
//		printDragFlags();
		setNonDragMeans();
	}
	
	public void setConstantMeans(double newMean) {
		setParameter(0, newMean);
		for (int i=1 ; i<noOfParameters() ; i++)
			setParameter(i, 0.0);
		calculateAllMeans(null);
	}
	
	public void activateTerm(int level, int termIndex, boolean onNotOff) {
		int[][] model = terms.activateTerm(level, termIndex, onNotOff);
		
		changeModel(model);
	}
	
	public boolean canDragMean(int[] xCat) {
		return canDragMean(xCat, canDrag, 0);
	}
	
	public String[][] getTermKeys() {
		return terms.getTermKeys();
	}
	
	public int[][] getActiveKeys() {
		return terms.getActiveKeys();
	}
	
	public int[][] getCurrentModel() {
		return terms.getCurrentModel();
	}
	
	public String[] getXKeys() {
		return xKey;
	}
	
	private boolean canDragMean(int[] xCat, Object dragFlags, int fromIndex) {
		if (fromIndex == xCat.length - 1) {
			boolean[] df = (boolean[])dragFlags;
			return df[xCat[fromIndex]];
		}
		else {
			Object[] df = (Object[])dragFlags;
			return canDragMean(xCat, df[xCat[fromIndex]], fromIndex + 1);
		}
	}
	
	private void clearDragFlags(Object flags) {
		if (flags instanceof boolean[]) {
			boolean[] f = (boolean[])flags;
			for (int i=0 ; i<f.length ; i++)
				f[i] = false;
		}
		else {
			Object[] f = (Object[])flags;
			for (int i=0 ; i<f.length ; i++)
				clearDragFlags(f[i]);
		}
	}
	
	private void setDragFlag(int[] index) {
		setDragFlag(canDrag, index, 0);
	}
	
	private void setDragFlag(Object flags, int[] index, int startIndex) {
		if (startIndex == index.length - 1) {
			boolean[] f = (boolean[])flags;
			f[index[startIndex]] = true;
		}
		else {
			Object[] f = (Object[])flags;
			setDragFlag(f[index[startIndex]], index, startIndex + 1);
		}
	}
	
	private void setAllDragFlags() {
		clearDragFlags(canDrag);
		int[] zeroIndex = new int[mainEffectKeys.length];
		setDragFlag(zeroIndex);			//	always allow one param to be dragged for mean
		
		int[][] currentModel = terms.getCurrentModel();
		for (int i=0 ; i<currentModel.length ; i++)
			setAllDragFlags(currentModel[i]);
	}
	
/*
	private void printDragFlags() {
		System.out.println("Drag flags");
		Object[] flags = (Object[]) canDrag;
		for (int i=0 ; i<flags.length ; i++) {
			Object[] flagsi = (Object[])flags[i];
			for (int j=0 ; j<flagsi.length ; j++) {
				boolean[] flagsij = (boolean[])flagsi[j];
				for (int k=0 ; k<flagsij.length ; k++)
					System.out.print(flagsij[k] ? "t " : "f ");
			}
		}
		System.out.println("\n");
	}
*/
	
	private void setAllDragFlags(int[] modelTerm) {
		int[] index = new int[mainEffectKeys.length];
		setDragFlagsFrom(modelTerm, index, 0);
	}
	
	private void setDragFlagsFrom(int[] modelTerm, int[] index, int startIndex) {
		CatVariable xVar = (CatVariable)data.getVariable(terms.getMainEffectKeys()[modelTerm[startIndex]]);
		int nLevels = xVar.noOfCategories();
		for (int i=0 ; i<nLevels ; i++) {
			index[modelTerm[startIndex]] = i;
			if (startIndex == modelTerm.length - 1)
				setDragFlag(index);
			else
				setDragFlagsFrom(modelTerm, index, startIndex + 1);
		}
	}
	
	public void setDragMean(int[] cat, double newMean) {
		if (canDragMean(cat)) {
			recursiveSetMean(cat, 0, means, newMean);
			setNonDragMeans();
		}
	}
	
	private void recursiveSetMean(int[] cat, int fromIndex, Object subMeans, double newMean) {
		if (fromIndex == cat.length - 1) {
			double[] m = (double[])subMeans;
			m[cat[fromIndex]] = newMean;
		}
		else {
			Object[] m = (Object[])subMeans;
			recursiveSetMean(cat, fromIndex + 1, m[cat[fromIndex]], newMean);
		}
	}
	
	private Object createArray(DataSet data, String[] mainEffectKeys, int startIndex,
																														boolean doubleNotBoolean) {
		int nLevels = ((CatVariable)data.getVariable(mainEffectKeys[startIndex])).noOfCategories();
		if (startIndex == mainEffectKeys.length - 1) {
			Object result;
			if (doubleNotBoolean)
				result = new double[nLevels];
			else
				result = new boolean[nLevels];
			return result;
		}
		else {
			Object result[] = new Object[nLevels];
			for (int i=0 ; i<nLevels ; i++)
				result[i] = createArray(data, mainEffectKeys, startIndex + 1, doubleNotBoolean);
			return result;
		}
	}
	
/*
	private void printStringArray(String title, String[] keys) {
		System.out.print(title);
		for (int i=0 ; i<keys.length ; i++)
			System.out.print(" " + keys[i]);
		System.out.print("\n");
	}
	
	private void printIntArray(String title, int[] indices) {
		System.out.print(title);
		for (int i=0 ; i<indices.length ; i++)
			System.out.print(" " + indices[i]);
		System.out.print("\n");
	}
*/
	
	public double evaluateMean(Value[] x) {
		int index[] = new int[x.length];
		for (int i=0 ; i<index.length ; i++)
			index[i] = ((CatVariable)data.getVariable(mainEffectKeys[i])).labelIndex(x[i]);
		return lookupMean(index, means, 0);
	}
	
	public double evaluateMean(int[] index) {
		return lookupMean(index, means, 0);
	}
	
	private double lookupMean(int[] index, Object means, int startIndex) {
		if (startIndex == index.length - 1) {
			double singleMeans[] = (double[])means;
			if (index[startIndex] >= 0)
				return singleMeans[index[startIndex]];
			else {
				double sum = 0.0;
				for (int i=0 ; i<singleMeans.length ; i++)
					sum += singleMeans[i];
				return sum / singleMeans.length;
			}
		}
		else {
			Object subMeans[] = (Object[])means;
			if (index[startIndex] >= 0) {
				Object layerMeans = subMeans[index[startIndex]];
				return lookupMean(index, layerMeans, startIndex + 1);
			}
			else {
				double sum = 0.0;
				for (int i=0 ; i<subMeans.length ; i++)
					sum += lookupMean(index, subMeans[i], startIndex + 1);
				return sum / subMeans.length;
			}
		}
	}
	
	private void calculateAllMeans(Object dragFlags) {
		int nFactors = mainEffectKeys.length;
		int[] xIndex = new int[nFactors];
		recursiveCalcMeans(xIndex, means, dragFlags, 0);
	}
	
	private void recursiveCalcMeans(int[] xIndex, Object means, Object dragFlags, int fromIndex) {
//		CatVariable xVar = (CatVariable)data.getVariable(mainEffectKeys[fromIndex]);
		if (fromIndex == xIndex.length - 1) {
			double[] m = (double[])means;
			boolean[] df = (boolean[])dragFlags;
			for (int i=0 ; i<m.length ; i++)
				if (df == null || !df[i]) {
					xIndex[fromIndex] = i;
					Value[] x = terms.createXVector(data, xIndex);
					m[i] = super.evaluateMean(x);
					
//					for (int j=0 ; j<x.length ; j++)
//						System.out.print(x[j].toString() + ", ");
//					System.out.println(m[i]);
				}
		}
		else {
			Object[] m = (Object[])means;
			Object[] df = (Object[])dragFlags;
			for (int i=0 ; i<m.length ; i++) {
				xIndex[fromIndex] = i;
				Object dfi = (df == null) ? null : df[i];
				recursiveCalcMeans(xIndex, m[i], dfi, fromIndex + 1);
			}
		}
	}
	
	public void setLSParams(String yKey, Object constraints, int[] bDecs, int sdDecs) {
		super.setLSParams(yKey, constraints, bDecs, sdDecs);
		calculateAllMeans(null);
	}
	
	private void updateSsqForMeans(Value[] x, Object means, Object dragFlags, int fromIndex,
																												double[] xy, double[] r) throws GivensException {
		CatVariable xVar = (CatVariable)data.getVariable(mainEffectKeys[fromIndex]);
		if (fromIndex == x.length - 1) {
			double[] m = (double[])means;
			boolean[] df = (boolean[])dragFlags;
			for (int i=0 ; i<m.length ; i++)
				if (df[i]) {
					x[fromIndex] = xVar.getLabel(i);
//					System.out.print("\n");
//					for (int j=0 ; j<x.length ; j++)
//						System.out.print(x[j].toString() + ", ");
//					System.out.print("\n");
					updateSsqForOneMean(x, m[i], xy, r);
				}
		}
		else {
			Object[] m = (Object[])means;
			Object[] df = (Object[])dragFlags;
			for (int i=0 ; i<m.length ; i++) {
				x[fromIndex] = xVar.getLabel(i);
				updateSsqForMeans(x, m[i], df[i], fromIndex + 1, xy, r);
			}
		}
	}
	
	
	private void updateSsqForOneMean(Value[] x, double mean, double[] xy, double[] r)
																																						throws GivensException {
		Value[] oldX = new Value[x.length];
		for (int i=0 ; i<x.length ; i++) {		//	replace 0th values for main effect
			CatVariable xiVar = (CatVariable)data.getVariable(mainEffectKeys[i]);
			oldX[i] = xiVar.valueAt(0);
			xiVar.setValueAt(x[i], 0);
		}
		
		xy[0] = 1.0;
		
		int firstIndex = 1;
		for (int i=0 ; i<xKey.length ; i++) {		//	expand 0th row for main effects and interactions
			CatVariable xiVar = (CatVariable)data.getVariable(xKey[i]);
			Value xi = xiVar.valueAt(0);
			int nCats = xiVar.noOfCategories();
			for (int j=0 ; j<nCats-1 ; j++)
				xy[firstIndex + j] = 0.0;
			int xiCat = xiVar.labelIndex(xi);
			if (xiCat > 0)
				xy[firstIndex + xiCat - 1] = 1.0;
			firstIndex += (nCats - 1);
		}
		xy[firstIndex] = mean;
		givenC(r, xy, 1.0);
		
		for (int i=0 ; i<x.length ; i++) {		//	restore 0th values for main effect
			CatVariable xiVar = (CatVariable)data.getVariable(mainEffectKeys[i]);
			xiVar.setValueAt(oldX[i], 0);
		}
	}
	
	public void setNonDragMeans() {
		int nParam = noOfParameters();
		
		double[] xy = new double[nParam + 1];
		double[] r = initSsqMatrix(nParam + 1);
		
		Value[] xMain = new Value[mainEffectKeys.length];
		try {
			updateSsqForMeans(xMain, means, canDrag, 0, xy, r);
			
			double[] bValue = bSub(r, nParam + 1, null);
			for (int i=0 ; i<b.length ; i++)
				b[i] = new NumValue(bValue[i], 9);
			
			calculateAllMeans(canDrag);
		} catch (GivensException e) {
		}
	}
	
}