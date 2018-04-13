package cast.variableEditors;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import cast.exercise.*;
import cast.variationEditor.*;

public class MeanSdVariableEditor extends CoreVariableEditor {
	
	private class MeanSdValueEditor extends CoreValueEditor {
		private JTextField meanEditor, sdEditor;
		
		public MeanSdValueEditor(JPanel valuePanel, GridBagConstraints meanConstraint, GridBagConstraints sdConstraint) {
			meanEditor = variation.createMonitoredTextField("", 5);
			sdEditor = variation.createMonitoredTextField("", 5);
			valuePanel.add(meanEditor, meanConstraint);
			valuePanel.add(sdEditor, sdConstraint);
		}
		
		public void setValue(String valueString) {
			StringTokenizer st = new StringTokenizer(valueString);
			meanEditor.setText(st.nextToken());
			sdEditor.setText(st.nextToken());
		}
		
		public String getValue() {
			return meanEditor.getText() + " " + sdEditor.getText();
		}
		
		public void clearEditor(JPanel valuePanel) {
			valuePanel.remove(meanEditor);
			valuePanel.remove(sdEditor);
			meanEditor = null;
			sdEditor = null;
		}
		
		public boolean isValidValue() {
			try {
				@SuppressWarnings("unused")
				double meanValue = Double.parseDouble(meanEditor.getText());
				double sdValue = Double.parseDouble(sdEditor.getText());
				return sdValue > 0.0;
			} catch (NumberFormatException e) {
				return false;
			}
		}
	}
	
//-----------------------------------------------------------------------------
	
	private GridBagConstraints meanConstraint = new GridBagConstraints(
																										0, 0,														//	gridx & gridy
																										1, 1,														//	gridwidth & gridheight
																										0, 0,														//	weightx & weighty,
																										GridBagConstraints.NORTH,				//	anchor
																										GridBagConstraints.NONE,				//	fill
																										new Insets(0, 0, 0, 0),					//	insets
																										5, 0);													//	ipadx & ipady
	
	private GridBagConstraints sdConstraint = new GridBagConstraints(
																										1, 0,														//	gridx & gridy
																										1, 1,														//	gridwidth & gridheight
																										0, 0,														//	weightx & weighty,
																										GridBagConstraints.NORTH,				//	anchor
																										GridBagConstraints.NONE,				//	fill
																										new Insets(0, 0, 0, 0),					//	insets
																										5, 0);													//	ipadx & ipady
	
	private JTextField propnRandomValue, zRandomValue, decimalsRandomValue;
	
	public MeanSdVariableEditor(DomVariation variation, VariableType[] validParams, boolean hasIndex) {
		super(variation, validParams, hasIndex);
	}
	
	protected String getBaseType() {
		return "meanSd";
	}
	
	public void initialiseValuePanel(JPanel valuePanel) {
		valuePanel.setLayout(new GridBagLayout());
		
		meanConstraint.gridy = 0;
		JLabel minLabel = new JLabel("Mean", JLabel.CENTER);
		valuePanel.add(minLabel, meanConstraint);
		meanConstraint.gridy ++;
		
		sdConstraint.gridy = 0;
		JLabel stepLabel = new JLabel("SD", JLabel.CENTER);
		valuePanel.add(stepLabel, sdConstraint);
		sdConstraint.gridy ++;
	}
	
	public CoreValueEditor addOneValueEditor(JPanel valuePanel) {
		CoreValueEditor valueEditor = new MeanSdValueEditor(valuePanel, meanConstraint, sdConstraint);
		meanConstraint.gridy ++;
		sdConstraint.gridy ++;
		
		return valueEditor;
	}
	
	public void removeLastValueEditor(JPanel valuePanel, CoreValueEditor valueEditor) {
		valueEditor.clearEditor(valuePanel);
		meanConstraint.gridy --;
		sdConstraint.gridy --;
	}
	
	public void createRandomValuePanel(JPanel valuePanel) {
		valuePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
			zRandomValue = variation.createMonitoredTextField("", 4);
			JLabel highValueLabel = new JLabel("Propn of axis used by +/-", JLabel.LEFT);
			highValueLabel.setLabelFor(zRandomValue);
		valuePanel.add(highValueLabel);
		valuePanel.add(zRandomValue);
		
			propnRandomValue = variation.createMonitoredTextField("", 4);
			JLabel propnValueLabel = new JLabel("SD is at least", JLabel.LEFT);
			propnValueLabel.setLabelFor(propnRandomValue);
		valuePanel.add(propnValueLabel);
		valuePanel.add(propnRandomValue);
		
			decimalsRandomValue = variation.createMonitoredTextField("", 4);
			JLabel decimalsValueLabel = new JLabel("with", JLabel.LEFT);
			decimalsValueLabel.setLabelFor(decimalsRandomValue);
		valuePanel.add(decimalsValueLabel);
		valuePanel.add(decimalsRandomValue);
		
			JLabel endLabel = new JLabel("decimals.", JLabel.LEFT);
		valuePanel.add(endLabel);
	}
	
	public boolean hasRandomValue() {
		return true;
	}
	
	public void initialiseRandomValue(String initialValue) {
		StringTokenizer st = new StringTokenizer(initialValue, ":");
		propnRandomValue.setText(st.nextToken());
		decimalsRandomValue.setText(st.nextToken());
		zRandomValue.setText(st.hasMoreTokens() ? st.nextToken() : "3");
	}
	
	public String getRandomValue() {
		String result = "(" + propnRandomValue.getText() + ":" + decimalsRandomValue.getText();
		double zValue = Double.valueOf(zRandomValue.getText());
		if (zValue != 3.0)
			result += ":" + zRandomValue.getText();
		result += ")";
		return result;
	}
	
	protected boolean isValidRandomValue() {
		try {
			double propnValue = Double.parseDouble(propnRandomValue.getText());
			if (propnValue <= 0 || propnValue > 1)
				return false;
			@SuppressWarnings("unused")
			int decimals = Integer.parseInt(decimalsRandomValue.getText());
			if (zRandomValue.getText().length() > 0) {
				double zValue = Double.parseDouble(zRandomValue.getText());
				return zValue > 0;
			}
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	public char arrayDelimiter() {
		return '*';
	}
}
