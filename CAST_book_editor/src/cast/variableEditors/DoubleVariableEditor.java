package cast.variableEditors;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import cast.exercise.*;
import cast.variationEditor.*;

public class DoubleVariableEditor extends CoreVariableEditor {
	
	private class DoubleValueEditor extends CoreValueEditor {
		private JTextField valueEditor;
		
		public DoubleValueEditor(JPanel valuePanel, GridBagConstraints constraints) {
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
			if (optionalVariable && valueEditor.getText().length() == 0)
				return true;
			try {
				@SuppressWarnings("unused")
				double value = Double.parseDouble(getValue());
				return true;
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
	
	private JTextField lowRandomValue, highRandomValue;
	
	public DoubleVariableEditor(DomVariation variation, VariableType[] validParams, boolean hasIndex) {
		super(variation, validParams, hasIndex);
	}
	
	protected String getBaseType() {
		return "double";
	}
	
	public void initialiseValuePanel(JPanel valuePanel) {
		valuePanel.setLayout(new GridBagLayout());
		
		valueConstraint.gridy = 0;
		JLabel heading = new JLabel("Value", JLabel.CENTER);
		valuePanel.add(heading, valueConstraint);
		valueConstraint.gridy ++;
	}
	
	public CoreValueEditor addOneValueEditor(JPanel valuePanel) {
		CoreValueEditor valueEditor = new DoubleValueEditor(valuePanel, valueConstraint);
		valueConstraint.gridy ++;
		
		return valueEditor;
	}
	
	public void removeLastValueEditor(JPanel valuePanel, CoreValueEditor valueEditor) {
		valueEditor.clearEditor(valuePanel);
		valueConstraint.gridy --;
	}
	
	public void createRandomValuePanel(JPanel valuePanel) {
			lowRandomValue = variation.createMonitoredTextField("", 6);
			JLabel lowValueLabel = new JLabel("between", JLabel.LEFT);
			lowValueLabel.setLabelFor(lowRandomValue);
		valuePanel.add(lowValueLabel);
		valuePanel.add(lowRandomValue);
		
			highRandomValue = variation.createMonitoredTextField("", 6);
			JLabel highValueLabel = new JLabel("and", JLabel.LEFT);
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
			double lowValue = Double.parseDouble(lowRandomValue.getText());
			double highValue = Double.parseDouble(highRandomValue.getText());
			return highValue >= lowValue;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	public char arrayDelimiter() {
		return ',';
	}
}
