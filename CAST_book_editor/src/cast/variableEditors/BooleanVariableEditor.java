package cast.variableEditors;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import cast.exercise.*;
import cast.variationEditor.*;

public class BooleanVariableEditor extends CoreVariableEditor {
	
	private class BooleanValueEditor extends CoreValueEditor {
		private JComboBox theChoice;
		
		public BooleanValueEditor(JPanel valuePanel, GridBagConstraints constraints) {
			theChoice = variation.createMonitoredMenu(null);
			theChoice = new JComboBox(trueFalseString);
			valuePanel.add(theChoice, constraints);
		}
		
		public void setValue(String valueString) {
			if (valueString.equals("true"))
				theChoice.setSelectedIndex(0);
			else
				theChoice.setSelectedIndex(1);
		}
		
		public String getValue() {
			return (theChoice.getSelectedIndex() == 0) ? "true" : "false";
		}
		
		public void clearEditor(JPanel valuePanel) {
			valuePanel.remove(theChoice);
			theChoice = null;
		}
		
		public boolean isValidValue() {
			return true;
		}
	}
	
//-----------------------------------------------------------------------------
	
	private GridBagConstraints valueConstraint = new GridBagConstraints(
																										0, 0,														//	gridx & gridy
																										1, 1,														//	gridwidth & gridheight
																										0, 0,														//	weightx & weighty,
																										GridBagConstraints.NORTH,				//	anchor
																										GridBagConstraints.NONE,				//	fill
																										new Insets(0, 0, 0, 0),					//	insets
																										0, 0);													//	ipadx & ipady
	
	private String trueFalseString[];
	
	public BooleanVariableEditor(DomVariation variation, VariableType[] validParams, boolean hasIndex) {
		super(variation, validParams, hasIndex);
	}
	
	protected String getBaseType() {
		return "boolean";
	}
	
	public void setTypeDetails(String typeDetails) {
		trueFalseString = new String[2];
		StringTokenizer st = new StringTokenizer(typeDetails, "#");
		if (st.countTokens() != 2) {
			trueFalseString[0] = "true";
			trueFalseString[1] = "false";
		}
		else {
			trueFalseString[0] = st.nextToken();
			trueFalseString[1] = st.nextToken();
		}
	}
	
	public void initialiseValuePanel(JPanel valuePanel) {
		valuePanel.setLayout(new GridBagLayout());
		valueConstraint.gridy = 0;
	}
	
	public CoreValueEditor addOneValueEditor(JPanel valuePanel) {
		CoreValueEditor valueEditor = new BooleanValueEditor(valuePanel, valueConstraint);
		valueConstraint.gridy ++;
		
		return valueEditor;
	}
	
	public void removeLastValueEditor(JPanel valuePanel, CoreValueEditor valueEditor) {
		valueEditor.clearEditor(valuePanel);
		valueConstraint.gridy --;
	}
	
	
	
	protected boolean isRandomChoice(String initialValue) {
		return initialValue.equals("(:)");
	}
	
	public void createRandomValuePanel(JPanel valuePanel) {
	}
	
	public boolean hasRandomValue() {
		return true;
	}
	
	public void initialiseRandomValue(String initialValue) {		//	always allows all possible values
	}
	
	public String getRandomValue() {
		return "(:)";
	}
	
	protected boolean isValidRandomValue() {
		return true;
	}
	
	public char arrayDelimiter() {
		return ',';
	}
	
}
