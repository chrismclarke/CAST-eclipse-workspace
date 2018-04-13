package cast.variationEditor;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import org.w3c.dom.*;

import cast.utils.*;
import cast.core.*;
import cast.exercise.*;

public class EndingsListPanel extends JPanel {
	static final private Color kEndingTitleBackground = new Color(0x330033);
	static final private Color kEndingBackground = new Color(0xFFFFEE);
			
	private GridBagConstraints endingConstraint = new GridBagConstraints(
																							0, 0,														//	gridx & gridy
																							1, 1,														//	gridwidth & gridheight
																							1, 0,														//	weightx & weighty,
																							GridBagConstraints.NORTHWEST,		//	anchor
																							GridBagConstraints.BOTH,				//	fill
																							new Insets(0, 5, 0, 10),				//	insets
																							0, 10);													//	ipadx & ipady
	private GridBagConstraints buttonConstraint = new GridBagConstraints(
																							1, 0,														//	gridx & gridy
																							1, 1,														//	gridwidth & gridheight
																							0, 0,														//	weightx & weighty,
																							GridBagConstraints.CENTER,			//	anchor
																							GridBagConstraints.NONE,				//	fill
																							new Insets(0, 0, 0, 0),					//	insets
																							0, 0);													//	ipadx & ipady	
	private GridBagConstraints lineConstraint = new GridBagConstraints(
																							0, 1,														//	gridx & gridy
																							2, 1,														//	gridwidth & gridheight
																							0, 0,														//	weightx & weighty,
																							GridBagConstraints.CENTER,			//	anchor
																							GridBagConstraints.BOTH,				//	fill
																							new Insets(0, 0, 0, 0),					//	insets
																							0, 0);													//	ipadx & ipady	
	private GridBagConstraints fillerConstraint = new GridBagConstraints(
																							0, 0,														//	gridx & gridy
																							2, 1,														//	gridwidth & gridheight
																							1, 1,														//	weightx & weighty,
																							GridBagConstraints.CENTER,			//	anchor
																							GridBagConstraints.BOTH,	//	fill
																							new Insets(0, 0, 0, 0),					//	insets
																							0, 0);													//	ipadx & ipady
	
	private DomVariation variation;
	private VariationEditor editor;
	
	private Vector endings;				//	Vector of QuestionAndParam
	private Vector endingPanels = null;				//	Vector of EndingPanel
	
	private JPanel endingsPanel, innerEndingsPanel;
	
	public EndingsListPanel(DomVariation variation, Vector endings, VariationEditor editor) {
		this.variation = variation;
		this.endings = endings;
		this.editor = editor;
		
		setLayout(new BorderLayout(0, 0));
			Border panelBorder = BorderFactory.createMatteBorder(0, 4, 2, 2, kEndingTitleBackground);
		setBorder(panelBorder);
		
			JPanel titlePanel = new JPanel();
			titlePanel.setBackground(kEndingTitleBackground);
			titlePanel.setLayout(new BorderLayout(0, 0));
		
				JLabel title = new JLabel("Question endings", JLabel.CENTER);
				title.setOpaque(false);
				title.setForeground(Color.white);
				title.setFont(new Font("SansSerif", Font.BOLD, 16));
					Border insetBorder = BorderFactory.createEmptyBorder(4, 0, 4, 0);
				title.setBorder(insetBorder);
				
			titlePanel.add("Center", title);
			
			if (!variation.isCoreVariation() || Options.hasMultipleCollections) {
				JPanel rightPanel = new JPanel();
				rightPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
				rightPanel.setOpaque(false);
			
					JButton newEndingButton = new JButton("Another ending");
					newEndingButton.addActionListener(new ActionListener() {
																				public void actionPerformed(ActionEvent e) {
																					createNewEnding();
																					resetPanel();
																				}
																		});
					
				rightPanel.add(newEndingButton);
				
				titlePanel.add("East", rightPanel);
			}
			
		add("North", titlePanel);
			
			endingsPanel = new JPanel();
			endingsPanel.setLayout(new BorderLayout(0, 0));
			
				innerEndingsPanel = allEndings();
			endingsPanel.add("Center", innerEndingsPanel);
			
			JScrollPane scrollPane = new JScrollPane(endingsPanel);
			Border emptyBorder = BorderFactory.createEmptyBorder(0, 0, 0, 0);
			scrollPane.setBorder(emptyBorder);
			
		add("Center", scrollPane);
	}
	
	private JPanel allEndings() {
		JPanel thePanel = new JPanel();
		thePanel.setBackground(kEndingBackground);
		thePanel.setLayout(new GridBagLayout());
		
		endingConstraint.gridy = 0;
		buttonConstraint.gridy = 0;
		lineConstraint.gridy = 1;
		
		endingPanels = new Vector();
		int nEndings = variation.getNoOfEndings();
		for (int i=0 ; i<nEndings ; i++) {
				QuestionAndParams endingAndParams = (QuestionAndParams)endings.elementAt(i);
				endingAndParams.clearParamsFromStartOfQuestion();
				VariableType[] endingParams = variation.getExercise().getVariableTypes(DomExercise.ENDING_PARAMS_ONLY);
				VariableType[] allParams = variation.getExercise().getVariableTypes(DomExercise.ALL_PARAMS);
				final DomEnding ending = variation.getEnding(i);
				EndingPanel endingPanel = new EndingPanel(endingAndParams, endingParams, allParams, ending);
				endingPanels.add(endingPanel);
			thePanel.add(endingPanel, endingConstraint);
			
				JButton deleteButton = new JButton("Delete");
				deleteButton.addActionListener(new ActionListener() {
																				public void actionPerformed(ActionEvent e) {
																					deleteEnding(ending);
																					resetPanel();
																				}
																		});
			thePanel.add(deleteButton, buttonConstraint);
			
				JPanel linePanel = new JPanel() {
																public Dimension getMinimumSize() { return new Dimension(10, 4); }
																public Dimension getPreferredSize() { return getMinimumSize(); }
															};
				linePanel.setBackground(kEndingTitleBackground);
			thePanel.add(linePanel, lineConstraint);
			
			endingConstraint.gridy += 2;
			buttonConstraint.gridy += 2;
			lineConstraint.gridy += 2;
		}
		
			fillerConstraint.gridy = 2 * nEndings;
			JPanel fillerPanel = new JPanel();
			fillerPanel.setOpaque(false);
		thePanel.add(fillerPanel, fillerConstraint);
		return thePanel;
	}
	
	private void deleteEnding(DomEnding ending) {
		variation.deleteEnding(ending);
		variation.setDomChanged();
	}
	
	public String[] getEndingStrings() throws ParamValueException {
		String[] endingParams = null;
		if (endingPanels != null) {
			int nEndings = endingPanels.size();
			endingParams = new String[nEndings];
			VariableType[] allValidParams = variation.getExercise().getVariableTypes(DomExercise.ALL_PARAMS);
			for (int i=0 ; i<nEndings ; i++) {
				EndingPanel endingPanel = (EndingPanel)endingPanels.elementAt(i);
				QuestionAndParams endingQnAndParams = (QuestionAndParams)endings.elementAt(i);
				endingPanel.updateEnding(endingQnAndParams);
				DomConverter endingConverter = new DomConverter(endingQnAndParams, allValidParams);
				String paramString = endingConverter.getQuestionParams();
				String questionString = endingConverter.getQuestionText();
				if (paramString.length() > 0)
					endingParams[i] = paramString + "#" + questionString;
				else
					endingParams[i] = questionString;
			}
		}
		return endingParams;
	}
	
	public String getAppletTag() throws ParamValueException {
		String endingTag = "<param name='questionExtra0' value='";
		
		String[] endingText = getEndingStrings();
		for (int i=0 ; i<endingText.length ; i++) {
			String end = endingText[i];
			end = end.replaceAll("\\\\\\\\n", "\\\\n");		//	to replace \\n by \n in question
			
			if (i > 0 || end.startsWith(" "))
				endingTag += "|||";
			
			endingTag += end;
		}
		
		endingTag += "'>";
		return endingTag;
	}
	
/*
	public String getAppletTag() {
		String endingTag = "<param name='questionExtra0' value='";
		VariableType[] allValidParams = variation.getExercise().getVariableTypes(DomExercise.ALL_PARAMS);
		
		for (int i=0 ; i<endings.size() ; i++) {
			QuestionAndParams endingQnParams = (QuestionAndParams)endings.elementAt(i);
			DomConverter endingConverter = new DomConverter(endingQnParams, allValidParams);
			
			String rawEndingText = endingConverter.getQuestionText();
			rawEndingText = rawEndingText.replaceAll("\\\\\\\\n", "\\\\n");		//	to replace \\n by \n in question
			String endingParamString = endingConverter.getQuestionParams();
			
			if (i > 0 || (endingParamString.length() == 0 && endingParamString.startsWith(" ")))
				endingTag += "|||";
			
			endingTag += endingParamString;
			
			if (endingParamString.length() > 0)
				endingTag += "#";
			
			endingTag += rawEndingText;
		}
		
		endingTag += "'>";
		return endingTag;
	}
*/
	
	private void createNewEnding() {
		DomExercise exercise = variation.getExercise();
//		DomTopic topic = exercise.getTopic();
//		Document topicDocument = topic.getDocument();
		
		Element exerciseElement = exercise.getDomElement();
		Element template = XmlHelper.getUniqueTag(exerciseElement, "template");
		Element endingTemplate = XmlHelper.getUniqueTag(template, "ending");
		
		DomEnding newEnding = new DomEnding(endingTemplate, variation, DomEnding.CREATE_COPY);			//	creates copy belonging to topic document
		
		variation.addNewEnding(newEnding);
		
		DomConverter.inputEndingsFromDom(variation, editor);
		endings = editor.getEndings();
	}
	
	private void resetPanel() {
		endingsPanel.remove(innerEndingsPanel);
		innerEndingsPanel = allEndings();
		endingsPanel.add("Center", innerEndingsPanel);
		innerEndingsPanel.revalidate();
		innerEndingsPanel.repaint();
	}
}