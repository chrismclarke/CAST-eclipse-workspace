package matrix;

import dataView.*;
import models.*;


public class LsFactorCoeffMatrixView extends LsCoeffMatrixView {
//	static public final String LS_FACTOR_MATRIX_VIEW = "LsFactorCoeffMatrixView";
	
	public LsFactorCoeffMatrixView(DataSet theData, XApplet applet, String lsKey) {
		super(theData, applet, lsKey);
	}
	
	protected NumValue getParameter(MultipleRegnModel lsModel, int row) {
		NumValue group0Mean = lsModel.getParameter(0);
		
		if (columns[0] instanceof FactorTerm) {
			if (row == 0)
				return group0Mean;
			else {
				double groupOffset = lsModel.getParameter(row).toDouble();
				return new NumValue(group0Mean.toDouble() + groupOffset, group0Mean.decimals);
			}
		}
		else if (columns[1] instanceof FactorTerm) {
			int baselineIndex = ((FactorTerm)columns[1]).getBaselineLevel();
			double baselineDiff = (baselineIndex == 0) ? 0.0
																				: lsModel.getParameter(baselineIndex).toDouble();
			
			if (row == 0)
				return new NumValue(group0Mean.toDouble() + baselineDiff, group0Mean.decimals);
			else {
				int groupIndex = (row <= baselineIndex) ? row - 1 : row;
				double groupOffset = (groupIndex == 0) ? 0.0
																						: lsModel.getParameter(groupIndex).toDouble();
				return new NumValue(groupOffset - baselineDiff, group0Mean.decimals);
			}
		}
		else {
			if (row == 0)
				return group0Mean;
			else {
				ContrastTerm contrast = (ContrastTerm)columns[1];
				if (contrast.isSingleIndicator()) {
					int contrastValues[] = contrast.getContrastValues();
					int level2Index = 0;
					for (int i=0 ; i<contrastValues.length ; i++)
						if (contrastValues[i] == 1) {
							level2Index = i;
							break;
						}
					if (row == 1)
						return lsModel.getParameter(level2Index);
					else if (row <= level2Index)
						return lsModel.getParameter(row - 1);
					else {
						double group2Baseline = lsModel.getParameter(level2Index).toDouble();
						double groupMean = lsModel.getParameter(row).toDouble();
						return new NumValue(groupMean - group2Baseline, group0Mean.decimals);
					}
				}
				else {
					NumValue slope = lsModel.getParameter(1);
					if (row == 1)
						return slope;
					else {
						double groupBaseline = row * slope.toDouble();
						double groupOffset = lsModel.getParameter(row).toDouble();
						return new NumValue(groupOffset - groupBaseline, group0Mean.decimals);
					}
				}
			}
		}
	}
}