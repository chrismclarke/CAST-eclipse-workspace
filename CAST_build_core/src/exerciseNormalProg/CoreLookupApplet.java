package exerciseNormalProg;

import java.awt.*;

import dataView.*;
import exercise2.*;


abstract public class CoreLookupApplet extends ExerciseApplet {	
	
	static final protected Color kTemplateColor = new Color(0x000099);	//	dark blue
	static final protected Color kTemplateBackground = new Color(0xFFE594);
	
	abstract public String getAxisInfo();
	abstract public String getVarName();
	abstract public NumValue getMaxValue();
}