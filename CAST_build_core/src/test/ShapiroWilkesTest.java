package test;

import java.awt.*;

import dataView.*;
import qnUtils.*;


class TestResult {
	double statistic, pValue;
	TestResult(double statistic, double pValue) {
		this.statistic = statistic;
		this.pValue = pValue;
	}
}


public class ShapiroWilkesTest extends HypothesisTest {
	
	static private double ppnd(double p) {
		final double split = 0.42;
		final double a0 = 2.50662823884, a1 = -18.61500062529, a2 = 41.39119773534,
			a3 = -25.44106049637;
		final double b1 = -8.47351093090, b2 = 23.08336743743, b3 = -21.06224101826,
			b4 = 3.13082909833;
		final double c0 = -2.78718931138, c1 = -2.29796479134, c2 = 4.85014127135,
			c3 = 2.32121276858;
		final double d1 = 3.54388924762, d2 = 1.63706781897;
		
		double q = p - 0.5;
		if (Math.abs(q) > split) {
			double r = p;
			if (q > 0.0)
				r = 1.0 - p;
			if (r <= 0.0)
					throw new RuntimeException("Illegal prob passed to ppnd()");
			r = Math.sqrt(-Math.log(r));
			double result = (((c3*r + c2)*r + c1)*r + c0)/((d2*r + d1)*r + 1.0);
			if (q < 0.0)
				result = -result;
			return result;
		}
		else {
			double r = q * q;
			return q*(((a3*r + a2)*r + a1)*r + a0)/((((b4*r + b3)*r + b2)*r + b1)*r + 1.0);
		}
	}
	
	
	static private double alnorm(double x, boolean upper) {
		final double ltone = 7.0, utzero = 18.66, con = 1.28;
		final double p = 0.398942280444, q = 0.39990348504, r = 0.398942280385;
		final double a1 = 5.75885480458, a2 = 2.62433121679, a3 = 5.92885724438;
		final double b1 = -29.8213557807, b2 = 48.6959930692;
		final double c1 = -3.8052e-8, c2 = 3.98064794e-4, c3 = -0.151679116635,
							c4 = 4.8385912808, c5 = 0.742380924027, c6 = 3.99019417011;
		final double d1 = 1.00000615302, d2 = 1.98615381364, d3 = 5.29330324926,  
							d4 = -15.1508972451, d5 = 30.789933034;
		
		boolean up = upper;
		double z = x;
		double result;
		if (z < 0.0) {
			up = !up;
			z = -z;
		}
		if (z > ltone && (!up || z > utzero))
			result = 0.0;
		else {
			double y = 0.5 * z * z;
			if (z > con) {
				double expMinusY = Math.exp(-y);
				double t1 = d5 / (z + c6);
				double t2 = d4 / ( z + c5 + t1);
				double t3 = d3 / (z + c4 + t2);
				double t4 = d2 / (z + c3 + t3);
				double t5 = d1 / (z + c2 + t4);
				result = r * expMinusY / (z + c1 + t5);
//				result = r * expMinusY / (z + c1 + d1 / (z + c2 + d2 / (z + c3 + d3 / (z + c4
//																		+ d4 / ( z + c5 + d5 / (z + c6))))));
//					Netscape 4.5 (Mac) does not evaluate correctly -- result approx 1e-300
			}
			else
				result = 0.5 - z * (p - q * y / (y + a1 + b1 / (y + a2 + b2 / (y + a3))));
		}
		if (!up)
			result = 1.0 - result;
		return result;
	}
	
	static private double poly(double[] c, double x) {
		double result = c[0];
		if (c.length > 1) {
			double p = x * c[c.length - 1];
			if (c.length > 2)
				for (int j=c.length-2; j>0 ; j--)
					p = (p + c[j]) * x;
			result += p;
		}
		return result;
	}
	
	
	
	static private double[] initialise(int n) {
		final double[] c1 = {0.0E0, 0.221157E0, -0.147981E0, -0.207119E1, 0.4434685E1, -0.2706056E1};
		final double[] c2 = {0.0E0, 0.42981E-1, -0.293762E0, -0.1752461E1, 0.5682633E1, -0.3582633E1};
		final double sqrth = 0.70711;
		final double qtr = 0.25;
		final double th = 0.375;
		
		int n2 = n / 2;
		double[] a = new double[n2];
		if (n == 3)
			a[0] = sqrth;
		else {
			double an25 = n + qtr;
			double summ2 = 0.0;
			for (int i=0 ; i<n2 ; i++) {
				a[i] = ppnd((i + 1 - th) / an25);
				summ2 += a[i] * a[i];
			}
			summ2 *= 2.0;
			double ssumm2 = Math.sqrt(summ2);
			double rsn = 1.0 / Math.sqrt(n);
			double a1 = poly(c1, rsn) - a[0] / ssumm2;
			
			int i1;
			double fac;
			if (n > 5) {
				i1 = 2;
				double a2 = -a[1] / ssumm2 + poly(c2, rsn);
				fac = Math.sqrt((summ2 - 2.0 * a[0] * a[0] - 2.0 * a[1] * a[1])
																/ (1.0 - 2.0 * a1 * a1 - 2.0 * a2 * a2));
				a[0] = a1;
				a[1] = a2;
			}
			else {
				i1 = 1;
				fac = Math.sqrt((summ2 - 2.0 * a[0] * a[0]) / (1.0 - 2.0 * a1 * a1));
				a[0] = a1;
			}
			
			for (int i=i1 ; i<n2 ; i++)
				a[i] = -a[i] / fac;
		}
		return a;
	}
	
	
	static private TestResult sWilk(double[] x, double[] a) {
		final double small = 1.0e-19;
		final double p16 = 0.1909859e1;
		final double stqr = 0.1047198e1;
		final double[] c3 = {0.5440e0, -0.39978e0, 0.25054e-1, -0.6714e-3};
		final double[] c4 = {0.13822e1, -0.77857e0, 0.62767e-1, -0.20322e-2};
		final double[] c5 = {-0.15861e1, -0.31082e0, -0.83751e-1, 0.38915e-2};
		final double[] c6 = {-0.4803e0, -0.82676e-1, 0.30302e-2};
		final double[] g = {-0.2273e1, 0.459e0};
		
		double pw = 1.0;
		double w = 1.0;
		int n = x.length;
//		int n2 = n / 2;
		if (n < 3)
			return new TestResult(w, pw);
		
		double range = x[n - 1] - x[0];
		if (range < small)
			return new TestResult(w, pw);
		
		double xx = x[0] / range;
		double sx = xx;
		double sa = -a[0];
	
		for (int i=1, j=n-2 ; i<n ; i++, j--) {
			double xi = x[i] / range;
			if (xx - xi > small)
				throw new RuntimeException("Data not in increasing order");
			sx += xi;
			if (i != j)
				sa += ((i > j) ? a[j] : -a[i]);
			xx = xi;
		}
		sa = sa / n;
		sx = sx / n;
		double ssa = 0.0;
		double ssx = 0.0;
		double sax = 0.0;
		
		for (int i=0, j=n-1 ; i<n ; i++, j--) {
			double asa;
			if (i != j)
				asa = ((i > j) ? a[j] : -a[i]) - sa;
			else
				asa = -sa;
			double xsx = x[i] / range - sx;
			ssa += asa * asa;
			ssx += xsx * xsx;
			sax += asa * xsx;
		}
		
		double ssassx = Math.sqrt(ssa * ssx);
		double w1 = (ssassx - sax) * (ssassx + sax) / (ssa * ssx);
		w = 1.0 - w1;
		
		if (n == 3)
			pw = p16 * (Math.asin(Math.sqrt(w)) - stqr);
		else {
			double y = Math.log(w1);
			xx = Math.log(n);
			double m = 0.0;
			double s = 1.0;
			if (n <= 11) {
				double gamma = poly(g, n);
				if (y <= gamma)
					return new TestResult(w, small);
				
				y = -Math.log(gamma - y);
				m = poly(c3, n);
				s = Math.exp(poly(c4, n));
			}
			else {
				m = poly(c5, xx);
				s = Math.exp(poly(c6, xx));
			}
			pw = alnorm((y - m) / s, true);
		}
		return new TestResult(w, pw);
	}
	
//-------------------------------------------------------------------------------
	
	private String nullHypothString, altHypothString;
	
	private double a[] = null;
	private int n = 0;
	private String yKey;
	
	public ShapiroWilkesTest(DataSet data, String yKey, XApplet applet) {
		super(data, null, HA_NOT_EQUAL, CUSTOM, applet);
		this.yKey = yKey;
		
		nullHypothString = applet.translate("Distn is normal");
		altHypothString = applet.translate("Distn is not normal");
	}
	
	public double evaluateStatistic() {
		NumVariable yVar = (NumVariable)data.getVariable(yKey);
		int nNew = yVar.noOfValues();
		
		if (a == null || nNew != n) {
			n = nNew;
			a = initialise(n);
		}
		
		double x[] = new double[n];
		NumValue xVal[] = yVar.getSortedData();
		for (int i=0 ; i<n ; i++)
			x[i] = xVal[i].toDouble();
		
		return sWilk(x, a).statistic;
	}
	
	public double evaluatePValue() {
		NumVariable yVar = (NumVariable)data.getVariable(yKey);
		int nNew = yVar.noOfValues();
		
		if (a == null || nNew != n) {
			n = nNew;
			a = initialise(n);
		}
		
		double x[] = new double[n];
		NumValue xVal[] = yVar.getSortedData();
		for (int i=0 ; i<n ; i++)
			x[i] = xVal[i].toDouble();

		TestResult result = sWilk(x, a);

//************************
//		System.out.println("W = " + result.statistic + ", p = " + result.pValue);
//************************
		return result.pValue;
	}
	
	protected Image getParamImage() {
		return null;
	}
	
	public Dimension getSize(Graphics g, int drawType) {
		if (drawType == GENERIC_DRAW)
			return super.getSize(g, drawType);
		else {
			FontMetrics fm = g.getFontMetrics();
			int width = Math.max(fm.stringWidth(nullHypothString),
																			fm.stringWidth(altHypothString));
			return new Dimension(width, fm.getAscent() + fm.getDescent());
		}
	}
	
	public int getBaselineFromTop(Graphics g, int drawType) {
		if (drawType == GENERIC_DRAW)
			return super.getBaselineFromTop(g, drawType);
		else
			return g.getFontMetrics().getAscent();
	}
	
	public void paintBlue(Graphics g, int left, int baseline, boolean showNull,
																						int drawType, Component c) {
		if (drawType == GENERIC_DRAW)
			super.paintBlue(g, left, baseline, showNull, drawType, c);
		else {
			Color oldColor = g.getColor();
			g.setColor(Color.blue);
			
			String hypothString = showNull ? nullHypothString : altHypothString;
			g.drawString(hypothString, left, baseline);
			
			g.setColor(oldColor);
		}
	}
	
	
}