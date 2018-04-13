package cast.index;

import java.awt.*;
import java.io.*;

import javax.swing.*;

import cast.utils.*;


public class VersionCounter extends JFrame {
	public static final Color kBackgroundColor = new Color(0x666666);
	
	static public File createFileReference(String directory, String file) {
		return new File("../../CAST/core/" + directory, file);
//		return new File("/Users/dstirlin/Documents/CAST/CAST/core/" + directory, file);
	}
	
	static final private String[] folderList = {
		"context",
		"structures",
		"variation",
		
		"valueDisplay",
		"displayInterp",
		"density",
		"boxPlot",
		"centerSpread",
		"moreVariation",
		"percentile",
		"transform",
		"counts",
		
		"scatterplot",
		"relationship",
		"correlation",
		"leastSqrs",
		"curvature",
		
		"multivariate",
		"regnGroups",
		"lurking",
		
		"timePlot",
		"smoothing",
		"trend",
		"cyclic",
		"seasonal",
		"multiplicative",
		"timePatterns",
		"indexNos",
		
		"freqTable",
		"univarCat",
		"groupedCat",
		"bivarCat",
		"tableDisplay",
		"logistic",
		
		"modelIntro",
		"popSamp",
		"infPopn",
		"probDensity",
		"probSim",
		"randomMean",
		"normalDistn",
		"randomPropn",
		"sampPractice",
		"controlChart",
		
		"causal",
		"designIntro",
		"pairBlock",
		"designTwo",
		"designPrelim",
		
		"estIntro",
		"seMean",
		"ciMean",
		"estPropn",
		"estOther",
		"ciExtra",
		
		"testIntro",
		"testPropn",
		"testMean",
		"decision",
		"testPValue",
		
		"twoGroupModel",
		"sumDiff",
		"twoGroupInf",
		"twoGroupPropn",
		"multiGroup",
		"testPaired",
		"randBlock",
		
		"regnModel",
		"regnEst",
		"regnTest",
		"regnPred",
		"regnProblem",
		"diagnostics",
		
		"prob",
		"indep",
		
		"oneFactorIntro",
		"oneFactor",
		"oneFactorAnova",
		"factorial",
		"twoFactorModel",
		"twoFactorAnova",
		"factorInteract",
		"covariates",
		"factorialTwo",
		"twoFactor",
		"twoFactorNum"
	};
	
	static final private String[] student_suffix = {"", "_b", "_c", "_a", "_m", "_o", "_i", "_z"};
	static final private String[] student_name = {"General", "Biometric", "Business", "African", "Climatic", "Official Stats", "Industrial", "Other"};
	
	static final private String[] lecturer_suffix = {"", "_b", "_c", "_z"};
	static final private String[] lecturer_name = {"General", "Biometric", "Business", "Other"};
	
	static final private String[] exercise_suffix = {"", "_b", "_c"};
	static final private String[] exercise_name = {"General", "Biometric", "Business"};
	
	private Button doBuild;
	private Label finishedLabel;
	
	public VersionCounter() {
		setLayout(new BorderLayout(0, 10));
		
		setBackground(kBackgroundColor);
		
			Panel buttonPanel = new Panel();
			buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
				doBuild = new Button("Count page versions");
			buttonPanel.add(doBuild);
		add("North", buttonPanel);
		
			Panel messagePanel = new Panel();
			messagePanel.setLayout(new FixedSizeLayout(200, 40));
				finishedLabel = new Label("", Label.LEFT);
			messagePanel.add(finishedLabel);
		add("Center", messagePanel);
	}
	
	private void countVersions() {
		finishedLabel.setText("Checking folders...");
		File outputFile = createFileReference("structure", "pageVersions.js");
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
			PrintWriter outputWriter = new PrintWriter(bw);
			
			printHeader("student", student_suffix, student_name, outputWriter);
			for (int i=0 ; i<folderList.length ; i++)
				countFolderVersions(folderList[i], "", "student", student_suffix, outputWriter);
			outputWriter.println("");
			
			printHeader("lecturer", lecturer_suffix, lecturer_name, outputWriter);
			for (int i=0 ; i<folderList.length ; i++)
				countFolderVersions(folderList[i], "l_", "lecturer", lecturer_suffix, outputWriter);
			outputWriter.println("");
			
			printHeader("exercises", exercise_suffix, exercise_name, outputWriter);
			for (int i=0 ; i<folderList.length ; i++)
				countFolderVersions(folderList[i], "e_", "exercises", exercise_suffix, outputWriter);

			outputWriter.println("\nfunction getSuffixList(prefixIndex) {");
			outputWriter.println("  if (prefixIndex == 0) return student_suffix;");
			outputWriter.println("  else if (prefixIndex == 1) return lecturer_suffix;");
			outputWriter.println("  else return exercises_suffix;");
			outputWriter.println("}");

			outputWriter.println("\nfunction getNames(prefixIndex) {");
			outputWriter.println("  if (prefixIndex == 0) return student_name;");
			outputWriter.println("  else if (prefixIndex == 1) return lecturer_name;");
			outputWriter.println("  else return exercises_name;");
			outputWriter.println("}");

			outputWriter.println("\nfunction getPageFlags(prefixIndex) {");
			outputWriter.println("  if (prefixIndex == 0) return student_versions;");
			outputWriter.println("  else if (prefixIndex == 1) return lecturer_versions;");
			outputWriter.println("  else return exercises_versions;");
			outputWriter.println("}");
			
			outputWriter.flush();
			outputWriter.close();
			
		} catch (IOException e) {
			System.err.println(e.toString());
		}
		finishedLabel.setText("Finished");
	}
	
	private void printHeader(String studentLecturer, String[] suffix, String[] name,
																				PrintWriter outputWriter) {
		String suffixList = "";
		String nameList = "";
//		String restrictedList = "";
		for (int i=0 ; i<suffix.length ; i++) {
			if (i > 0) {
				suffixList += ", ";
				nameList += ", ";
			}
			suffixList += "\"" + suffix[i] + "\"";
			nameList += "\"" + name[i] + "\"";
		}
		
		outputWriter.println("var " + studentLecturer + "_suffix = new Array(" + suffixList + ");");
		outputWriter.println("var " + studentLecturer + "_name = new Array(" + nameList + ");");
		
		outputWriter.println("\nvar " + studentLecturer + "_versions = new Array();");
	}
	
	private void countFolderVersions(String name, String filePrefix, String studentLecturer,
																						String[] suffix, PrintWriter outputWriter) {
		outputWriter.print(studentLecturer + "_versions[\"H" + name + "\"] = new Array(");
		boolean finished = false;
		for (int i=1 ; !finished ; i++) {
			int fileFlags = 0;
			for (int j=0 ; j<suffix.length ; j++) {
				File f = createFileReference("H" + name, filePrefix + name + suffix[j] + i + ".html");
//				System.out.println("file: " + f);
				if (f.exists())
					fileFlags |= (1 << j);
			}
			if (fileFlags == 0)
				finished = true;
			else {
				if (i > 1)
					outputWriter.print(", ");
				outputWriter.print(fileFlags);
			}
		}
		outputWriter.println(");");
	}
	
	@SuppressWarnings("deprecation")
	public boolean action(Event evt, Object what) {
		if (evt.target == doBuild) {
			countVersions();
			return true;
		}
		return false;
	}
}
