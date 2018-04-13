package cast.variationEditor;

import javax.swing.*;


abstract public class CoreValueEditor {
	
	abstract public void setValue(String valueString);
	abstract public String getValue();
	abstract public void clearEditor(JPanel valuePanel);
	abstract public boolean isValidValue();
}