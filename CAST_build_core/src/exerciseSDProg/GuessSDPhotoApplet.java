package exerciseSDProg;

import java.awt.*;
import java.util.*;

import dataView.*;
import utils.*;
import exercise2.*;
import formula.*;
import imageUtils.*;
import imageGroups.*;


public class GuessSDPhotoApplet extends ExerciseApplet {
//	static final private String VAR_NAME_PARAM = "varName";
	
	private ImageCanvas thePhoto;
	
	protected ResultValuePanel resultPanel;
	
//================================================
	
	protected void createDisplay() {
		setLayout(new BorderLayout(0, 10));
		
			questionPanel = new QuestionPanel(this);
		add("North", questionPanel);
		
		add("Center", getWorkingPanels(null));
				
			XPanel bottomPanel = new XPanel();
			bottomPanel.setLayout(new VerticalLayout(VerticalLayout.FILL, VerticalLayout.VERT_TOP, 4));
			
				resultPanel = new ResultValuePanel(this,
											MText.expandText("Approx standard deviation is"), getUnits(), 6);
				registerStatusItem("sd", resultPanel);
			bottomPanel.add(resultPanel);
			
			bottomPanel.add(createMarkingPanel(NO_HINTS));
			
				XPanel messagePanel = new XPanel();
				messagePanel.setLayout(new FixedSizeLayout(100, getMessageHeight()));
					
					message = new ExerciseMessagePanel(this);
				messagePanel.add(message);
			bottomPanel.add(messagePanel);
		
		add("South", bottomPanel);
	}
	
//-----------------------------------------------------------
	
	protected void registerParameterTypes() {
//		registerParameter("index", "int");					//	always registered
		registerParameter("photoFile", "string");
		registerParameter("itemsName", "string");
		registerParameter("variable", "string");
		registerParameter("units", "string");
		registerParameter("mean", "const");
		registerParameter("answer", "string");
	}
	
	public String getPhotoFile() {
//		String dirAndFile = "guessSdPhotos/" + getStringParam("photoFile") + ".jpg";
		String dirAndFile = "guessSdPhotos/" + getStringParam("photoFile") + ".png";
//		System.out.println("Photo file: " + dirAndFile);
		return dirAndFile;
	}
	
	public String getItemsName() {
		return getStringParam("itemsName");
	}
	
	public String getVarName() {
		return getStringParam("variable");
	}
	
	public NumValue getMean() {
		return getNumValueParam("mean");
	}
	
	private NumValue getIndexedSD(int index) {
		StringTokenizer st = new StringTokenizer(getStringParam("answer"));
		for (int i=0 ; i<index ; i++)
			st.nextToken();
		return new NumValue(st.nextToken());
	}
	
	public NumValue getLowSD() {
		return getIndexedSD(0);
	}
	
	public NumValue getBestSD() {
		return getIndexedSD(1);
	}
	
	public NumValue getHighSD() {
		return getIndexedSD(2);
	}
	
	protected String getUnits() {
		return getStringParam("units");
	}
	
	
//-----------------------------------------------------------
	
	protected XPanel getWorkingPanels(DataSet data) {
		XPanel thePanel = new XPanel();
//		thePanel.setLayout(new VerticalLayout(VerticalLayout.CENTER, VerticalLayout.VERT_CENTER, 0));
		thePanel.setLayout(new BorderLayout(0, 0));
			
			thePhoto = new ImageCanvas(SdPhotosImages.getPhoto(getPhotoFile(), thePanel), SdPhotosImages.kWidth,
																																SdPhotosImages.kHeight, this);
			
//		thePanel.add(thePhoto);
		thePanel.add("Center", thePhoto);
		return thePanel;
	}
	
	protected void setDisplayForQuestion() {
		thePhoto.setImage(SdPhotosImages.getPhoto(getPhotoFile(), this));
		thePhoto.repaint();
		
		resultPanel.changeUnits(getUnits());
		resultPanel.clear();
		resultPanel.invalidate();
		validate();
	}
	
	protected void setDataForQuestion() {
	}
	
	protected DataSet getData() {
		return null;
	}
	
	
//-----------------------------------------------------------
	
	protected void insertMessageContent(MessagePanel messagePanel) {
		NumValue attempt = resultPanel.getAttempt();
		switch (result) {
			case ANS_UNCHECKED:
				messagePanel.insertText("Guess the standard deviation of the " + getVarName()
														+ "s of the " + getItemsName() + " then type it into the text-edit box above.");
				break;
			case ANS_INCOMPLETE:
				messagePanel.insertRedHeading("Error!\n");
				messagePanel.insertText("You must type a value for the standard deviation.");
				break;
			case ANS_INVALID:
				messagePanel.insertRedHeading("Wrong!\n");
				messagePanel.insertText("The standard deviation should be positive.");
				break;
			case ANS_TOLD:
				messagePanel.insertRedHeading("Answer\n");
				messagePanel.insertText("A reasonable value for the standard deviation is shown.");
				messagePanel.insertText("\nIt is possible that about 5% of " + getItemsName() + " might have "
																			+ getVarName());
				insertTwoSdMessage(messagePanel, attempt);
				break;
			case ANS_CORRECT:
				messagePanel.insertRedHeading("Good!\n");
				messagePanel.insertText("The standard deviation that you specified is a reasonable value for the "
																					+ getVarName() + "s of the " + getItemsName() + ".");
				messagePanel.insertText("\nIt is possible that about 5% of " + getItemsName() + " might have "
																			+ getVarName());
				insertTwoSdMessage(messagePanel, attempt);
				break;
			case ANS_WRONG:
				messagePanel.insertRedHeading("Not a reasonable value!\n");
				NumValue lowAnswer = getLowSD();
				String moreLess = (attempt.toDouble() < lowAnswer.toDouble()) ? "more" : "less";
				messagePanel.insertText("It is likely that " + moreLess + " than 5% of the " + getItemsName() + " will have "
																+ getVarName());
				insertTwoSdMessage(messagePanel, attempt);
				break;
		}
	}
	
	private void insertTwoSdMessage(MessagePanel messagePanel, NumValue sd) {
		NumValue mean = getMean();
		int decimals = Math.max(mean.decimals, sd.decimals);
		NumValue lowValue = new NumValue(mean.toDouble() - 2.0 * sd.toDouble(), decimals);
		NumValue highValue = new NumValue(mean.toDouble() + 2.0 * sd.toDouble(), decimals);
		messagePanel.insertText(" outside the range " + lowValue + " to " + highValue + " " + getUnits()
																+ " (i.e. #mu# #plusMinus# 2#sigma#).");
	}
	
	protected int getMessageHeight() {
		return 150;
	}
	
	
//-----------------------------------------------------------
	
	protected int assessAnswer() {
		double highApprox = getHighSD().toDouble();
		double lowApprox = getLowSD().toDouble();
		
		double attempt = resultPanel.getAttempt().toDouble();
		
		return (resultPanel.isClear()) ? ANS_INCOMPLETE
						: (attempt <= 0.0) ? ANS_INVALID
						: (attempt >= lowApprox && attempt <= highApprox) ? ANS_CORRECT
						: ANS_WRONG;
	}
	
	protected void giveFeedback() {
	}
	
	protected void showCorrectWorking() {
		resultPanel.showAnswer(getBestSD());
	}
	
	protected double getMark() {
		return (assessAnswer() == ANS_CORRECT) ? 1 : 0;
	}
}