package cast.other;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

import javax.swing.*;
import javax.swing.border.Border;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import cast.utils.*;



public class GenerateRData extends JFrame {
	static final private String kPagePattern = "a(C|S|P)\\((.*?)\\);";
	static final private String kPageInfoPattern = "'(.*?)','(.*?)','(.*?)'";
	
	static final private String kTermPattern = "aT\\('(.*?)',(.*?)\\);\\s*(aSrc\\(\"(.*?)\"\\);)?";
	
	static final private String kDescriptionString = "Package: cast.#\n" + 
			"Title: Data sets for CAST e-book \"#\"\n" + 
			"Version: 1.0\n" + 
			"Authors@R: person(\"Doug\", \"Stirling\", email = \"d.stirling@massey.ac.nz\",\n" + 
			"                  role = c(\"aut\", \"cre\"))\n" + 
			"Description: This package contains the data sets used in the CAST e-book. \"#\"\n" + 
			"Depends: R (>= 3.1.0)\n" + 
			"License: GPL-3\n" + 
			"LazyData: true";
	
	static final private String kXmlStartString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
			"<!DOCTYPE allData SYSTEM \"../../../structure/bookXmlDefn.dtd\">\n" +
			"<allData>";
	
	static final private String kXmlEndString = "</allData>";

	static final private String kNamePattern = "name=\"varName\"\\s*value=\"([^\"]*)\"";
	static final private String kNamePattern2 = "name=\"([^\"]*)Name\"\\s*value=\"([^\"]*)\"";	
	static final private String kLabelPattern = "name=\"labelName\"\\s*value=\"([^\"]*)\"";

	private String bookName;
	private File bookDir;
	private DataReference[] theData;
	
	
	public GenerateRData(final String bookName, File coreDir) {
		super("Generate R data sets for \"" + bookName + "\"");
		this.bookName = bookName;
		bookDir = new File(coreDir, "bk/" + bookName);
		initialise(coreDir);
		
		readSelection(bookName);
		
		setLayout(new BorderLayout(0, 0));
		
			JPanel dataListPanel = new JPanel();
			dataListPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 0));
			
			for (int i=0 ; i<theData.length ; i++)
				dataListPanel.add(new DataPanel(theData[i]));
			
			JScrollPane scrollPane = new JScrollPane(dataListPanel);
		
		add("Center", scrollPane);
		
			JPanel bottomPanel = new JPanel();
			bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
			Border spacingBorder = BorderFactory.createMatteBorder(5, 0, 5, 0, Color.white);
			bottomPanel.setBorder(spacingBorder);
			bottomPanel.setBackground(Color.white);
			
				JButton saveButton = new JButton("Save selection");
				saveButton.addActionListener(new ActionListener() {
			    public void actionPerformed(ActionEvent e) {
			    	saveSelection(bookName);
			    }
				});
			bottomPanel.add(saveButton);
			
				JButton generateButton = new JButton("Generate R data");
				generateButton.addActionListener(new ActionListener() {
			    public void actionPerformed(ActionEvent e) {
			    	createRData(bookName);
			    }
				});
			bottomPanel.add(generateButton);
			
		add("South", bottomPanel);
	}
	
	private void initialise(File coreDir) {
		File indexFile = new File(bookDir, "book_dataSets.html");

		String html = HtmlHelper.getFileAsString(indexFile);
		html = html.replaceAll("#.#", "");

		Pattern pagePattern = Pattern.compile(kPagePattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher pageMatcher = pagePattern.matcher(html);
		int nPages = 0;
		while (pageMatcher.find())
			nPages ++;

		PageReference[] thePages = new PageReference[nPages];

		pageMatcher = pagePattern.matcher(html);
		int i = 0;
		while (pageMatcher.find()) {
			char pageType = pageMatcher.group(1).charAt(0);
			String pageInfo = XmlHelper.decodeHtml(pageMatcher.group(2), false);		//	decodes HTML entities like &amp;
			if (pageInfo != null)
				pageInfo = pageInfo.replaceAll("\\\\u03c3", "\u03c3");		//	decodes sigma
			if (pageType == 'C' || pageType == 'S')
				thePages[i++] = null;
			else if (pageType == 'P') {
				Pattern pageInfoPattern = Pattern.compile(kPageInfoPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
				Matcher pageInfoMatcher = pageInfoPattern.matcher(pageInfo);
				pageInfoMatcher.find();
				String pageName = pageInfoMatcher.group(1);
				pageName = pageName.replaceAll("\\\\'", "'");
				String pageDir = pageInfoMatcher.group(2);
				String pageFilePrefix = pageInfoMatcher.group(3);
				thePages[i++] = new PageReference(pageName, pageDir, pageFilePrefix, coreDir);
			}
		}

		Pattern termPattern = Pattern.compile(kTermPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher termMatcher = termPattern.matcher(html);
		int nTerms = 0;
		while (termMatcher.find())
			nTerms ++;

		theData = new DataReference[nTerms];

		termMatcher = termPattern.matcher(html);
		i = 0;
		while (termMatcher.find()) {
			String termName = termMatcher.group(1);
			String termIndexString = termMatcher.group(2);
			String source = null;
			if (termMatcher.group(3) != null) {
				source = termMatcher.group(4);
			}
			String[] termIndices = termIndexString.split(",");
			int nPagesWithApplets = 0;
			for (int j=0 ; j<termIndices.length ; j++)
				if (thePages[Integer.valueOf(termIndices[j])].noOfApplets() > 0)
					nPagesWithApplets ++;			//		find how many pages have applets with data
			
			PageReference[] pageReferences = new PageReference[nPagesWithApplets];		//		this may have length zero if there are no pages with data applets referencing term
			nPagesWithApplets = 0;
			for (int j=0 ; j<termIndices.length ; j++) {
				PageReference pageJ = thePages[Integer.valueOf(termIndices[j])];
				if (pageJ.noOfApplets() > 0)
					pageReferences[nPagesWithApplets ++] = pageJ;		//		only include pages with data applets
			}
			
			theData[i++] = new DataReference(termName, pageReferences, source);
		}
		
		thePages = null;		//		so garbage collector will deal with unused pages
	}
	
	private void saveSelection(String bookName) {
		File xmlDir = new File(bookDir, "xml");
		File saveFile = new File(xmlDir, "exportToRSettings.xml");
		try {
			PrintWriter writer = new PrintWriter(saveFile, "UTF-8");
			writer.print(kXmlStartString);
			
			for (int i=0 ; i<theData.length ; i++)
				if (theData[i].getBestPage() != null) {			//		don't write if there are no pages with actual data
					String dataSetString = "<dataset name=\"" + theData[i].getName() + "\"";
					dataSetString += " pagePrefix=\"" + theData[i].getBestPage().getFilePrefix() + "\"";
					dataSetString += " appletIndex=\"" + theData[i].getBestAppletInPage() + "\"";
					dataSetString += " selected=\"" + theData[i].isSelectedForExport() + "\" />\n";
					writer.print(dataSetString);
				}
			writer.print(kXmlEndString);
			writer.close();
		} catch (IOException e) {
			System.out.println("Could not save R output settings file for " + bookName);
		}
	}
	
	private void readSelection(String bookName) {
		File xmlDir = new File(bookDir, "xml");
		File settingsFile = new File(xmlDir, "exportToRSettings.xml");
		
		if (settingsFile.exists()) {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(true);
			try {
				DocumentBuilder db = dbf.newDocumentBuilder();
				db.setErrorHandler(new ErrorHandler() {
					public void warning(SAXParseException exception) throws SAXException {
					}
					public void error(SAXParseException exception) throws SAXException {
					}
					public void fatalError(SAXParseException exception) throws SAXException {
					}
				} );

				Document settingsDomDocument = db.parse(settingsFile);

				Element domElement = settingsDomDocument.getDocumentElement();
				
				NodeList dataNodes = domElement.getElementsByTagName("dataset");
				int nNodes = dataNodes.getLength();
				for (int i=0 ; i<nNodes ; i++) {
					Element e = (Element)dataNodes.item(i);
					String dataName = e.getAttribute("name");
					String pagePrefix = e.getAttribute("pagePrefix");
					int appletInPage = Integer.valueOf(e.getAttribute("appletIndex"));
					boolean isSelected = Boolean.parseBoolean(e.getAttribute("selected"));
					
					for (int j=0 ; j<theData.length ; j++)
						if (theData[j].getName().equals(dataName)) {
							DataReference d = theData[j];
							PageReference[] thePages = d.getPages();
							for (int k=0 ; k<thePages.length ; k++)
								if (thePages[k].getFilePrefix().equals(pagePrefix)) {
									PageReference p = thePages[k];
									if (appletInPage < p.noOfApplets()) {
										d.setBestApplet(p, appletInPage);
										d.selectForExport(isSelected);
									}
									break;
								}
							break;
						}
				}
			} catch(Exception e) {
				System.err.println("Cannot open settings file for " + bookName);
				e.printStackTrace();
			}
		}
	}
	
	private void createRData(String bookName) {
		JFileChooser chooser = new JFileChooser(); 
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setDialogTitle("Select folder for R Data package");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { 
			File mainDir = chooser.getSelectedFile();
			
			File packageDir = new File(mainDir, "cast." + bookName);
			if (!packageDir.exists())
				packageDir.mkdir();
			
			File descriptionFile = new File(packageDir, "DESCRIPTION");
			if (!descriptionFile.exists()) {
				String descriptionString = kDescriptionString.replaceAll("#", bookName);
				try {
					PrintWriter writer = new PrintWriter(descriptionFile, "UTF-8");
					writer.print(descriptionString);
					writer.close();
				} catch (IOException e) {
					System.out.println("Could not create DESCRIPTION file for " + bookName);
				}
			}
			
			File ignoreFile = new File(packageDir, ".Rbuildignore");
			if (!ignoreFile.exists()) {
				try {
					PrintWriter writer = new PrintWriter(ignoreFile, "UTF-8");
					writer.print("^data-raw$\n"
							+ "^buildScript\\.R$\n"
							+ "^README.md\\.md$");
					writer.close();
				} catch (IOException e) {
					System.out.println("Could not create ignore file for " + bookName);
				}
			}
			
			File buildFile = new File(packageDir, "buildScript.R");
			if (!buildFile.exists()) {
				try {
					PrintWriter writer = new PrintWriter(buildFile, "UTF-8");
					writer.print("files <- sort(dir(file.path(getwd(), 'data-raw/'), pattern=\".R$\", full.names = TRUE))\n" + 
							"invisible(lapply(files, source))");
					writer.close();
				} catch (IOException e) {
					System.out.println("Could not create build file for " + bookName);
				}
			}
			
			File readMeFile = new File(packageDir, "README.text");
			if (!readMeFile.exists()) {
				try {
					PrintWriter writer = new PrintWriter(readMeFile, "UTF-8");
					writer.print("To compile and install the package:\n"
							+ "   1. Use setwd() to change the working directory to the data package directory\n"
							+ "   2. Install the package devtools with install.packages(devtools)\n"
							+ "   3. Load the package devtools with library(devtools)\n"
							+ "   4. In R, use the command devtools::document() to create documentation files"
							+ "   5. Run the R script buildScript.R (to create the data directory)\n"
							+ "   6. In R, use the command devtools::build()");
					writer.close();
				} catch (IOException e) {
					System.out.println("Could not create README file for " + bookName);
				}
			}
			
			File outputDir = new File(packageDir, "data-raw");
			if (!outputDir.exists())
				outputDir.mkdir();
			
			File rDir = new File(packageDir, "R");
			if (!rDir.exists())
				rDir.mkdir();
			
			for (int i=0 ; i<theData.length ; i++)
				createDataset(theData[i], outputDir, rDir);
		}
	}
	
	private class NameType {
		String varName, dataName;
		String type;
		NameType(String varName, String dataName, String type) {
			this.varName = varName;
			this.dataName = dataName;
			this.type = type;
		}
	}
	
	private class DataDimensions {
		int nRows=0;
		Vector<NameType> varNames = new Vector<NameType>();
	}
	
	private void createDataset(DataReference data, File outputDir, File rDir) {
		PageReference bestPage = data.getBestPage();
		if (data.isSelectedForExport() && bestPage != null) {
			String appletString = bestPage.getApplet(data.getBestAppletInPage());
			String dataName = data.getName().replaceAll(" ", ".");
			String source = data.getSource();
			DataDimensions dimensions = new DataDimensions();

			File outputFile = new File(outputDir, dataName + ".R");
			try {
				PrintWriter writer = new PrintWriter(outputFile, "UTF-8");
				writer.print(dataName + "<-data.frame(");
				
				outputStandardVar(appletString, writer, dimensions);
				outputOtherVars(appletString, writer, dimensions);
				outputLabelVar(appletString, writer, dimensions);

				writer.println(")");
				if (source != null && source.length() > 0)
					writer.println("Hmisc::label(" + dataName + ")=\"Source: " + source + "\"\n");
				writer.println("devtools::use_data(" + dataName + ",overwrite=TRUE)");

				writer.close();
			} catch (IOException e) {
				System.out.println("Could not print R output file for " + dataName);
			}

			outputFile = new File(rDir, dataName + ".R");
			try {
				PrintWriter writer = new PrintWriter(outputFile, "UTF-8");
				writer.println("#' " + data.getName() + "\n" + 
						"#'");
				writer.println("#' A data set from the " + bookName + " CAST e-book" + "\n" + 
						"#'");
				writer.println("#' @docType data\n" + 
						"#'\n" + 
						"#' @usage data(" + dataName + ")\n" +
						"#'");
				writer.println("#' @format A data frame with " + dimensions.varNames.size() + " variables and " + dimensions.nRows + " rows:");
				writer.println("#' \\describe{");
				for (int i=0 ; i<dimensions.varNames.size() ; i++) {
					NameType varInfo = dimensions.varNames.elementAt(i);
					writer.println("#'  \\item{" + varInfo.varName + "}{" + varInfo.dataName + ": "+ varInfo.type + " variable}");
				}
				writer.println("#' }\n" + 
						"#'\n" + 
						"#' @keywords datasets\n" +
						"#'");
				if (source != null)
					writer.println("#' @source " + source + "\n" + 
							"#'");
				writer.println("\"" + dataName + "\"");

				writer.close();
			} catch (IOException e) {
				System.out.println("Could not print R description file for " + dataName);
			}
		}
	}

	private void outputStandardVar(String appletString, PrintWriter writer, DataDimensions dimensions) {
		Pattern namePattern = Pattern.compile(kNamePattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher nameMatcher = namePattern.matcher(appletString);
		if (nameMatcher.find()) {
			String varName = nameMatcher.group(1);
			
			boolean hasLabelVar = appletString.contains("<param name=\"labelName\"");
			if (hasLabelVar)
				outputVarParams(appletString, varName, "values", null, writer, dimensions);
			else
				outputVarParams(appletString, varName, "values", "labels", writer, dimensions);
		}
	}

	private void outputOtherVars(String appletString, PrintWriter writer, DataDimensions dimensions) {
		Pattern namePattern = Pattern.compile(kNamePattern2, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher nameMatcher = namePattern.matcher(appletString);
		while (nameMatcher.find()) {
			String prefix = nameMatcher.group(1);
			String varName = nameMatcher.group(2);
			if (prefix.endsWith("Var")) {
				prefix = prefix.substring(0, prefix.length() - 3);
				outputVarParams(appletString, varName, prefix + "Values", prefix + "Labels", writer, dimensions);
			}
			else
				outputVarParams(appletString, varName, prefix + "Values", prefix + "Labels", writer, dimensions);
		}
	}
	
	private void outputLabelVar(String appletString, PrintWriter writer, DataDimensions dimensions) {
		Pattern namePattern = Pattern.compile(kLabelPattern, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher nameMatcher = namePattern.matcher(appletString);
		if (nameMatcher.find()) {
			String varName = nameMatcher.group(1);
			outputVarParams(appletString, varName, null, "labels", writer, dimensions);
		}
	}
	
	private void outputVarParams(String appletString, String varName, String valuesParamName, String labelsParamName, PrintWriter writer, DataDimensions dimensions) {
		String values = null;
		if (valuesParamName != null) {
			String valuesPatternString = "name=\"" + valuesParamName + "\"\\s*value=\"([^\"]*)\"";
			Pattern valuesPattern = Pattern.compile(valuesPatternString, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
			Matcher valuesMatcher = valuesPattern.matcher(appletString);
			if (valuesMatcher.find())
				values = valuesMatcher.group(1);
		}
		
		String labels = null;
		if (labelsParamName != null) {
			String labelsPatternString = "name=\"" + labelsParamName + "\"\\s*value=\"([^\"]*)\"";
			Pattern labelsPattern = Pattern.compile(labelsPatternString, Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
			Matcher labelsMatcher = labelsPattern.matcher(appletString);
			if (labelsMatcher.find())
				labels = labelsMatcher.group(1);
		}
		
		if (values != null || labels !=null) {
			if (dimensions.varNames.size() > 0)
				writer.print(",");
			outputVar(varName, values, labels, writer, dimensions);
		}
	}
	
	private class DataValues {
		int nValues;
		String valueString;
		DataValues(int nValues, String valueString) {
			this.nValues = nValues;
			this.valueString = valueString;
		}
	}

	private void outputVar(String nameParam, String valueParam, String labelsParam, PrintWriter writer, DataDimensions dimensions) {
		String varName = nameParam.replaceAll("[ \\(\\)]", ".");
		varName = varName.replaceAll("\\.\\.", ".");
		varName = varName.replaceAll("\\.$", "");
		DataValues values = null;
		if (valueParam != null)
			values = formatValuesString(valueParam);

		if (labelsParam == null) {
			writer.print(varName + "=c(" + values.valueString + ")");
			dimensions.varNames.add(new NameType(varName, nameParam, "numerical"));
		}
		else {
			DataValues labels = formatLabels(labelsParam);
			if (valueParam == null)	 {	//		labels variable
				writer.print(varName + "=c(" + labels.valueString + ")");
				dimensions.varNames.add(new NameType(varName, nameParam, "character"));
			}
			else {
				writer.print(varName + "=factor(c(" + values.valueString + "),labels=c(" + labels.valueString + "))");
				dimensions.varNames.add(new NameType(varName, nameParam, "factor"));
			}
		}
		if (dimensions.nRows == 0)
			dimensions.nRows = values.nValues;
	}
	
	private DataValues formatValuesString(String s) {
		int noOfRows = 0;
		Pattern itemPattern = Pattern.compile("\\s*([\\d@]+)\\s*", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher itemMatcher = itemPattern.matcher(s);
		while (itemMatcher.find()) {
			String item = itemMatcher.group(1);
			int atIndex = item.indexOf('@');
			if (atIndex > 0)
				noOfRows += Integer.parseInt(item.substring(0, atIndex));
			else noOfRows ++;
		}
		
		s = s.replaceAll("(\\d*)@(\\d*)", "rep($2,$1)");
		s = s.replaceAll(" ", ",");
		
		return new DataValues(noOfRows, s);
	}
	
	private DataValues formatLabels(String s) {
		String newString = "";
		Pattern hashPattern = Pattern.compile("([^#]*)#([^#]*)#([^#]*)", Pattern.CASE_INSENSITIVE + Pattern.DOTALL);
		Matcher hashMatcher = hashPattern.matcher(s);
		while (hashMatcher.find())
			newString += hashMatcher.group(1) + "#" + hashMatcher.group(2).replaceAll(" ","~") + "#" + hashMatcher.group(3);
		if (newString.length() == 0)
			newString = s;
		
		String labelsWithCommas = newString.replaceAll(" ", ",");
		String quotedLabels = labelsWithCommas.replaceAll("([^,]+)", "\"$1\"");
		String quotedLabelsWithSpaces = quotedLabels.replaceAll("~", " ");
		String labelsWithoutHashes = quotedLabelsWithSpaces.replaceAll("#", "");

		int noOfValues = 1;
		int index = 0;
		while ((index = labelsWithoutHashes.indexOf(",", index)) != -1) {
			index++;
			noOfValues++;
		}

		return new DataValues(noOfValues, labelsWithoutHashes);
	}
}
