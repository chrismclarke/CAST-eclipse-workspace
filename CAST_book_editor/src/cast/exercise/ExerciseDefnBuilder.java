package cast.exercise;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
//import java.nio.channels.*;
import java.util.*;

import javax.swing.*;

import org.w3c.dom.*;

import cast.utils.*;


public class ExerciseDefnBuilder extends JFrame {
	static final private Color kBackgroundColor = new Color(0xeeeeff);
	static final public int FOR_EXERCISE = 0;
	static final public int FOR_TEST = 1;
	
	static public void processTopic(DomTopic topic, Hashtable customExercises, JLabel finishedLabel, File dir, int outputType) {
		String topicCoreName = topic.getTopicName();
		File outputFile = new File(dir, topicCoreName + ".js");
		
		try {
//			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile),"UTF8"));
//			PrintWriter outputWriter = new PrintWriter(bw);
			PrintWriter outputWriter = FileFinder.createUTF8Writer(outputFile);
			
			finishedLabel.setText("Checking topic " + topicCoreName + " ...");
			
			if (outputType == FOR_TEST) {
				outputWriter.println("exercises = null;");
				outputWriter.println("currentExercise = null;\n");
			}
			
			int nExercises = topic.noOfExercises();
			for (int i=0 ; i<nExercises ; i++) {
				Vector customVariations = null;
				if (customExercises != null) {
					String exerciseName = topic.getExercise(i).getName();
					customVariations = (Vector)customExercises.get(exerciseName);
				}
				
				processExercise(topic.getExercise(i).getDomElement(), customVariations, outputWriter);
			}
			
			if (outputType == FOR_TEST)
				outputWriter.println("\nexercisesLoaded();");
			
			outputWriter.flush();
			outputWriter.close();
			
		} catch (IOException e) {
			System.err.println(e.toString());
		}
		finishedLabel.setText("Finished");
	}
	
	static private void processExercise(Element exerciseElement, Vector customVariations, PrintWriter outputWriter) {
		String applet = XmlHelper.getUniqueTagAsString(exerciseElement, "applet");
		int width = XmlHelper.getUniqueTagAsInt(exerciseElement, "width");
		int height = XmlHelper.getUniqueTagAsInt(exerciseElement, "height");
		
		outputWriter.println("startExercise(\"" + applet + "\", " + width + ", " + height + ");");
		
		NodeList nl = exerciseElement.getElementsByTagName("coreParam");
		for (int i=0 ; i<nl.getLength() ; i++) {
			Element paramElement = (Element)nl.item(i);
			processCoreParam(paramElement, outputWriter);
		}
		
		outputWriter.println("");
		
		nl = exerciseElement.getElementsByTagName("variation");
		for (int i=0 ; i<nl.getLength() ; i++) {
			Element variationElement = (Element)nl.item(i);
			processVariation(variationElement, applet, outputWriter);
		}
		
		if (customVariations != null)
			for (int i=0 ; i<customVariations.size() ; i++) {
				DomVariation variation = (DomVariation)customVariations.elementAt(i);
				processVariation(variation.getDomElement(), applet, outputWriter);
			}
		
		outputWriter.println("\n//----------------------------------------------\n");
	}
	
	static private void processCoreParam(Element paramElement, PrintWriter outputWriter) {
		String paramName = paramElement.getAttribute("name");
		String param = paramElement.getFirstChild().getNodeValue();
		
		outputWriter.println("addCoreParam(\"" + paramName + "\", \"" + param + "\");");
	}
	
	static private void processVariation(Element variationElement, String appletName, PrintWriter outputWriter) {
		String shortName = XmlHelper.getUniqueTagAsString(variationElement, "shortName");
		if (shortName == null)
			throw new RuntimeException("Error: Variation for \"" + appletName + "\" has no <shortName> tag");
		String question = ExerciseXmlHelper.quoteString(XmlHelper.getUniqueTagAsString(variationElement, "question"));
							//	allows for null question when there is global <question> tag
		String qnParam = ExerciseXmlHelper.quoteString(XmlHelper.getUniqueTagAsString(variationElement, "qnParam"));
							//	allows for null qnParam when there is global <qnParam> tag
		String qnExtra = ExerciseXmlHelper.quoteString(getEndings(variationElement));
							//	allows for null qnExtra
		int height = XmlHelper.getUniqueTagAsInt(variationElement, "height");
							//	zero if no custom height
		
		outputWriter.print("addVariation(\"" + shortName + "\", " + question + ", " + qnParam + ", " + qnExtra + ", " + height + ");\n");
	}
	
	static private String getEndings(Element variationElement) {
		String allEndings = null;
		NodeList nl = variationElement.getElementsByTagName("ending");
		for (int i=0 ; i<nl.getLength() ; i++) {
			Element endingElement = (Element)nl.item(i);
			String ending = XmlHelper.getTagInterior(endingElement);
			if (allEndings == null)
				allEndings = ending;
			else
				allEndings += ("|||" + ending);
		}
		return allEndings;
	}
	
/*
	static private void copyFile(final File sourceFile, final File destFile) throws IOException {
		if(!destFile.exists())
			destFile.createNewFile();

		FileChannel source = null;
		FileChannel destination = null;

		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		}
		finally {
			if(source != null)
				source.close();
			if(destination != null)
				destination.close();
		}
	}
*/
	
	
//-----------------------------------------------------------------------------------
	
	private JButton doBuildExercises;
	private JLabel finishedLabel;
	
	public ExerciseDefnBuilder(final DomTopic[] topics, final DomCustomVariations customVariations,
																															final File exerciseXmlDir, final File coreDir) {
		super("Process exercises");
		
		setLayout(new BorderLayout(0, 10));
		setBackground(kBackgroundColor);
		
			JPanel messagePanel = new JPanel();
			messagePanel.setLayout(new FixedSizeLayout(200, 40));
				finishedLabel = new JLabel("", Label.LEFT);
			messagePanel.add(finishedLabel);
			
		add("Center", messagePanel);
		
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_TOP, 7));
			
				final JCheckBox useCustomCheck = new JCheckBox("Use custom variations");
			buttonPanel.add(useCustomCheck);
				
				doBuildExercises = new JButton("Create Javascript definitions of exercises");
				doBuildExercises.addActionListener(new ActionListener() {
																public void actionPerformed(ActionEvent e) {
																	File exerciseDir = new File(coreDir, "exercises");
																	for (int i=0 ; i<topics.length ; i++) {
																		Hashtable customExercises = null;
																		if (useCustomCheck.isSelected()) {
																			String topicName = topics[i].getTopicName();
																			customExercises = customVariations.getExercises(topicName);
																		}
																		
																		processTopic(topics[i], customExercises, finishedLabel, exerciseDir, FOR_EXERCISE);
																	}
																}
														});
			buttonPanel.add(doBuildExercises);
			
		add("North", buttonPanel);
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
}
