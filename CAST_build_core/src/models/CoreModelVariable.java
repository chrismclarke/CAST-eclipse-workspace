package models;

import java.awt.*;

import dataView.*;
import distn.*;
import axis.*;


abstract public class CoreModelVariable extends CoreVariable {
	static final private double givensEps = 0.000001;
	static final private double ssCompEps = 0.0;
	static final private double varianceEps = 0.0;
	
	static public void givenC (double[] r, double[] x, double v) throws GivensException {
		int irUsed = x.length * (x.length + 1) / 2;
		if (r.length < irUsed)
			throw new GivensException(GivensException.R_TOO_SMALL);
		else if (v < 0.0)
			throw new GivensException(GivensException.R_TOO_SMALL);
		else {
			double vLocal = v;
			int ii = -1;
			for (int i=0 ; i<x.length ; i++) {
				ii += (i+1);
				double xi = x[i];
				if (xi != 0.0) {
					double xi2 = xi * xi;
					double cTemp = r[ii];
					int ij = ii;
					int iPlus = i + 1;
					
					if (cTemp < 0.0) {
						r[ii] = vLocal / xi2;
						if (i == (x.length-1))
							break;
						else {
							for (int j=iPlus; j<x.length ; j++) {
								ij += j;
								r[ij] = x[j] / xi;
							}
							return;
						}
					}

					if (cTemp <= 0.0)
						for (int j=iPlus ; j<x.length ; j++) {
							ij += j;
							double xj = x[j];
							x[j] -= xi * r[ij];
							if (Math.abs(x[j]) <= givensEps * Math.abs(xj))
								x[j] = 0.0;
						}
					else {
						double vNew = vLocal + cTemp * xi2;
						double c = vLocal / vNew;
						double s = cTemp * xi / vNew;
						vLocal = vNew;
						r[ii] = cTemp * c;
						if (i == x.length - 1) {
							if (Math.abs(r[irUsed - 1]) <= 0.0)
								throw new GivensException(GivensException.NON_POS_RSS);
							else if (vLocal <= 0.0)
								throw new GivensException(GivensException.POS_WT_VAR);
						}
						else
							for (int j=iPlus ; j<x.length ; j++) {
								ij += j;
								double rTemp = c * r[ij] + s * x[j];
								x[j] -= xi * r[ij];
								r[ij] = rTemp;
						}
					}
				}
			}
		}
	}
	
	static public double[] bSub(double[] r, int iDep, double[] coeff) throws GivensException {
		if (coeff == null || coeff.length < (iDep - 1))
			coeff = new double[iDep - 1];
		int ii = iDep * (iDep + 1) / 2 - 1;
		int nxVars = iDep - 1;
		if (r.length < ii + 1)
			throw new GivensException(GivensException.R_TOO_SMALL);
		else if (nxVars >= 1) {
			int k = ii;
			int nx = iDep - 1;
			for (int i=0 ; i<nxVars ; i++) {
				ii -= (nx + 1);
				k --;
				double temp = r[k];
//				if (r[ii] < 0.0)
//					singularCount ++;
				if (i > 0) {
					int ij = ii;
					for (int j=nx ; j<nxVars ; j++) {
						ij += j;
						temp -= r[ij] * coeff[j];
					}
				}
				nx --;
				coeff[nx] = temp;
			}
		}
		return coeff;
	}
	
	static public SSComponent ssComp(double[] r, int iDep, int nObs, int iComp) throws GivensException {
		int irUsed = iDep * (iDep + 1) / 2;
		if (r.length < irUsed)
			throw new GivensException(GivensException.R_TOO_SMALL);
		if (iComp < 0 || iComp >= iDep)
			throw new GivensException(GivensException.ILLEGAL_COMPONENT);
		
		int idf;
		double ssq = 0.0;
		if (iComp == 0) {
			int nxVars = iDep - 1;
			idf = nObs - nxVars;
			int ii = -1;
			for (int i=0 ; i<nxVars ; i++) {
				ii += (i+1);
				if (r[ii]<= ssCompEps)
					idf ++;
			}
			if (r[irUsed - 1] > ssCompEps)
				ssq = 1.0 / r[irUsed - 1];
		}
		else {
			idf = 0;
			int ii = iComp * (iComp + 1) / 2 - 1;
			if (r[ii] > ssCompEps) {
				idf = 1;
				int ij = irUsed - iDep + iComp - 1;
				ssq = r[ij] * r[ij] / r[ii];
			}
		}
		return new SSComponent(ssq, idf);
	}
	
	static public SSComponent ssComp(double[] r, int iDep, int nObs, int iComp1, int nComps)
																																		throws GivensException {
		int irUsed = iDep * (iDep + 1) / 2;
		if (r.length < irUsed)
			throw new GivensException(GivensException.R_TOO_SMALL);
		if (iComp1 <= 0 || nComps <= 0 || iComp1 + nComps > iDep)
			throw new GivensException(GivensException.ILLEGAL_COMPONENT);
		
		int idf = 0;
		double ssq = 0.0;
		
		for (int i=0 ; i<nComps ; i++) {
			int iComp = iComp1 + i;
			int ii = iComp * (iComp + 1) / 2 - 1;
			if (r[ii] > ssCompEps) {
				idf += 1;
				int ij = irUsed - iDep + iComp - 1;
				ssq += r[ij] * r[ij] / r[ii];
			}
		}
		
		return new SSComponent(ssq, idf);
	}
	
	static public double[] variance(double[] r, int iDep, int nObs, double[] s) throws GivensException {
		return variance(r, iDep, nObs, s, false, 0.0);
	}
	
	static public double[] variance(double[] r, int iDep, int nObs, double[] s,
											boolean useKnownVariance, double knownVariance) throws GivensException {
																	//	knownVariance is the model variance in order to find
																	//	theoretical variance of parameter estimates
		int irUsed = iDep * (iDep + 1) / 2;
		if (r.length < irUsed)
			throw new GivensException(GivensException.R_TOO_SMALL);
		if (s == null || s.length != (irUsed - iDep))
			s = new double[irUsed - iDep];
		
		int nxVars = iDep - 1;
		int nCons = 0;
		int ij = -1;
		for (int i=0 ; i<nxVars ; i++) {
			int jj = -1;
			int j = -1;
			while (true) {
				j ++;
				ij ++;
				jj += (j + 1);
				if (j >= i) {
					if (r[ij] <= varianceEps)
						nCons ++;
						break;
				}
				double sTemp = -r[ij];
				int ik = ij;
				int kj = jj;
				int kMax = i - 1;
				int kMin = j + 1;
				for (int k=kMin ; k<=kMax ; k++) {
					ik ++;
					kj +=  k;
					sTemp -= r[ik] * s[kj];
				}
				s[ij] = sTemp;
			}
		}

		double sigma = 0.0;
		if (useKnownVariance)
			sigma = knownVariance;
		else {
			int idf = nObs - nxVars + nCons;
			if (idf <= 0)
				throw new GivensException(GivensException.TOO_FEW_ERROR_DF);
			
			if (r[irUsed - 1] > 0.0)
				sigma = 1.0 / (r[irUsed - 1] * idf);
		}
		int ii = -1;
		for (int i=0 ; i<nxVars ; i++) {
			ii += (i+1);
			s[ii] = sigma * r[ii];
			if (r[ii] < 0.0)
				s[ii] = 0.0;
		}
		
		ii = -1;
		ij = -1;
		for (int i=0 ; i<nxVars ; i++) {
			ii += (i+1);
			for (int j=0 ; j<=i ; j++) {
				int kk = ii;
				ij ++;
				int ki = ij;
				int kj = ii;
				double sTemp = s[kk];
				if (i != j)
					sTemp *= s[ij];
				
				int k = i;
				while (true) {
					k ++;
					if (k > nxVars - 1)
						break;
					kk += (k+1);
					ki += k;
					kj += k;
					sTemp += s[ki] * s[kj] * s[kk];
				}
				s[ij] = sTemp;
			}
		}
		return s;
	}
	
	static public double[] alias(double[] r, int nVars, double[] eps, double[] worksp) throws GivensException {
															//		Not tested. Requires separate eps for each variable
		int irUsed = nVars * (nVars + 1) / 2;
		if (r.length < irUsed)
			throw new GivensException(GivensException.R_TOO_SMALL);
		int nxVars = nVars - 1;
		if (worksp == null || worksp.length != nVars)
			worksp = new double[nVars];
		int ii = -1;
		for (int i=0 ; i<nxVars ; i++) {
			ii += (i+1);
			worksp[i] = 0.0;
			if (Math.abs(r[ii]) * eps[i] > 1.0) {
				double v = r[ii];
				r[ii] = -1.0;
				int ij = ii;
				int iPlus = i+1;
				for (int j=iPlus ; j<nVars ; j++) {
					ij += j;
					worksp[j] = r[ij];
					r[ij] = 0.0;
				}
				givenC(r, worksp, v);
			}
		}
		return worksp;
	}

	
	static public double[] initSsqMatrix(int nVars) {
		double[] r = new double[nVars * (nVars + 1) / 2];
		int ij = -1;
		for (int i=0 ; i<nVars ; i++) {
			for (int j=0 ; j<i ; j++) {
				ij ++;
				r[ij] = 0.0;
			}
			ij ++;
			r[ij] = -1.0;
		}
		return r;
	}
	
//----------------------------------------------------------------------
	
	static private String[] createArray(String xKey) {
		String temp[] = new String[1];
		temp[0] = xKey;
		return temp;
	}
	
//----------------------------------------------------------------------
	
	protected NumValue s0 = new NumValue(1.0, 0);
	
	private Value tempValue[] = new Value[1];
	protected DataSet data;
	protected String xKey[];
	
	protected int deletedIndex = -1;		//	for deleted fit and deleted residuals
	
	public CoreModelVariable(String theName, DataSet data, String[] xKey) {
		super(theName);
		this.data = data;
		this.xKey = xKey;
	}
	
	public CoreModelVariable(String theName, DataSet data, String xKey) {
		this(theName, data, createArray(xKey));
	}
	
//----------------------------------------------------------------------
	
	abstract public double evaluateMean(Value[] x);
	
	abstract public void setParameters(String s);
	
	abstract public void updateLSParams(String yKey);
	
	abstract public int noOfParameters();
	
//----------------------------------------------------------------------

	public int noOfConstrainedParameters() {
		return 0;
	}
	
	public void setXKey(String newXKey) {
		this.xKey = createArray(newXKey);
	}
	
	public void setXKey(String[] xKey) {
		this.xKey = xKey;
	}
	
	public String[] getXKey() {
		return xKey;
	}
	
	public boolean setDeletedIndex(int deletedIndex) {
		if (this.deletedIndex == deletedIndex)
			return false;
		this.deletedIndex = deletedIndex;
		return true;
	}
	
	public double evaluateMean(Value x) {
		tempValue[0] = x;
		return evaluateMean(tempValue);
	}
	
	public NumValue evaluateSD() {
		return s0;
	}
	
	public NumValue evaluateSD(Value x) {
		return s0;
	}
	
	public NumValue evaluateSD(Value[] x) {
		return evaluateSD(x[0]);
	}
	
	public void setSD(double newSD) {
		s0.setValue(newSD);
	}
	
	public NumValue getMinSD() {
		return s0;										//	Really only used for GroupsModelVariable
	}
	
//----------------------------------------------------------------------
	
	public double yToZ(double y, Value[] x) {
		return (y - evaluateMean(x)) / evaluateSD(x).toDouble();
	}
	
	public double zToY(double z, Value[] x) {
		return z * evaluateSD(x).toDouble() + evaluateMean(x);
	}
	
	public double getScaledDensity(double y, Value[] x) {
										//		density(y) = getScaledDensity(y) * getDensityFactor()
		double devn = yToZ(y, x);
		return Math.exp(-0.5 * devn * devn);
	}
	
	public double getCumulativeProb(double y, Value[] x) {
										//		p(Y <= y)
		return NormalDistnVariable.stdCumProb(yToZ(y, x));
	}
	
	public double getQuantile(double prob, Value[] x) {
		return zToY(NormalDistnVariable.stdQuantile(prob), x);
	}
	
//----------------------------------------------------------------------
	
	public double yToZ(double y, Value x) {
		tempValue[0] = x;
		return yToZ(y, tempValue);
	}
	
	public double zToY(double z, Value x) {
		tempValue[0] = x;
		return zToY(z, tempValue);
	}
	
	public double getScaledDensity(double y, Value x) {
		double devn = yToZ(y, x);
		return Math.exp(-0.5 * devn * devn);
	}
	
	public double getCumulativeProb(double y, Value x) {
		return NormalDistnVariable.stdCumProb(yToZ(y, x));
	}
	
	public double getQuantile(double prob, Value x) {
		return zToY(NormalDistnVariable.stdQuantile(prob), x);
	}
	
//----------------------------------------------------------------------
	
	public void drawModel(Graphics g, DataView view, NumCatAxis xAxis,
											NumCatAxis yAxis, Color fillColor, Color meanColor) {
								//		Only implemented for GroupsModelVariable and LinearModel
	}
	
	public void drawMean(Graphics g, DataView view, NumCatAxis xAxis, NumCatAxis yAxis) {
								//		Only implemented for GroupsModelVariable, MeanOnlyModel and LinearModel
	}
	
	public double getLeverage(Value[] x) {
		return Double.NaN;			
	}
	
	public double getLeverage(Value x) {
		tempValue[0] = x;
		return getLeverage(tempValue);			
	}
	
}
