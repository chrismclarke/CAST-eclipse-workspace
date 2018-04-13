package models;

import java.util.*;

import dataView.*;


public class MultipleRegnModel extends CoreModelVariable {
	static final private NumValue kZero = new NumValue(0.0, 0);
	
	protected NumValue[] b;
	
	private double[] xxInv = null;		// for evaluating leverages
	
	protected int[] nBParam;
	protected int nBTotal;
	
	private int nConstraints = 0;
	
	public MultipleRegnModel(String name, DataSet data, String[] xKey, NumValue[] b,
																																					NumValue s0) {
		super(name, data, xKey);
		countBParams(data, xKey);
		if (b.length != nBTotal)
			throw new RuntimeException("Wrong number of parameters to initialise MultipleRegnModel");
		this.b = b;
		this.s0 = s0;
	}
	
	public MultipleRegnModel(String name, DataSet data, String[] xKey, NumValue[] b) {
		this(name, data, xKey, b, kZero);
	}
	
	public MultipleRegnModel(String name, DataSet data, String[] xKey, String params) {
		super(name, data, xKey);
		countBParams(data, xKey);
		setParameters(params);
	}
	
	public MultipleRegnModel(String name, DataSet data, String[] xKey) {
																		//		only for situations where parameters
																		//		are set by LS immediately after
		super(name, data, xKey);
		countBParams(data, xKey);
		b = new NumValue[nBTotal];
	}
	
	public void setXKey(String[] xKey) {
		super.setXKey(xKey);
		int oldNBTotal = nBTotal;
		countBParams(data, xKey);
		if (oldNBTotal != nBTotal)
			b = new NumValue[nBTotal];
	}
	
	protected void countBParams(DataSet data, String[] xKey) {
		nBParam = new int[xKey.length];
		boolean hasFactors = false;
		for (int i=0 ; i<xKey.length ; i++) {
			CoreVariable xVar = data.getVariable(xKey[i]);
			if (xVar instanceof CatVariable) {
				hasFactors = true;
				nBParam[i] = ((CatVariable)xVar).noOfCategories() - 1;
			}
			else
				nBParam[i] = 1;
		}
		
		nBTotal = 1;
		for (int i=0 ; i<nBParam.length ; i++)
				nBTotal += nBParam[i];
		
		if (!hasFactors)
			nBParam = null;
	}
	
	public void setParameters(String params) {
		StringTokenizer theParams = new StringTokenizer(params);
		
		int nb = theParams.countTokens() - 1;
		if (nb != nBTotal)
			throw new RuntimeException("Wrong number of parameters to initialise MultipleRegnModel");
		b = new NumValue[nb];
		for (int i=0 ; i<nb ; i++)
			b[i] = new NumValue(theParams.nextToken());
		
		s0 = new NumValue(theParams.nextToken());
	}
	
	public void setParameters(NumValue[] b) {
		if (b.length != nBTotal)
			throw new RuntimeException("Wrong number of parameters to initialise MultipleRegnModel");
		this.b = b;
	}
	
	public void setParameterDecimals(int[] decimals) {
		if (decimals.length != nBTotal)
			throw new RuntimeException("Wrong number of parameters to initialise MultipleRegnModel");
		if (b == null || b.length != nBTotal)
			b = new NumValue[nBTotal];
			
		for (int i=0 ; i<nBTotal ; i++)
			if (b[i] == null)
				b[i] = new NumValue(0.0, decimals[i]);
			else
				b[i].decimals = decimals[i];
	}
	
	public void setParameterDecimals(int decimals) {
		if (b == null || b.length != nBTotal)
			b = new NumValue[nBTotal];
		
		for (int i=0 ; i<nBTotal ; i++)
			if (b[i] == null)
				b[i] = new NumValue(0.0, decimals);
			else
				b[i].decimals = decimals;
	}
	
	public void setParameter(int index, NumValue b) {
		this.b[index] = b;
	}
	
	public void setParameter(int index, double bValue) {
		b[index].setValue(bValue);
	}

	public int noOfConstrainedParameters() {
		return nConstraints;
	}
	
	public NumValue getParameter(int index) {
		return b[index];
	}
	
	public int noOfParameters() {
		return b.length;
	}
	
	public int[] noOfXParameters() {
		return nBParam;
	}
	
	public void setLSParams(String yKey, int[] bDecs, int sdDecs) {
		setLSParams(yKey, null, bDecs, sdDecs);
	}
	
	protected boolean fillXRow(double[] x, ValueEnumeration[] xe) {
		boolean rowOK = true;
		x[0] = 1.0;
		if (nBParam == null)
			for (int i=0 ; i<xe.length ; i++) {
				x[i+1] = xe[i].nextDouble();
				if (Double.isNaN(x[i+1]))
					rowOK = false;
			}
		else {
			int firstIndex = 1;
			for (int i=0 ; i<xe.length ; i++) {
				Value xi = xe[i].nextValue();
				if (nBParam[i] == 1 && (xi instanceof NumValue)) {
					x[firstIndex] = ((NumValue)xi).toDouble();
					if (Double.isNaN(x[firstIndex]))
						rowOK = false;
					firstIndex ++;
				}
				else {
					CatVariable xiVar = (CatVariable)data.getVariable(xKey[i]);
					int nCats = xiVar.noOfCategories();
					for (int j=0 ; j<nCats-1 ; j++)
						x[firstIndex + j] = 0.0;
					int xiCat = xiVar.labelIndex(xi);
					if (xiCat > 0)
						x[firstIndex + xiCat - 1] = 1.0;
					firstIndex += (nCats - 1);
				}
			}
		}
		return rowOK;
	}
	
	private int setupSsqMatrix(String yKey, Object constraints, double[] r, int noOfParams)
																																throws GivensException {
									//		constraints can be null, (double[]) for fixed values or (double[][]) for contrasts
		double[] xy = new double[noOfParams + 1];
		
		int nObs = 0;
		int index = 0;
		nConstraints = 0;
		
		ValueEnumeration xe[] = new ValueEnumeration[xKey.length];
		for (int i=0 ; i<xKey.length ; i++)
			xe[i] = ((Variable)data.getVariable(xKey[i])).values();
		ValueEnumeration ye = ((NumVariable)data.getVariable(yKey)).values();
		
		while (ye.hasMoreValues()) {
			boolean rowOK = fillXRow(xy, xe);
			xy[noOfParams] = ye.nextDouble();
			if (Double.isNaN(xy[noOfParams]))
				rowOK = false;
			if (rowOK && index != deletedIndex) {
				givenC(r, xy, 1.0);
				nObs ++;
			}
			index ++;
		}
	
		if (constraints != null)
			if (constraints instanceof double[]) {
				double[] fixedB = (double[])constraints;
				for (int i=0 ; i<fixedB.length ; i++)
					if (!Double.isNaN(fixedB[i])) {
						for (int j=0 ; j<xy.length ; j++)
							xy[j] = 0.0;
						xy[i] = 1.0;
						xy[xy.length - 1] = fixedB[i];
						givenC(r, xy, 0.0);				//		constrain the value of parameter
						nConstraints ++;
					}
			}
			else {
				double[][] contrasts = (double[][])constraints;
				for (int i=0 ; i<contrasts.length ; i++) {
					for (int j=0 ; j<xy.length ; j++)
						xy[j] = contrasts[i][j];
					givenC(r, xy, 0.0);				//		use contrast to apply constraint
					nConstraints ++;
				}
			}
		return nObs;
	}
	
	public void setLSParams(String yKey, Object constraints, int[] bDecs, int sdDecs) {
		int noOfParams = noOfParameters();
		double[] r = initSsqMatrix(noOfParams + 1);
		try {
			int nObs = setupSsqMatrix(yKey, constraints, r, noOfParams);
			double[] bValue = bSub(r, noOfParams + 1, null);
			for (int i=0 ; i<b.length ; i++)
				b[i] = new NumValue(bValue[i], bDecs[i]);
			
			SSComponent rss = ssComp(r, noOfParams + 1, nObs, 0);
			s0 = new NumValue(Math.sqrt(rss.ssq / rss.df), sdDecs);
		} catch (GivensException e) {
			System.err.println(e);
		}
		
		xxInv = null;
	}
	
	public SSComponent[] getBestSsqComponents(String yKey) {
		int nComponents = (nBParam == null) ? b.length + 1 : nBParam.length + 2;
		SSComponent[] ssq = new SSComponent[nComponents];
			
		int noOfParams = noOfParameters();
		double[] r = initSsqMatrix(noOfParams + 1);
		try {
			int nObs = setupSsqMatrix(yKey, null, r, noOfParams);
			
			for (int i=0 ; i<2 ; i++)				//		resid and mean
				ssq[i] = ssComp(r, noOfParams + 1, nObs, i);
			
			if (nBParam == null)
				for (int i=0 ; i<b.length + 1 ; i++)
					ssq[i] = ssComp(r, noOfParams + 1, nObs, i);
			else {
				int compIndex = 2;
				int basicCompIndex = 2;
				for (int i=0 ; i<nBParam.length ; i++) {
					ssq[compIndex] = ssComp(r, noOfParams + 1, nObs, basicCompIndex, nBParam[i]);
					basicCompIndex += nBParam[i];
					compIndex ++;
				}
			}
		} catch (GivensException e) {
			System.err.println(e);
		}
		return ssq;
	}
	
	public SSComponent getResidSsqComponent(String yKey, Object constraints) {
		int noOfParams = noOfParameters();
		double[] r = initSsqMatrix(noOfParams + 1);
		try {
			int nObs = setupSsqMatrix(yKey, constraints, r, noOfParams);
			
			return ssComp(r, noOfParams + 1, nObs, 0);
		} catch (GivensException e) {
			System.err.println(e);
		}
		return null;
	}
	
	public double[] getCoeffVariances(String yKey) {
		return getCoeffVariances(yKey, false, 0.0);
	}
	
	public double[] getCoeffVariances(String yKey, Object constraints) {
		return getCoeffVariances(yKey, constraints, false, 0.0);
	}
	
	public double[] getCoeffVariances(String yKey, boolean useKnownVariance, double knownVariance) {
		return getCoeffVariances(yKey, null, useKnownVariance, knownVariance);
	}
	
	public double[] getCoeffVariances(String yKey, Object constraints, boolean useKnownVariance, double knownVariance) {
		int noOfParams = noOfParameters();
		double[] r = initSsqMatrix(noOfParams + 1);
		try {
			int nObs = setupSsqMatrix(yKey, constraints, r, noOfParams);
		
			return variance(r, noOfParams + 1, nObs, null, useKnownVariance, knownVariance);
		} catch (GivensException e) {
			System.err.println(e);
			return null;
		}
	}
	
	public double[] getXXInv() {
		int noOfParams = noOfParameters();
		double[] x = new double[noOfParams + 1];
		double[] r = initSsqMatrix(noOfParams + 1);
		
		int nObs = 0;
		
		ValueEnumeration xe[] = new ValueEnumeration[xKey.length];
		for (int i=0 ; i<xKey.length ; i++)
			xe[i] = ((Variable)data.getVariable(xKey[i])).values();
		
		try {
			while (xe[0].hasMoreValues()) {
				boolean ok = fillXRow(x, xe);
				if (!ok)
					break;
				x[noOfParams] = 0.0;		//	Should not be needed, but variance() expects a y-value
				givenC(r, x, 1.0);
				nObs ++;
			}
		
			double[] result = variance(r, noOfParams + 1, nObs, null, true, 1.0);
			
//			for (int i=0 ; i<result.length ; i++)
//				System.out.print(result[i] + ", ");
//			System.out.println("\n");
			
			return result;
		} catch (GivensException e) {
			System.err.println(e);
			return null;
		}
	}
	
	public void updateLSParams(String yKey) {
		updateLSParams(yKey, null);
	}
	
	public void updateLSParams(String yKey, Object constraints) {
		int paramDecs[] = new int[b.length];
		for (int i=0 ; i<b.length ; i++)
			paramDecs[i] = b[i].decimals;
		int sdDecs = (s0 == null) ? 0 : s0.decimals;
		setLSParams(yKey, constraints, paramDecs, sdDecs);
	}
	
	public double evaluateMean(double[] x) {
//		double result = 0.0;
//		for (int i=0 ; i<x.length ; i++)
//			result += x[i] * b[i].toDouble();
		double result = b[0].toDouble();
		for (int i=0 ; i<x.length ; i++)
			result += x[i] * b[i + 1].toDouble();
		return result;
	}
	
	private double[] expandXValues(Value[] x) {
//		int noOfParams = noOfParameters();
//		double xVal[] = new double[noOfParams];
//		xVal[0] = 1.0;
//		int valIndex = 1;
		int noOfX = noOfParameters() - 1;
		double xVal[] = new double[noOfX];
		int valIndex = 0;
		for (int i=0 ; i<x.length ; i++)
			if (nBParam == null || x[i] instanceof NumValue)
				xVal[valIndex ++] = ((NumValue)x[i]).toDouble();
			else {
				CatVariable xiVar = (CatVariable)data.getVariable(xKey[i]);
				int nCats = xiVar.noOfCategories();
				int xCat = xiVar.labelIndex(x[i]);
				for (int j=0 ; j<nCats-1 ; j++)
					xVal[valIndex + j] = 0.0;
				if (xCat > 0)
					xVal[valIndex + xCat - 1] = 1.0;
				valIndex += (nCats - 1);
			}
			
		return xVal;
	}
	
	public double evaluateMean(Value[] x) {
		return evaluateMean(expandXValues(x));
	}
	
	public double getLeverage(Value[] x) {
		if (xxInv == null)
			xxInv = getXXInv();
		
		int noOfParams = noOfParameters();
		double xVal[] = new double[noOfParams];
		xVal[0] = 1.0;
		for (int i=0 ; i<x.length ; i++)
			xVal[i + 1] = ((NumValue)x[i]).toDouble();
		
		double result = 0.0;
		int ij = 0;
		for (int i=0 ; i<xVal.length ; i++) {
			for (int j=0 ; j<i ; j++)
				result += 2.0 * xVal[i] * xVal[j] * xxInv[ij ++];
			result += xVal[i] * xVal[i] * xxInv[ij ++];
		}
		
		return result;
	}
	
	public double getVIF(int index, double[] scaledVar) {
									//	assumes that scaledVar was found by getCoeffVariances() with sigma = 1
		double actualVar = scaledVar[(index + 1) * (index + 2) / 2 - 1];
		
		NumVariable xVar = (NumVariable)data.getVariable(xKey[index - 1]);
		ValueEnumeration xe = xVar.values();
		double sxx = 0.0;
		double sx = 0.0;
		int n = 0;
		while (xe.hasMoreValues()) {
			double x = xe.nextDouble();
			sxx += x * x;
			sx += x;
			n ++;
		}
		double ssq = sxx - sx * sx / n;
		return ssq * actualVar;
	}
	
	private double coeffCovar(double[] v, int i, int j) {
		int iLow = Math.min(i, j);
		int iHigh = Math.max(i, j);
		
		int rowStart = iHigh * (iHigh + 1) / 2;
		return v[rowStart + iLow];
	}
	
	public double getContrastVar(double[] contrast, double knownVariance) {
		double[] v = getXXInv();
		int noOfParams = noOfParameters();
		
		double contrastVar = 0.0;
		
		for (int i=0 ; i<noOfParams ; i++)
			for (int j=0 ; j<noOfParams ; j++)
				contrastVar += contrast[i] * contrast[j] * coeffCovar(v, i, j) * knownVariance;
		
		return contrastVar;
	}
	
	public double getContrastVar(String yKey, double[] contrast) {
		double[] v = getCoeffVariances(yKey);
		
		int noOfParams = noOfParameters();
		double contrastVar = 0.0;
		
		for (int i=0 ; i<noOfParams ; i++)
			for (int j=0 ; j<noOfParams ; j++)
				contrastVar += contrast[i] * contrast[j] * coeffCovar(v, i, j);
		
		return contrastVar;
	}
	
/*
	public double getContrastVar(String yKey, double[] contrast) {
				//		Old version did calculations from the F-ratio for imposing the contrast as a constraint
		int noOfParams = noOfParameters();
		double[] r = initSsqMatrix(noOfParams + 1);
		try {
			int nObs = setupSsqMatrix(yKey, null, r, noOfParams);
			
			SSComponent fullRssComp = ssComp(r, noOfParams + 1, nObs, 0);
			double meanResidSsq = fullRssComp.ssq / fullRssComp.df;
			double[] bValue = bSub(r, noOfParams + 1, null);
			double cValue = 0.0;
			for (int i=0 ; i<bValue.length ; i++)
				cValue += contrast[i] * bValue[i];
			
			double[] xy = new double[noOfParams + 1];
			for (int j=0 ; j<xy.length-1 ; j++)
				xy[j] = contrast[j];
			xy[noOfParams] = 0.0;
			givenC(r, xy, 0.0);				//		use contrast to apply constraint
			
			SSComponent reducedRssComp = ssComp(r, noOfParams + 1, nObs, 0);
			
			return meanResidSsq * cValue * cValue / (reducedRssComp.ssq - fullRssComp.ssq);
		} catch (GivensException e) {
			System.err.println(e);
			return 0.0;
		}
	}
*/
	
}