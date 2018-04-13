package cast.variationEditor;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.*;

import cast.exercise.*;

public class EndingPanel extends JPanel {
//	private DomEnding ending;
	
	private QuestionPanel endingField;
	private ParameterListPanel paramListPanel;
	
	public EndingPanel(QuestionAndParams qnAndParams, VariableType[] endingParams, VariableType[] allParams,
																																													DomEnding ending) {
//		this.ending = ending;
		
		setLayout(new BorderLayout(0, 4));
		setOpaque(false);
		
			Border insetBorder = BorderFactory.createEmptyBorder(4, 0, 2, 6);
			Border rightLineBorder = BorderFactory.createMatteBorder(0, 0, 0, 1, Color.gray);
			Border compound = BorderFactory.createCompoundBorder(rightLineBorder, insetBorder);
		setBorder(compound);
		
				endingField = ending.getVariation().createMonitoredQuestionPanel(qnAndParams.getQuestion(), allParams);
			JScrollPane scrollArea = new JScrollPane(endingField);
			scrollArea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			scrollArea.setPreferredSize(new Dimension(40, 65));			//		sets height of scrollArea
			
		add("North", scrollArea);
		
			paramListPanel = new ParameterListPanel(qnAndParams.getParams(), endingParams, ending.getVariation());
		add("Center", paramListPanel);
	}
	
	public void updateEnding(QuestionAndParams qnAndParams) throws ParamValueException {
		paramListPanel.updateParams(qnAndParams.getParams());
		qnAndParams.setQuestion(endingField.getText());
	}
}