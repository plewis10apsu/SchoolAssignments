package edu.apsu.langlearn;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.*;

public class LangLearn extends Application {
    private ObservableList<LanguageWordMeaning> langWordMeanObList;
    private ListView<LanguageWordMeaning> langWordMeanListView;
    private Stage stage;
    private TextField langTextField;
    private TextField wordTextField;
    private TextField meaningTextField;
    private Button multiButton;
    private File selectedFile;
    private boolean savingWasCanceled = false;
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        
        createMainUI();
    }
    
    private void createMainUI() {
        VBox root = new VBox();
        
        //Create a menu bar
        MenuBar menuBar = new MenuBar();
        HBox.setHgrow(menuBar, Priority.ALWAYS);
        
        //Create a File menu
        Menu fileMenu = new Menu("File");
        
        //Create menu items
        MenuItem newMenuItem = new MenuItem("New");
        newMenuItem.setOnAction(e -> newData());
        
        MenuItem openMenuItem = new MenuItem("Open");
        openMenuItem.setOnAction(e -> openData());
        
        MenuItem saveMenuItem = new MenuItem("Save");
        saveMenuItem.setOnAction(e -> saveData());
        
        MenuItem exitMenuItem = new MenuItem("Exit");
        exitMenuItem.setOnAction(e -> Platform.exit());
        
        //Create separators
        SeparatorMenuItem separator1 = new SeparatorMenuItem();
        SeparatorMenuItem separator2 = new SeparatorMenuItem();
        
        //Add MenuItems to the File menu
        fileMenu.getItems().addAll(newMenuItem, separator1,
                openMenuItem, saveMenuItem, separator2, exitMenuItem);
        
        //Add file menu to the MenuBar
        menuBar.getMenus().add(fileMenu);
        
        langWordMeanObList = FXCollections.observableArrayList();
        
        //Create a ListView to display the words, meaning and Language
        langWordMeanListView = new ListView<>();
        langWordMeanListView.setPrefHeight(200);
        listViewSelectionListener();
        langWordMeanListView.setItems(langWordMeanObList);
        VBox listViewVBox = new VBox(langWordMeanListView);
        
        //Create a Labels and TextFields grouped together in HBoxes
        Label langLabel = new Label("Language");
        langTextField = new TextField();
        langTextField.setPrefWidth(250);
        HBox langHBox = new HBox(langLabel, langTextField);
        langHBox.setAlignment(Pos.BASELINE_RIGHT);
        langHBox.setSpacing(25);
        
        Label wordLabel = new Label("Word");
        wordTextField = new TextField();
        wordTextField.setPrefWidth(250);
        HBox wordHBox = new HBox(wordLabel, wordTextField);
        wordHBox.setAlignment(Pos.BASELINE_RIGHT);
        wordHBox.setSpacing(45);
        wordHBox.setPadding(new Insets(1, 0, 1, 0));
        
        Label meaningLabel = new Label("Meaning");
        meaningTextField = new TextField();
        meaningTextField.setPrefWidth(250);
        HBox meaningHBox = new HBox(meaningLabel, meaningTextField);
        meaningHBox.setAlignment(Pos.BASELINE_RIGHT);
        meaningHBox.setSpacing(30);
        
        //Create Buttons grouped together in an HBox
        multiButton = new Button("Add");
        multiButton.setPrefWidth(75);
        multiButton.setOnAction(e -> handle());
        
        Button resetButton = new Button("Reset");
        resetButton.setPrefWidth(75);
        resetButton.setOnAction(e -> {
            clearTextFields();
            multiButton.setText("Add");
            unselect();
        });
        
        HBox buttonHBox = new HBox(multiButton, resetButton);
        buttonHBox.setAlignment(Pos.BASELINE_RIGHT);
        buttonHBox.setSpacing(1);
        buttonHBox.setPadding(new Insets(1, 0, 0, 0));
        
        //Create a VBox to contain input fields and buttons for updates
        VBox updateVBox = new VBox(langHBox, wordHBox, meaningHBox, buttonHBox);
        updateVBox.setAlignment(Pos.TOP_CENTER);
        updateVBox.setPadding(new Insets(30, 30, 0, 30));
        
        //Create an HBox to arrange the listViewVBox and updateVBox side by side
        HBox horizontalContainerHBox = new HBox(listViewVBox, updateVBox);
        
        //Create a VBox to stack the MenuBar and horizontalContainerHBox vertically
        VBox verticalContainerVBox = new VBox(menuBar, horizontalContainerHBox);
        
        //Add the verticalContainerVBox to the root
        root.getChildren().add(verticalContainerVBox);
        
        //Create the scene and add it to the stage
        Scene scene = new Scene(root);
        stage.setTitle("Peggy Lewis' LangLearn");
        stage.setScene(scene);
        stage.show();
    }
    
    private void saveData() {
        createFileChooser(true);
        if (selectedFile != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile))) {
                for (LanguageWordMeaning item : langWordMeanObList) {
                    String line = item.getLanguage() + "," + item.getWord() + "," + item.getMeaning();
                    writer.write(line);
                    writer.newLine();
                }
                displayAlert(Alert.AlertType.INFORMATION,
                        "Success",
                        "Save Successful",
                        "The data has been saved successfully.");
            } catch (IOException e) {
                displayAlert(Alert.AlertType.ERROR,
                        "IO Error",
                        "An error occurred while saving the file.",
                        "Please try again later.");
                savingWasCanceled = true;
            }
            clearTextFields();
        } else {
            displayAlert(Alert.AlertType.INFORMATION,
                    "Canceled",
                    "Save operation canceled",
                    "No file saved.");
            savingWasCanceled = true;
        }
        unselect();
    }
    
    private void openData() {
        createListView();
        multiButton.setText("Add");
    }
    
    private void newData() {
        clearTextFields();
        langWordMeanListView.getItems().clear();
        multiButton.setText("Add");
    }
    
    /*
     * Creates a ListView containing LanguageWordMeaning (LWM) objects from a selected .lang file
     */
    private void createListView() {
        createFileChooser(false);
        
        // Check if the user canceled the file selection
        if (selectedFile != null) {
            
            // Clear the existing data
            langWordMeanObList = FXCollections.observableArrayList();
            
            //Try to open the selected file for reading
            try (BufferedReader reader = new BufferedReader(new FileReader(selectedFile))) {
                String line;
                
                //Read lines from selected file and parse them into LWM objects
                while ((line = reader.readLine()) != null) {
                    
                    String[] split = line.split(",");
                    langWordMeanObList.add(new LanguageWordMeaning(split));
                    
                }
                //Sort the ObservableList using the compareTo method from LWM class
                langWordMeanObList.sort(LanguageWordMeaning::compareTo);
                
            } catch (FileNotFoundException e) {
                displayAlert(Alert.AlertType.ERROR,
                        "File Not Found",
                        "The file could not be found.",
                        "Please check the file path.");
            } catch (IOException e) {
                displayAlert(Alert.AlertType.ERROR,
                        "IO Error",
                        "An error occurred while reading the file.",
                        "Please try again later.");
            }
            
            //Set the sorted ObservableList to the ListView
            langWordMeanListView.setItems(langWordMeanObList);
            clearTextFields();
        } else {
            unselect();
            displayAlert(Alert.AlertType.INFORMATION,
                    "Canceled",
                    "Open operation canceled",
                    "No file selected.");
        }
    }
    
    private void createFileChooser(boolean forSaving) {
        //Create FileChooser
        FileChooser fileChooser = new FileChooser();
        
        //Add an extension filter for .lang files only
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Lang Files", "*.lang")
        );
        if (forSaving) {
            //Show the save file dialog and allow the user to save a file
            selectedFile = fileChooser.showSaveDialog(stage);
        } else {
            //Show the open file dialog and allow the user to select a file
            selectedFile = fileChooser.showOpenDialog(stage);
        }
    }
    
    private void listViewSelectionListener() {
        langWordMeanListView.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue != null && !savingWasCanceled) {
                        setText(langTextField, wordTextField, meaningTextField, newValue);
                        multiButton.setText("Update");
                    } else if (!savingWasCanceled) {
                        multiButton.setText("Add");
                    }
                    savingWasCanceled = false;
                });
    }
    
    private void handle() {
        String lang = langTextField.getText().toUpperCase();
        String word = wordTextField.getText().toLowerCase();
        String meaning = meaningTextField.getText();
        
        if (isValidInput(lang, word, meaning)) {
            LanguageWordMeaning newItem = new LanguageWordMeaning(lang, word, meaning);
            if (multiButton.getText().equals("Add")) {
                handleNewItem(newItem);
            } else {
                handleUpdateItem(newItem);
            }
            unselect();
            clearTextFields();
        } else {
            displayAlert(Alert.AlertType.ERROR,
                    "Input Error",
                    "Fields Cannot Be Blank",
                    "Please make sure all text fields are filled " +
                            "before adding.");
        }
    }
    
    private void handleNewItem(LanguageWordMeaning newItem) {
        // Check if a word with the same value already exists
        if (langWordMeanObList != null && !isWordAlready(newItem)) {
            langWordMeanObList.add(newItem);
            langWordMeanObList.sort(LanguageWordMeaning::compareTo);
        }
    }
    
    private void handleUpdateItem(LanguageWordMeaning newItem) {
        // Update the item
        int selectedIndex = langWordMeanListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < langWordMeanObList.size()) {
            langWordMeanObList.set(selectedIndex, newItem);
            langWordMeanObList.sort(LanguageWordMeaning::compareTo);
        }
    }
    
    private boolean isValidInput(String lang, String word, String meaning) {
        return !lang.isEmpty() && !word.isEmpty() && !meaning.isEmpty();
    }
    
    private void setText(TextField lang, TextField word, TextField meaning, LanguageWordMeaning text) {
        lang.setText(text.getLanguage());
        word.setText(text.getWord());
        meaning.setText(text.getMeaning());
    }
    
    private void clearTextFields() {
        langTextField.clear();
        wordTextField.clear();
        meaningTextField.clear();
    }
    
    private boolean isWordAlready(LanguageWordMeaning newItem) {
        for (LanguageWordMeaning existingItem : langWordMeanObList) {
            if (existingItem.getWord().equalsIgnoreCase(newItem.getWord())) {
                return true;
            }
        }
        return false;
    }
    
    private void unselect() {
        // Clear the ListView selection to remove the gray highlight
        langWordMeanListView.getSelectionModel().clearSelection();
    }
    
    private void displayAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    public class LanguageWordMeaning implements Comparable<LanguageWordMeaning> {
        private final String language;
        private final String word;
        private final String meaning;
        
        public LanguageWordMeaning(String language, String word, String meaning) {
            this.language = language;
            this.word = word;
            this.meaning = meaning;
        }
        
        public LanguageWordMeaning(String[] split) {
            if (split.length != 3) {
                displayAlert(Alert.AlertType.ERROR,
                        "Invalid data",
                        "Insufficient data.",
                        "The input does not have enough data.");
            }
            this.language = split[0];
            this.word = split[1];
            this.meaning = split[2];
        }
        
        public String getLanguage() {
            return language;
        }
        
        public String getWord() {
            return word;
        }
        
        public String getMeaning() {
            return meaning;
        }
        
        @Override
        public String toString() {
            return getLanguage() + " / " + getWord();
        }
        
        @Override
        public int compareTo(LanguageWordMeaning o) {
            //Compare Language
            int langCompare = getLanguage().compareTo(o.getLanguage());
            if (langCompare != 0) {
                return langCompare;
            }
            //Compare Word
            return getWord().compareTo(o.getWord());
        }
    }
}
