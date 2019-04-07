package com.smoftware.elmour.dialog;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.smoftware.elmour.Entity;
import com.smoftware.elmour.EntityConfig;
import com.smoftware.elmour.UI.MyTextArea;
import com.smoftware.elmour.Utility;
import com.smoftware.elmour.profile.ProfileManager;

import java.util.ArrayList;

/**
 * Created by steve on 12/8/17.
 */

public class PopUp extends Window implements PopUpSubject {
	private static final String TAG = PopUp.class.getSimpleName();

	private enum State {HIDDEN, LISTENING}

	public enum PopUpType { CONVERSATION, SIGN }

	class Dialog {
		public String name;
		public Array<String> lineStrings;
	}

	private Array<PopUpObserver> observers;
	private ConversationGraph graph;
	private String currentEntityID;
	private Json json;
	private Dialog dialog;
	private String[] fullTextSections;
	private String fullText;
	private boolean displayText = true;
	private String currentText;
	private String currentVisibleText = "";
	private boolean delay = true;
	private MyTextArea textArea;
	private String currentCharacter;
	private State state = State.HIDDEN;
	private boolean interactReceived = false;
	private boolean isReady = false;
	private boolean isEcho = false;
	private boolean isActive = false;
	private boolean isThreadRunning = false;
	private boolean doneWithCurrentNode = true;
	private long setVisibleDelay = 0;
	private PopUpType popUpType;
	private Object criticalSection;

	public PopUp(PopUpType popUpType) {
		//Notes:
		//font is set in the Utility class
		//popup is created in PlayerHUD class
		//textArea is created in hide() function so that it is recreated each time it is shown (hack to get around issues)
		super("", Utility.ELMOUR_UI_SKIN, "default");

		dialog = new Dialog();

		json = new Json();
		observers = new Array<PopUpObserver>();
		this.popUpType = popUpType;
		criticalSection  = new Object();
		hide();
	}

	@Override
	public void addObserver(PopUpObserver popUpObserver) {
		observers.add(popUpObserver);
	}

	@Override
	public void removeObserver(PopUpObserver popUpObserver) {
		observers.removeValue(popUpObserver, true);
	}

	@Override
	public void removeAllObservers() {
		for(PopUpObserver observer: observers){
			observers.removeValue(observer, true);
		}
	}

	@Override
	public void notify(int value, PopUpObserver.PopUpEvent event) {
		for(PopUpObserver observer: observers){
			observer.onNotify(value, event);
		}
	}

	public boolean isVisible() { return state != State.HIDDEN; }

	public boolean isReady() { return isReady; }

	public boolean isDoneWithCurrentNode() { return doneWithCurrentNode; }

	public void interact(boolean isEcho) {

        synchronized (criticalSection) {
			this.isEcho = isEcho;

			Gdx.app.log(TAG, "popup interact cur state = " + state.toString());
			Gdx.app.log(TAG, "interact   fullText = " + fullText);
			if (isEcho)
				Gdx.app.log(TAG, "isEcho");

			switch (state) {
				case HIDDEN:
					if (fullText != "") {
						Gdx.app.log(TAG, "setting isReady to false in interact");
						isReady = false;
						this.setVisible(true);
						isActive = true;
						state = State.LISTENING;
						startInteractionThread();
					}
					break;
				case LISTENING:
					interactReceived = true;
					break;
			}

			Gdx.app.log(TAG, "popup interact new state = " + state.toString());
		}
	}

	boolean firstTime = true;

	public void cleanupTextArea() {
	    if (firstTime) {
            try {
                this.reset();
            } catch (IndexOutOfBoundsException e) {
                Gdx.app.error(TAG, "IndexOutOfBoundsException caught when cleaning up text area");
                e.printStackTrace();
            }

            textArea = new MyTextArea("", Utility.ELMOUR_UI_SKIN);
            textArea.adjustOffsetY(-3f);
            textArea.setDisabled(true);
            textArea.layout();
            //fullText = "";

            // set isReady to false so that full text doesn't flash on popup at first
            //Gdx.app.log(TAG, "setting isReady to false in cleanupTextArea");
            isReady = false;

            //layout
            this.add();
            this.defaults().expand().fill();
            this.add(textArea);

            firstTime = false;
        }
        else {
	        setTextForUIThread("", true);
        }
	}

	public void hide() {
		cleanupTextArea();
		this.setVisible(false);
		state = State.HIDDEN;

		isActive = false;

		//Gdx.app.debug(TAG, "popup interact new state = " + state.toString());
	}

	public boolean isListening() {
		if (state == State.LISTENING) {
			Gdx.app.log(TAG, "IS LISTENING");
			return true;
		}
		else {
			Gdx.app.log(TAG, "IS NOT LISTENING");
			return false;
		}
	}

	private void setTextForUIThread(String text, boolean displayText) {
		currentText = text;
		this.displayText = displayText;
	}

	public void update() {
		// called from UI thread

		//1/18/2019 - commented this out while making change for ¶ handling.
		// the actual issue may have been due to line feeds in yEd.
		//
		// make sure there are no embedded line returns (borderline bug in MyTextArea)
		//currentText = currentText.replace("\n", "");

		textArea.setText(currentText, displayText);
		//Gdx.app.log(TAG, "currentText = " + currentText);
	}

	public String getCurrentEntityID() {
		return currentEntityID;
	}

	public void loadConversationFromConfig(EntityConfig entityConfig){
		String fullFilenamePath = entityConfig.getConversationConfigPath();
		currentEntityID = entityConfig.getEntityID();
		loadConversationFromJson(fullFilenamePath);
	}

	public void loadConversationFromJson(String jsonFilePath) {
		this.getTitleLabel().setText("");

		if( jsonFilePath.isEmpty() || !Gdx.files.internal(jsonFilePath).exists() ){
			Gdx.app.debug(TAG, "Conversation file does not exist!");
			return;
		}

		ConversationGraph graph = json.fromJson(ConversationGraph.class, Gdx.files.internal(jsonFilePath));
		setConversationGraph(graph);
	}

	public void setConversationGraph(ConversationGraph graph){
		if( graph != null ) graph.removeAllObservers();
		this.graph = graph;
		populateConversationDialogById(graph.getCurrentConversationID());
	}

	public ConversationGraph getCurrentConversationGraph(){
		return this.graph;
	}

	public boolean populateConversationDialogById(String conversationID){
		Conversation conversation = graph.getConversationByID(conversationID);
		if( conversation == null ) return false;
		graph.setCurrentConversation(conversationID);
		fullText = conversation.getDialog();

		// Remove new line characters to fix issue with yEd formatting.
		// Instead, ¶ is being used to indicate actual new lines.
		fullText = fullText.replace("\n", " ");

		currentCharacter = conversation.getCharacter();

		if (currentCharacter == null)
			currentCharacter = new String(" ");

		// set character name in placeholder for label
		if (currentCharacter.startsWith("{")) {
			// get character name placeholder
			String placeholder = currentCharacter.substring(1, currentCharacter.length() - 1);
			currentCharacter = ProfileManager.getInstance().getProperty(placeholder, String.class);
		}

		// set character name(s) if placeholders are in dialog text
		String tmp = fullText;
		int leftBracketIndex = tmp.indexOf('{', 0);
		while (leftBracketIndex >= 0) {
			int rightBracketIndex = tmp.indexOf('}', leftBracketIndex + 1);
			String placeholder = tmp.substring(leftBracketIndex + 1, rightBracketIndex);
			String characterName = ProfileManager.getInstance().getProperty(placeholder, String.class);
			if (characterName == null) characterName = "<Name not set>";
			fullText = fullText.replace("{" + placeholder + "}", characterName);
			leftBracketIndex = tmp.indexOf('{', rightBracketIndex + 1);
		}
		Gdx.app.log(TAG, "populating fullText = " + fullText);

		// Split the fulltext into the separate sections to display
		fullTextSections = fullText.split("§");

		String type = conversation.getType();

		if (type.equals(ConversationNode.NodeType.ACTION.toString())) {
			//graph.notify(graph, ConversationGraphObserver.ConversationCommandEvent.valueOf(fullText));
			graph.notify(graph, ConversationGraphObserver.ConversationCommandEvent.valueOf(fullText), conversationID);
			// return false to indicate not to interact with this node
			return false;
		}
		// todo
		else if (fullText.equals("EXIT_CONVERSATION")) {
			graph.notify(graph, ConversationGraphObserver.ConversationCommandEvent.EXIT_CONVERSATION);
			graph.notify(graph, ConversationGraphObserver.ConversationCommandEvent.EXIT_CONVERSATION, conversationID);
		}

		return true;
	}

	public void populateConversationDialogByText(String text, String character){
		fullText = text;
		currentCharacter = character;
	}

	public void setTextForSignInteraction(final Entity.Interaction interaction) {
		FileHandle file = Gdx.files.internal("RPGGame/maps/Game/Text/Signs/" + interaction.toString() + ".txt");
		fullText = file.readString();
		Gdx.app.log(TAG, "file text = " + fullText);

		if (fullText.contains(";")) {
			// need to parse out delay time from beginning of file
			String[] sa = fullText.split(";");
			setVisibleDelay = Integer.parseInt(sa[0]);
			fullText = sa[1];
		}
		else {
			setVisibleDelay = 0;
		}

		// Remove new line characters to fix issue with yEd formatting.
		// Instead, ¶ is being used to indicate actual new lines.
		fullText = fullText.replace("\n", "");

		// Split the fulltext into the separate sections to display
		fullTextSections = fullText.split("§");
	}

	public void endConversation() {
		isActive = false;
	}

	private void startInteractionThread() {
		Runnable r = new Runnable() {
			public void run() {
				Gdx.app.log(TAG, "Starting InteractionThread...");

				if (isThreadRunning) {
					Gdx.app.log(TAG, "InteractionThread already running so exiting");
					return;
				}
				else {
					isThreadRunning = true;
				}

				try { Thread.sleep(setVisibleDelay); } catch (InterruptedException e) { e.printStackTrace(); }

				while (isActive) {
					int sectionsProcessed = 0;
					doneWithCurrentNode = false;

                    if (popUpType == PopUpType.CONVERSATION) {
                        graph.notify(currentCharacter, ConversationGraphObserver.ConversationCommandEvent.CHARACTER_NAME);
                    }

                    if (fullTextSections != null) {
						for (String currFullText : fullTextSections) {
							Gdx.app.log(TAG, "interaction thread   currFullText = " + currFullText);
							sectionsProcessed++;
							currentVisibleText = "";

							// need slight delay here so previous dialog can cleanup
							pause(100);

							if (dialog.lineStrings != null)
								Gdx.app.log(TAG, String.format("lineString = %d strings", dialog.lineStrings.size));

							if (dialog.lineStrings == null || dialog.lineStrings.size == 0) {
								// calculate the line strings of the textArea with currentText
								dialog.lineStrings = calculateLineStrings(currFullText);

								//Gdx.app.log(TAG, String.format("initializing textArea.getLineStrings() returned %d strings", dialog.lineStrings.size));
								//Gdx.app.log(TAG, "setting isReady to true");
								isReady = true;
							}

							// reset
							delay = true;

							// loop through lines
							for (int lineIdx = 0; lineIdx < dialog.lineStrings.size; lineIdx++) {
								// process the current line
								processLine(dialog.lineStrings.get(lineIdx));

								synchronized (criticalSection) {
									if (sectionsProcessed == fullTextSections.length && lineIdx == dialog.lineStrings.size - 1) {
										// this is the last line of the last section, so set flag so PlayerHUD knows to move on to next node
										doneWithCurrentNode = true;
									}

									if (state == State.HIDDEN)
										// break out of loop and exit thread if we were hidden
										break;
									else
										// go into listening mode
										state = State.LISTENING;

									if (popUpType == PopUpType.CONVERSATION &&
											sectionsProcessed == fullTextSections.length &&
											lineIdx == dialog.lineStrings.size - 1) {
										// show choices now since this is the last line of the dialog
										showChoices();
									}
								}

								if ((lineIdx != 0 && (lineIdx + 1) % 2 == 0) || lineIdx == dialog.lineStrings.size - 1) {
									// done populating current box so need to pause for next interaction
									while (isActive && !interactReceived && state == State.LISTENING) {
										pause(100);
									}

									if (lineIdx == dialog.lineStrings.size - 1) {
										break;
									}

									// reset for next iteration
									interactReceived = false;
									delay = true;

									if (state == State.HIDDEN)
										break;

									currentVisibleText = "";
									setTextForUIThread(currentVisibleText, true);
								}
							}

							// if this is an echo, then keep the text displayed until next interaction
							interactReceived = false;
							while (isActive && isEcho && !interactReceived) {
								pause(100);
							}

							if (doneWithCurrentNode && graph == null) {
								isActive = false;
							}

							// total reset
							currentText = "";
							displayText = false;
							interactReceived = false;

							// post a Runnable to the rendering thread
							Gdx.app.postRunnable(new Runnable() {
								@Override
								public void run() {
									dialog.lineStrings.clear();
									cleanupTextArea();
								}
							});
						}
					}
				}

				Gdx.app.log(TAG, "Exiting InteractionThread");
				PopUp.this.notify(0, PopUpObserver.PopUpEvent.INTERACTION_THREAD_EXIT);
				hide();
				isThreadRunning = false;
			}
		};

		new Thread(r).start();
	}

	private Array<String> calculateLineStrings(String currFullText) {
		// set full text so that the total number of lines can be figured out
		// send displayText = false so that text isn't displayed

		// remove "\r" and "\n" line returns
		// then replace "¶" with "\n" so number of lines is figured out correctly
		// later they will be removed if necessary while text is being processed
		//currFullText = currFullText.replace("\r\n", "");
		currFullText = currFullText.replace("\r", "");
        currFullText = currFullText.replace("\n", "");
		currFullText = currFullText.replace("¶", "\n");
		currFullText = currFullText.replace("�", "\n"); // strange character that is suppose to be ¶

		Gdx.app.log(TAG, "setting text for UI thread = " + currFullText);
		setTextForUIThread(currFullText, false);
		Gdx.app.log(TAG, "setting isReady to true");
		isReady = true;

		// wait up to 5 sec to make sure lines are populated
		int numLines = textArea.getLines();
		for (int q = 0; q < 100 && numLines == 0; q++) {
            //Gdx.app.log(TAG, String.format("textArea.getLines() = %d", textArea.getLines()));
            pause(50);

            numLines = textArea.getLines();
            //Gdx.app.log(TAG, String.format("textArea.getLines() = %d", numLines));

			// reassemble lines to make sure we got everything
			String check = "";
			for (String str : textArea.getLineStrings()) {
				check += str;
			}

			if (check.equals(currFullText)) {
				Gdx.app.log(TAG, "Got all line strings");
				break;
			}
        }

		pause(100);
		return textArea.getLineStrings();
	}

	private void processLine(String line) {
		Gdx.app.log(TAG, String.format("line.length() = %d", line.length()));

		char currentChar;

		// display line char by char for next two lines
		String currentTextBeforeNextLine = currentVisibleText;
		for (int i = 0; i < line.length(); i++) {

            if (!isEcho && (interactReceived || delay == false)) {
                Gdx.app.log(TAG, "interactReceived || delay == false");
                if (interactReceived) {
                    Gdx.app.log(TAG, "interactReceived");
                }
                if (!delay)
                    Gdx.app.log(TAG, "delay == false");
                if (!isEcho)
                    Gdx.app.log(TAG, "isEcho == false");

                interactReceived = false;
                delay = false;

                if (currentTextBeforeNextLine.length() > 0 &&
                        currentTextBeforeNextLine.charAt(currentTextBeforeNextLine.length() - 1) == '\n' && line.charAt(0) == '\n') {
                    // There is a line return at end of the previous line and also at the beginning
                    // of the next line, so get rid of the one at the end of the previous line
                    // otherwise the 2nd line will not be visible.
                    currentTextBeforeNextLine = currentTextBeforeNextLine.substring(0, currentTextBeforeNextLine.length() - 1);
                }

                currentVisibleText = currentTextBeforeNextLine + line;
                currentVisibleText = currentVisibleText.trim();
                setTextForUIThread(currentVisibleText, true);
                Gdx.app.log(TAG, "currentVisibleText = " + currentVisibleText);
                break;
            } else {
                currentChar = line.charAt(i);
                //Gdx.app.log(TAG, String.format("line.charAt(i) %c", line.charAt(i)));

                // ignore new line chars since they are not needed here (every line is one its own line),
                // they are still needed above if interaction is received prematurely to complete the 2 line text area
                if (currentChar != '\n') {
                    currentVisibleText += currentChar;

                    setTextForUIThread(currentVisibleText, true);

                    // add EOL char to text so that pending text isn't displayed as chars are added
                    if (i == line.length() - 1) {
                        currentVisibleText += '\n';
                        setTextForUIThread(currentVisibleText, true);
                    }

                    // delay for each character
                    pause(50);
                }
            }
        }
	}

	private void showChoices() {
		Gdx.app.log(TAG, "SHOWING CHOICES:");
		ArrayList<ConversationChoice> choices = graph.getCurrentChoices();
		if (choices != null) {
			// remove any choices that are no longer available based on profile settings
			for (int i = choices.size() - 1; i >= 0; i--) {
				ConversationChoice choice = choices.get(i);
				String commandEvent = choice.getConversationCommandEvent().toString();
				String profileSetting = ProfileManager.getInstance().getProperty(commandEvent, String.class);
				if (profileSetting != null) {
					choices.remove(i);
				}

				Gdx.app.log(TAG, "choice = " + choice.getChoicePhrase() + ", next id = " + choice.getDestinationId());
			}
			graph.notify(graph, choices);
		}
	}

	private void pause(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
