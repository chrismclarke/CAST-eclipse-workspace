package cast.exercise;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;

import org.w3c.dom.*;

import cast.utils.*;


public class TestDefnBuilder extends JFrame {
	static final private Color kBackgroundColor = new Color(0xeeeeff);
	
	private JButton doBuildTests;
	private JLabel finishedLabel;
	
	public TestDefnBuilder(final DomTopic[] topics, final DomCustomVariations customVariations, final File pluginDir) {
		super("Process Tests");
		
		setLayout(new BorderLayout(0, 10));
		setBackground(kBackgroundColor);
		
			Panel messagePanel = new Panel();
			messagePanel.setLayout(new FixedSizeLayout(200, 40));
				finishedLabel = new JLabel("", Label.LEFT);
			messagePanel.add(finishedLabel);
			
		add("Center", messagePanel);
		
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 7));
				
					final JCheckBox useCustomCheck = new JCheckBox("Use custom variations");
				buttonPanel.add(useCustomCheck);
					
				doBuildTests = new JButton("Create Javascript definitions of tests");
				doBuildTests.addActionListener(new ActionListener() {
																			public void actionPerformed(ActionEvent e) {
																				File exerciseDir = new File(pluginDir, "questionData");
																				for (int i=0 ; i<topics.length ; i++) {
																					Hashtable customExercises = null;
																					if (useCustomCheck.isSelected()) {
																						String topicName = topics[i].getTopicName();
																						customExercises = customVariations.getExercises(topicName);
																					}
																					
																					ExerciseDefnBuilder.processTopic(topics[i], customExercises, finishedLabel,
																																								exerciseDir, ExerciseDefnBuilder.FOR_TEST);
																				}
																				
																				processAllTests(topics, exerciseDir);
																			}
																	});
			buttonPanel.add(doBuildTests);
		add("North", buttonPanel);
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

//-----------------------------------------------------------------------------------
	
	private void processAllTests(DomTopic[] topics, File exerciseDir) {
		
		File outputFile = new File(exerciseDir, "availableTests.js");
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			PrintWriter outputWriter = new PrintWriter(bw);
			
			for (int i=0 ; i<topics.length ; i++)
				processTopic_test(topics[i], outputWriter);
			
			outputWriter.println("\ntopicsLoaded();");

			outputWriter.flush();
			outputWriter.close();
			
		} catch (IOException e) {
			System.err.println(e.toString());
		}
		finishedLabel.setText("Finished");
	}
	
	private void processTopic_test(DomTopic topic, PrintWriter outputWriter) {
		String shortTopicName = ExerciseXmlHelper.quoteString(topic.getTopicName());
		String longTopicName = ExerciseXmlHelper.quoteString(topic.getLongName());
		
		outputWriter.println("startTopic(" + shortTopicName + ", " + longTopicName + ");");
		outputWriter.println("\n//----------------------------------------------\n");
		
		int nExercises = topic.noOfExercises();
		for (int i=0 ; i<nExercises ; i++) {
			DomExercise exercise = topic.getExercise(i);
			processExercise_test(exercise, outputWriter);
		}
	}
	
	private void processExercise_test(DomExercise exercise, PrintWriter outputWriter) {
		Element exerciseElement = exercise.getDomElement();
		String applet = ExerciseXmlHelper.quoteString(XmlHelper.getUniqueTagAsString(exerciseElement, "applet"));
		String description = ExerciseXmlHelper.quoteString(XmlHelper.getUniqueTagAsString(exerciseElement, "description"));
		String comment = ExerciseXmlHelper.quoteString(XmlHelper.getUniqueTagAsString(exerciseElement, "comment"));
		
		outputWriter.println("startSummaryExercise(" + description + ", " + comment + ", " + applet + ");");
		
		NodeList nl = exerciseElement.getElementsByTagName("variation");
		for (int i=0 ; i<nl.getLength() ; i++) {
			Element variationElement = (Element)nl.item(i);
			processVariation_test(variationElement, outputWriter);
		}
		
		outputWriter.println("");
		
		nl = exerciseElement.getElementsByTagName("option");
		for (int i=0 ; i<nl.getLength() ; i++) {
			Element paramElement = (Element)nl.item(i);
			processOption_test(paramElement, outputWriter);
		}
		
		outputWriter.println("\n//----------------------------------------------\n");
	}
	
	private void processVariation_test(Element variationElement, PrintWriter outputWriter) {
		String shortName = XmlHelper.getUniqueTagAsString(variationElement, "shortName");
		String longName = XmlHelper.getUniqueTagAsString(variationElement, "longName");
		
		outputWriter.print("addSummaryVariation(\"" + shortName + "\", \"" + longName + "\");\n");
	}
	
	private void processOption_test(Element optionElement, PrintWriter outputWriter) {
		String option = optionElement.getAttribute("name");
		String description = optionElement.getFirstChild().getNodeValue();
		
		outputWriter.println("addSummaryOption(\"" + option + "\", \"" + description + "\");");
	}
}
