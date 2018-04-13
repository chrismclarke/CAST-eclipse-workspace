package cast.variableEditors;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import cast.exercise.*;
import cast.variationEditor.*;

public class IntVariableEditor extends CoreVariableEditor {
	
	private class IntValueEditor extends CoreValueEditor {
		private JTextField valueEditor;
		
		public IntValueEditor(JPanel valuePanel, GridBagConstraints constraints) {
			valueEditor = variation.createMonitoredTextField("", 6);
			
			valuePanel.add(valueEditor, constraints);
		}
		
		public void setValue(String valueString) {
			valueEditor.setText(valueString);
		}
		
		public String getValue() {
			return valueEditor.getText();
		}
		
		public void clearEditor(JPanel valuePanel) {
			valuePanel.remove(valueEditor);
			valueEditor = null;
		}
		
		public boolean isValidValue() {
			if (getValue().length() == 0 && optionalVariable)
				return true;
			try {
				int value = Integer.parseInt(getValue());
				return value >= minValue;
			} catch (NumberFormatException e) {
				return false;
			}
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
	
	private int minValue = Integer.MIN_VALUE;
	private JTextField lowRandomValue, highRandomValue;
	
	public IntVariableEditor(DomVariation variation, VariableType[] validParams, boolean hasIndex) {
		super(variation, validParams, hasIndex);
	}
	
	protected String getBaseType() {
		return "int";
	}
	
	public void setTypeDetails(String typeDetails) {
		if (typeDetails.equals("optional"))
			optionalVariable = true;
		else
			minValue = Integer.parseInt(typeDetails);
	}
	
	public void initialiseValuePanel(JPanel valuePanel) {
		valuePanel.setLayout(new GridBagLayout());
		
		valueConstraint.gridy = 0;
		JLabel heading = new JLabel("Value", JLabel.CENTER);
		valuePanel.add(heading, valueConstraint);
		valueConstraint.gridy ++;
	}
	
	public CoreValueEditor addOneValueEditor(JPanel valuePanel) {
		CoreValueEditor valueEditor = new IntValueEditor(valuePanel, valueConstraint);
		valueConstraint.gridy ++;
		
		return valueEditor;
	}
	
	public void removeLastValueEditor(JPanel valuePanel, CoreValueEditor valueEditor) {
		valueEditor.clearEditor(valuePanel);
		valueConstraint.gridy --;
	}
	
	public void createRandomValuePanel(JPanel valuePanel) {
		valuePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			lowRandomValue = variation.createMonitoredTextField("", 6);
		valuePanel.add(lowRandomValue);
		
			highRandomValue = variation.createMonitoredTextField("", 6);
			JLabel highValueLabel = new JLabel("to", JLabel.LEFT);
			highValueLabel.setLabelFor(highRandomValue);
		valuePanel.add(highValueLabel);
		valuePanel.add(highRandomValue);
	}
	
	public boolean hasRandomValue() {
		return true;
	}
	
	public void initialiseRandomValue(String initialValue) {
		StringTokenizer st = new StringTokenizer(initialValue, ":");
		lowRandomValue.setText(st.nextToken());
		highRandomValue.setText(st.nextToken());
	}
	
	public String getRandomValue() {
		return "(" + lowRandomValue.getText() + ":" + highRandomValue.getText() + ")";
	}
	
	protected boolean isValidRandomValue() {
		try {
			int lowValue = Integer.parseInt(lowRandomValue.getText());
			int highValue = Integer.parseInt(highRandomValue.getText());
			return lowValue >= minValue && highValue >= lowValue;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	public char arrayDelimiter() {
		return ',';
	}
	
}
