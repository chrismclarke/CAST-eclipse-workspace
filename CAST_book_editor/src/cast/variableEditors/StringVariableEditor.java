package cast.variableEditors;

import java.awt.*;

import javax.swing.*;

import cast.exercise.*;
import cast.variationEditor.*;

public class StringVariableEditor extends CoreVariableEditor {
	
	private class StringValueEditor extends CoreValueEditor {
		private JTextField valueEditor;
		
		public StringValueEditor(JPanel valuePanel, GridBagConstraints constraints) {
			valueEditor = variation.createMonitoredTextField("", textFieldWidth);
			valuePanel.add(valueEditor, constraints);
		}
		
		public void setValue(String valueString) {
			valueEditor.setText(valueString);
			valueEditor.select(0,0);						//	so that the start of long strings is shown
		}
		
		public String getValue() {
			return valueEditor.getText();
		}
		
		public void clearEditor(JPanel valuePanel) {
			valuePanel.remove(valueEditor);
			valueEditor = null;
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
	
	private int textFieldWidth = 16;
	
	public StringVariableEditor(DomVariation variation, VariableType[] validParams, boolean hasIndex) {
		super(variation, validParams, hasIndex);
	}
	
	protected String getBaseType() {
		return "string";
	}
	
	public void setTypeDetails(String typeDetails) {
		super.setTypeDetails(typeDetails);
		if (!optionalVariable)
			textFieldWidth = Integer.parseInt(typeDetails);			//	textFieldWidth is only for display
	}
		
	public void initialiseValuePanel(JPanel valuePanel) {
		valuePanel.setLayout(new GridBagLayout());
		valueConstraint.gridy = 0;
	}
	
	public CoreValueEditor addOneValueEditor(JPanel valuePanel) {
		CoreValueEditor valueEditor = new StringValueEditor(valuePanel, valueConstraint);
		valueConstraint.gridy ++;
		
		return valueEditor;
	}
	
	public void removeLastValueEditor(JPanel valuePanel, CoreValueEditor valueEditor) {
		valueEditor.clearEditor(valuePanel);
		valueConstraint.gridy --;
	}
	
	public void createRandomValuePanel(JPanel valuePanel) {
	}
	
	public boolean hasRandomValue() {
		return false;
	}
	
	public void initialiseRandomValue(String initialValue) {		//	no random strings are possible
	}
	
	public String getRandomValue() {
		return null;
	}
	
	protected boolean isValidRandomValue() {
		return false;
	}
	
	public char arrayDelimiter() {
		return '*';
	}
}
