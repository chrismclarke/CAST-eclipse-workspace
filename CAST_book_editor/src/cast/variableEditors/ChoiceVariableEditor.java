package cast.variableEditors;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import cast.exercise.*;
import cast.variationEditor.*;

public class ChoiceVariableEditor extends CoreVariableEditor {
	
	private class ChoiceValueEditor extends CoreValueEditor {
		private JComboBox theChoice;
		
		public ChoiceValueEditor(JPanel valuePanel, GridBagConstraints constraints) {
			theChoice = variation.createMonitoredMenu(null);
			theChoice = new JComboBox(choiceString);
			valuePanel.add(theChoice, constraints);
		}
		
		public void setValue(String valueString) {
			if (returnsInt) {
				int theValue = Integer.parseInt(valueString);
				theChoice.setSelectedIndex(theValue);
			}
			else
				theChoice.setSelectedItem(valueString);
		}
		
		public String getValue() {
			return returnsInt ? String.valueOf(theChoice.getSelectedIndex()) : (String)theChoice.getSelectedItem();
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
	
	private boolean returnsInt;
	private String choiceString[];
	
	public ChoiceVariableEditor(DomVariation variation, VariableType[] validParams,
																																boolean returnsInt, boolean hasIndex) {
		super(variation, validParams, hasIndex);
		this.returnsInt = returnsInt;
	}
	
	protected String getBaseType() {
		return (returnsInt ? "int" : "string") + "_choice";
	}
	
	public void setTypeDetails(String typeDetails) {
		StringTokenizer st = new StringTokenizer(typeDetails, "#");
		int noOfOptions = st.countTokens();
		choiceString = new String[noOfOptions];
		
		for (int i=0 ; i<noOfOptions ; i++)
			choiceString[i] = st.nextToken();
	}
		
	public void initialiseValuePanel(JPanel valuePanel) {
		valuePanel.setLayout(new GridBagLayout());
		valueConstraint.gridy = 0;
	}
	
	public CoreValueEditor addOneValueEditor(JPanel valuePanel) {
		CoreValueEditor valueEditor = new ChoiceValueEditor(valuePanel, valueConstraint);
		valueConstraint.gridy ++;
		
		return valueEditor;
	}
	
	public void removeLastValueEditor(JPanel valuePanel, CoreValueEditor valueEditor) {
		valueEditor.clearEditor(valuePanel);
		valueConstraint.gridy --;
	}
	
	protected boolean isRandomChoice(String initialValue) {
		if (returnsInt) {
			if (!initialValue.startsWith("(0:") || !initialValue.endsWith(")"))
				return false;
			initialValue = initialValue.substring(3, initialValue.length() - 1);
			String countString = String.valueOf(choiceString.length - 1);
			return countString.equals(initialValue);
		}
		else {
			if (!initialValue.startsWith("[](") || !initialValue.endsWith(")"))
				return false;
			
			initialValue = initialValue.substring(3, initialValue.length() - 1);
			StringTokenizer st = new StringTokenizer(initialValue, "*");
			if (st.countTokens() != choiceString.length)
				return false;
			
			for (int i=0 ; i<choiceString.length ; i++)
				if (!choiceString[i].equals(st.nextToken()))
					return false;
			
			return true;
		}
	}
	
	public void createRandomValuePanel(JPanel valuePanel) {
	}
	
	public boolean hasRandomValue() {
		return true;
	}
	
	public void initialiseRandomValue(String initialValue) {		//	always allows all possible values
	}
	
	public String getRandomValue() {
		if (returnsInt) {
			int nOptions = choiceString.length;
			return "(0:" + (nOptions - 1) + ")";
		}
		else {
			String s = "[](";
			for (int i=0 ; i<choiceString.length ; i++) {
				if (i > 0)
					s += "*";
				s += choiceString[i];
			}
			
			return s + ")";
		}
	}
	
	protected boolean isValidRandomValue() {
		return true;
	}
	
	public char arrayDelimiter() {
		return returnsInt ? ',' : '*';
	}
}
