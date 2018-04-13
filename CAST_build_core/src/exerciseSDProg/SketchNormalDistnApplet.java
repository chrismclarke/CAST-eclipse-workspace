package exerciseSDProg;

import dataView.*;
import axis.*;
import distn.*;
import formula.*;

import exerciseSD.*;


public class SketchNormalDistnApplet extends SketchDistnApplet {
	
	protected CoreDragView getDataView(DataSet data, MultiHorizAxis theAxis) {
		return new NormalDragView(data, this, theAxis, null, "y");
	}
	
	
//-----------------------------------------------------------
	
	protected String getDisplayString() {
		return "normal distribution";
	}
	
	protected String getDragWording() {
		return "the two arrows to adjust the shape of the normal distribution to match the specified values of #mu# and #sigma#.";
	}
	
	protected String getToldWording() {
		return "The tails of the distribution virtually disappear at " + MText.expandText("#mu# #plusMinus# 3#sigma#.");
	}
	
//-----------------------------------------------------------
	
	protected DataSet getData() {
		DataSet data = new DataSet();
		
			NormalDistnVariable yVar = new NormalDistnVariable(getVarName());
		data.addVariable("y", yVar);
		
		return data;
	}
}