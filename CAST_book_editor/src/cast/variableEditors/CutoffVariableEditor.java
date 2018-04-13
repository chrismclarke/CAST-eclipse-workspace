package cast.variableEditors;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import cast.utils.*;
import cast.exercise.*;
import cast.variationEditor.*;

public class CutoffVariableEditor extends CoreVariableEditor {
	
	private class CutoffValueEditor extends CoreValueEditor {
		private JTextField lowEditor, highEditor;
		
		public CutoffValueEditor(JPanel valuePanel, GridBagConstraints lowConstraint, GridBagConstraints highConstraint) {
			lowEditor = variation.createMonitoredTextField("", 5);
			highEditor = variation.createMonitoredTextField("", 5);
			valuePanel.add(lowEditor, lowConstraint);
			valuePanel.add(highEditor, highConstraint);
		}
		
		public void setValue(String valueString) {
			StringTokenizer st = new StringTokenizer(valueString, ",");
			lowEditor.setText(st.nextToken());
			highEditor.setText(st.nextToken());
		}
		
		public String getValue() {
			return lowEditor.getText() + "," + highEditor.getText();
		}
		
		public void clearEditor(JPanel valuePanel) {
			valuePanel.remove(lowEditor);
			valuePanel.remove(highEditor);
			lowEditor = null;
			highEditor = null;
		}
		
		public boolean isValidValue() {
			try {
				double lowValue = Double.parseDouble(lowEditor.getText());
				double highValue = Double.parseDouble(highEditor.getText());
				return lowValue < highValue;
			} catch (NumberFormatException e) {
				return false;
			}
		}
	}
	
//-----------------------------------------------------------------------------
	
	private GridBagConstraints lowConstraint = new GridBagConstraints(
																										0, 0,														//	gridx & gridy
																										1, 1,														//	gridwidth & gridheight
																										0, 0,														//	weightx & weighty,
																										GridBagConstraints.NORTH,				//	anchor
																										GridBagConstraints.NONE,				//	fill
																										new Insets(0, 0, 0, 0),					//	insets
																										5, 0);													//	ipadx & ipady
	
	private GridBagConstraints highConstraint = new GridBagConstraints(
																										1, 0,														//	gridx & gridy
																										1, 1,														//	gridwidth & gridheight
																										0, 0,														//	weightx & weighty,
																										GridBagConstraints.NORTH,				//	anchor
																										GridBagConstraints.NONE,				//	fill
																										new Insets(0, 0, 0, 0),					//	insets
																										5, 0);													//	ipadx & ipady
	
	private JTextField lowRandomZ, highRandomZ, decimalsRandomValue;
	
	public CutoffVariableEditor(DomVariation variation, VariableType[] validParams, boolean hasIndex) {
		super(variation, validParams, hasIndex);
	}
	
	protected String getBaseType() {
		return "cut-offs";
	}
	
	public void initialiseValuePanel(JPanel valuePanel) {
		valuePanel.setLayout(new GridBagLayout());
		
		lowConstraint.gridy = 0;
		JLabel lowLabel = new JLabel("Low limit", JLabel.CENTER);
		valuePanel.add(lowLabel, lowConstraint);
		lowConstraint.gridy ++;
		
		highConstraint.gridy = 0;
		JLabel highLabel = new JLabel("High limit", JLabel.CENTER);
		valuePanel.add(highLabel, highConstraint);
		highConstraint.gridy ++;
	}
	
	public CoreValueEditor addOneValueEditor(JPanel valuePanel) {
		CoreValueEditor valueEditor = new CutoffValueEditor(valuePanel, lowConstraint, highConstraint);
		lowConstraint.gridy ++;
		highConstraint.gridy ++;
		
		return valueEditor;
	}
	
	public void removeLastValueEditor(JPanel valuePanel, CoreValueEditor valueEditor) {
		valueEditor.clearEditor(valuePanel);
		lowConstraint.gridy --;
		highConstraint.gridy --;
	}
	
	public void createRandomValuePanel(JPanel valuePanel) {
		valuePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		
		valuePanel.add(new JLabel("Cutoffs generated between z-scores", JLabel.CENTER));
		
			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			mainPanel.setOpaque(false);
			
				lowRandomZ = variation.createMonitoredTextField("", 3);
				JLabel lowValueLabel = new JLabel("z =", JLabel.LEFT);
				lowValueLabel.setLabelFor(lowRandomZ);
			mainPanel.add(lowValueLabel);
			mainPanel.add(lowRandomZ);
			
				highRandomZ = variation.createMonitoredTextField("", 3);
				JLabel highValueLabel = new JLabel("&", JLabel.LEFT);
				highValueLabel.setLabelFor(highRandomZ);
			mainPanel.add(highValueLabel);
			mainPanel.add(highRandomZ);
			
				decimalsRandomValue = variation.createMonitoredTextField("", 2);
				JLabel decimalsValueLabel = new JLabel("with", JLabel.LEFT);
				decimalsValueLabel.setLabelFor(decimalsRandomValue);
			mainPanel.add(decimalsValueLabel);
			mainPanel.add(decimalsRandomValue);
			
				JLabel endLabel = new JLabel("decimals.", JLabel.LEFT);
			mainPanel.add(endLabel);
			
		valuePanel.add(mainPanel);
	}
	
	public boolean hasRandomValue() {
		return true;
	}
	
	public void initialiseRandomValue(String initialValue) {
		StringTokenizer st = new StringTokenizer(initialValue, ":");
		lowRandomZ.setText(st.nextToken());
		highRandomZ.setText(st.nextToken());
		decimalsRandomValue.setText(st.nextToken());
	}
	
	public String getRandomValue() {
		return "(" + lowRandomZ.getText() + ":" + highRandomZ.getText() + ":" + decimalsRandomValue.getText() + ")";
	}
	
	protected boolean isValidRandomValue() {
		try {
			double lowZ = Double.parseDouble(lowRandomZ.getText());
			double highZ = Double.parseDouble(highRandomZ.getText());
			if (lowZ >= highZ)
				return false;
			@SuppressWarnings("unused")
			int decimals = Integer.parseInt(decimalsRandomValue.getText());
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	public char arrayDelimiter() {
		return '*';
	}
}
