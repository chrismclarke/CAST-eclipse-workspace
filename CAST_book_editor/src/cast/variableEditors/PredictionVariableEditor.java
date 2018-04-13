package cast.variableEditors;

import java.awt.*;
import java.util.*;

import javax.swing.*;

import cast.utils.*;
import cast.exercise.*;
import cast.variationEditor.*;

public class PredictionVariableEditor extends CoreVariableEditor {
	static final private String[] kProblemTypes = {"Standard", "Outlier", "Leverage", "Extrapolation", "Extrapolation with outlier"};
	
	private class PredictionValueEditor extends CoreValueEditor {
		private JComboBox predictionTypeChoice;
		private JTextField predictionXEditor;
		
		public PredictionValueEditor(JPanel valuePanel, GridBagConstraints extremeTypeConstraint,
																														GridBagConstraints predictionXConstraint) {
			predictionTypeChoice = variation.createMonitoredMenu(kProblemTypes);
			valuePanel.add(predictionTypeChoice, extremeTypeConstraint);
			predictionXEditor = variation.createMonitoredTextField("", 5);
			valuePanel.add(predictionXEditor, predictionXConstraint);
		}
		
		public void setValue(String valueString) {
			StringTokenizer st = new StringTokenizer(valueString);
			int problemIndex = Integer.parseInt(st.nextToken());		//		must be between 0 and 4
			predictionTypeChoice.setSelectedIndex(problemIndex);
			predictionXEditor.setText(st.nextToken());
		}
		
		public String getValue() {
			return predictionTypeChoice.getSelectedIndex() + " " + predictionXEditor.getText();
		}
		
		public void clearEditor(JPanel valuePanel) {
			valuePanel.remove(predictionTypeChoice);
			valuePanel.remove(predictionXEditor);
			predictionTypeChoice = null;
			predictionXEditor = null;
		}
		
		public boolean isValidValue() {
			try {
				@SuppressWarnings("unused")
				double predictionX = Double.parseDouble(predictionXEditor.getText());
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		}
	}
	
//-----------------------------------------------------------------------------
	
	private GridBagConstraints extremeTypeConstraint = new GridBagConstraints(
																										0, 0,														//	gridx & gridy
																										1, 1,														//	gridwidth & gridheight
																										0, 0,														//	weightx & weighty,
																										GridBagConstraints.NORTH,				//	anchor
																										GridBagConstraints.NONE,				//	fill
																										new Insets(0, 0, 0, 0),					//	insets
																										5, 0);													//	ipadx & ipady
	
	private GridBagConstraints predictionXConstraint = new GridBagConstraints(
																										1, 0,														//	gridx & gridy
																										1, 1,														//	gridwidth & gridheight
																										0, 0,														//	weightx & weighty,
																										GridBagConstraints.NORTH,				//	anchor
																										GridBagConstraints.NONE,				//	fill
																										new Insets(0, 0, 0, 0),					//	insets
																										5, 0);													//	ipadx & ipady
	
			
	private JTextField lowInterpolationX, highInterpolationX, lowExtrapolationX, highExtrapolationX;
	
	public PredictionVariableEditor(DomVariation variation, VariableType[] validParams, boolean hasIndex) {
		super(variation, validParams, hasIndex);
	}
	
	protected String getBaseType() {
		return "predictionType";
	}
	
	public void initialiseValuePanel(JPanel valuePanel) {
		valuePanel.setLayout(new GridBagLayout());
		
		extremeTypeConstraint.gridy = 0;
		JLabel minLabel = new JLabel("Problem type", JLabel.CENTER);
		valuePanel.add(minLabel, extremeTypeConstraint);
		extremeTypeConstraint.gridy ++;
		
		predictionXConstraint.gridy = 0;
		JLabel maxLabel = new JLabel("Prediction X", JLabel.CENTER);
		valuePanel.add(maxLabel, predictionXConstraint);
		predictionXConstraint.gridy ++;
	}
	
	public CoreValueEditor addOneValueEditor(JPanel valuePanel) {
		CoreValueEditor valueEditor = new PredictionValueEditor(valuePanel, extremeTypeConstraint, predictionXConstraint);
		extremeTypeConstraint.gridy ++;
		predictionXConstraint.gridy ++;
		
		return valueEditor;
	}
	
	public void removeLastValueEditor(JPanel valuePanel, CoreValueEditor valueEditor) {
		valueEditor.clearEditor(valuePanel);
		extremeTypeConstraint.gridy --;
		predictionXConstraint.gridy --;
	}
	
	public void createRandomValuePanel(JPanel valuePanel) {
		valuePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 0));
		
			JPanel interpPanel = new JPanel();
			interpPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			interpPanel.setOpaque(false);
			
				lowInterpolationX = variation.createMonitoredTextField("", 7);
				JLabel lowInterpolationLabel = new JLabel("Interpolate at X between", JLabel.LEFT);
				lowInterpolationLabel.setLabelFor(lowInterpolationX);
			interpPanel.add(lowInterpolationLabel);
			interpPanel.add(lowInterpolationX);
			
				highInterpolationX = variation.createMonitoredTextField("", 7);
				JLabel highInterpolationLabel = new JLabel("and", JLabel.LEFT);
				highInterpolationLabel.setLabelFor(highInterpolationX);
			interpPanel.add(highInterpolationLabel);
			interpPanel.add(highInterpolationX);
			
		valuePanel.add(interpPanel);
		
			JPanel extrapPanel = new JPanel();
			extrapPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
			extrapPanel.setOpaque(false);
			
				lowExtrapolationX = variation.createMonitoredTextField("", 7);
				JLabel lowExtrapolationLabel = new JLabel("Extrapolate at X between", JLabel.LEFT);
				lowExtrapolationLabel.setLabelFor(lowExtrapolationX);
			extrapPanel.add(lowExtrapolationLabel);
			extrapPanel.add(lowExtrapolationX);
			
				highExtrapolationX = variation.createMonitoredTextField("", 7);
				JLabel highExtrapolationLabel = new JLabel("and", JLabel.LEFT);
				highExtrapolationLabel.setLabelFor(highExtrapolationX);
			extrapPanel.add(highExtrapolationLabel);
			extrapPanel.add(highExtrapolationX);
			
		valuePanel.add(extrapPanel);
	}
	
	public boolean hasRandomValue() {
		return true;
	}
	
	public void initialiseRandomValue(String initialValue) {
		StringTokenizer st = new StringTokenizer(initialValue, ",:");
		lowInterpolationX.setText(st.nextToken());
		highInterpolationX.setText(st.nextToken());
		lowExtrapolationX.setText(st.nextToken());
		highExtrapolationX.setText(st.nextToken());
	}
	
	public String getRandomValue() {
		return "(" + lowInterpolationX.getText() + ":" + highInterpolationX.getText() + ","
														+ lowExtrapolationX.getText() + ":" + highExtrapolationX.getText() + ")";
	}
	
	protected boolean isValidRandomValue() {
		try {
			double lowInterp = Double.parseDouble(lowInterpolationX.getText());
			double highInterp = Double.parseDouble(highInterpolationX.getText());
			double lowExtrap = Double.parseDouble(lowExtrapolationX.getText());
			double highExtrap = Double.parseDouble(highExtrapolationX.getText());
			return lowInterp <= highInterp && lowExtrap <= highExtrap;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	public char arrayDelimiter() {
		return '*';
	}
}
