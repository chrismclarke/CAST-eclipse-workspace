package multiRegnProg;

import dataView.*;
import models.*;

import multiRegn.*;


public class TestApplet extends XApplet {
	
	private CoreModelDataSet data;
	
	public void setupApplet() {
		data = new MultiRegnDataSet(this);
		
		doUniRegn();
		doMultiRegn();
		doMultiRegn2();
	}
	
	private void doUniRegn() {
//		MultipleRegnModel ls = (MultipleRegnModel)data.getVariable("ls");
		
		int noOfX = 1;
		double[] xy = new double[noOfX + 2];
		double[] r = LinearModel.initSsqMatrix(noOfX + 2);
		
		int nObs = 0;
		
		ValueEnumeration xe = ((NumVariable)data.getVariable("x")).values();
		ValueEnumeration ye = ((NumVariable)data.getVariable("y")).values();
		
		try {
			while (ye.hasMoreValues()) {
				xy[0] = 1.0;
				xy[1] = xe.nextDouble();
				xy[2] = ye.nextDouble();
				MultipleRegnModel.givenC(r, xy, 1.0);
				nObs ++;
			}
			
			double bVar[] = MultipleRegnModel.variance(r, 3, nObs, null, true, 1.0);
			
			int ij = 0;
			for (int i=0 ; i<2 ; i++) {
				for (int j=0 ; j<=i ; j++)
					System.out.print(bVar[ij++] + "  ");
				System.out.print("\n");
			}
		} catch (GivensException e) {
			System.err.println(e);
		}
	}
	
	private void doMultiRegn() {
//		MultipleRegnModel ls = (MultipleRegnModel)data.getVariable("ls");
		
		int noOfX = 2;
		double[] xy = new double[noOfX + 2];
		double[] r = MultipleRegnModel.initSsqMatrix(noOfX + 2);
		
		int nObs = 0;
		
		ValueEnumeration xe = ((NumVariable)data.getVariable("x")).values();
		ValueEnumeration ze = ((NumVariable)data.getVariable("z")).values();
		ValueEnumeration ye = ((NumVariable)data.getVariable("y")).values();
		
		try {
			while (ye.hasMoreValues()) {
				xy[0] = 1.0;
				xy[1] = xe.nextDouble();
				xy[2] = ze.nextDouble();
				xy[3] = ye.nextDouble();
				MultipleRegnModel.givenC(r, xy, 1.0);
				nObs ++;
			}
			
			double bVar[] = MultipleRegnModel.variance(r, 4, nObs, null, true, 1.0);
			
			int ij = 0;
			for (int i=0 ; i<3 ; i++) {
				for (int j=0 ; j<=i ; j++)
					System.out.print(bVar[ij++] + "  ");
				System.out.print("\n");
			}
		} catch (GivensException e) {
			System.err.println(e);
		}
	}
	
	
	private void doMultiRegn2() {
		MultipleRegnModel ls = (MultipleRegnModel)data.getVariable("ls");
		double bVar[] = ls.getCoeffVariances("y", true, 1.0);
			
		int ij = 0;
		for (int i=0 ; i<3 ; i++) {
			for (int j=0 ; j<=i ; j++)
				System.out.print(bVar[ij++] + "  ");
			System.out.print("\n");
		}
	}
}