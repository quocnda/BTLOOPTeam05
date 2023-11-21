package com.example.btl1_dictionary;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebView;
import javafx.util.Callback;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

public class Search_Controller extends General_Controller {

    @FXML
    public TextField searchBar;

    @FXML
    private ImageView voice;
    @FXML
    private final Image Voice_Image = new Image(getClass().getResource("/com/example/btl1_dictionary/Image/Voice_Button.png").toExternalForm());

    @FXML
    private ImageView saved;

    @FXML
    private final Image Saved_Image_Off = new Image(getClass().getResource("/com/example/btl1_dictionary/Image/star_black.png").toExternalForm());

    @FXML
    private final Image Saved_Image_On = new Image(getClass().getResource("/com/example/btl1_dictionary/Image/star.png").toExternalForm());

    @FXML
    private WebView word;

    @FXML
    private WebView meaning;

    @FXML
    private ListView<String> suggestion;

    private List<String> wordList = new ArrayList<String>();

    @FXML
    private ImageView searchButton;

    private boolean isSaved = false;

    private List<String> savedList = new ArrayList<>();
    private List<String> historyList = new ArrayList<>();
    private List<Integer> frequencyList = new ArrayList<>();

    public TextField getSearchBar() {
        return searchBar;
    }

    public void initialize() {

//        for(int i = 0 ; i <ldata.size() ; i++) {
//            System.out.println(ldata.get(i).getWord());
//        }
        System.out.println("davao ktao");
        Database_Connect.loadSuggestions();
        //ConnectToMongo.test("Ykhoa");
//        List<Data> ldata = ConnectToMongo.getData("Ykhoa");
    }

    private void showSuggestions(String input) {
        ObservableList<String> filteredSuggestions = FXCollections.observableArrayList();
        List<String> matchingPrefixSuggestions = new ArrayList<>();
        List<String> nonMatchingSuggestions = new ArrayList();

        for (String suggestion : wordList) {
            if (suggestion.toLowerCase().startsWith(input.toLowerCase())) {
                matchingPrefixSuggestions.add(suggestion);
            } else if (suggestion.toLowerCase().contains(input.toLowerCase())) {
                nonMatchingSuggestions.add(suggestion);
            }
        }

        matchingPrefixSuggestions.sort((s1, s2) -> {
            String prefix = input.toLowerCase();
            return Integer.compare(s1.toLowerCase().indexOf(prefix), s2.toLowerCase().indexOf(prefix));
        });

        filteredSuggestions.addAll(matchingPrefixSuggestions);
        filteredSuggestions.addAll(nonMatchingSuggestions);

        suggestion.setItems(filteredSuggestions);
    }

    public static class CustomListCellFactory implements Callback<ListView<String>, ListCell<String>> {
        @Override
        public ListCell<String> call(ListView<String> param) {
            return new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item == null || empty) {
                        setText(null);
                    } else {
                        setText(item);
                        setFont(Font.font("Segoe UI", FontWeight.EXTRA_LIGHT, 15));
                    }
                }
            };
        }
    }


    @FXML
    public void onKeyTyped(KeyEvent event) {
        String input = searchBar.getText().toLowerCase();
        suggestion.setCellFactory(new CustomListCellFactory());

        Database_Connect.createSuggestions(input);

        wordList = Database_Connect.suggestions;
        suggestion.setItems(FXCollections.observableArrayList(wordList));

        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            showSuggestions(newValue);
        });

        if (event.getCode() == KeyCode.BACK_SPACE || event.getCode() == KeyCode.DELETE) {
            String currentValue = searchBar.getText();
            showSuggestions(currentValue);
        }
    }

    @FXML
    void search(MouseEvent event) throws Exception {
        isSaved = false;
        String input = searchBar.getText().toLowerCase();

        Database_Connect.lookUpDatabase(input);

        word.getEngine().loadContent(Database_Connect.word, "text/html");
        meaning.getEngine().loadContent(Database_Connect.meaning, "text/html");

        if (!Database_Connect.found) {
            voice.setImage(null);
        } else {
            voice.setImage(Voice_Image);

            handleHistory(input);
            handleFrequency(input);
            handleSaved(input);
        }
    }

    void handleHistory(String input) throws IOException {
        String line = "";
        readFile(historyPath,historyList);

        int size = historyList.size();
        int MAX_LENGTH = 300;
        if (size > MAX_LENGTH) {
            while (size >= MAX_LENGTH) {
                historyList.remove(size - 1);
                size--;
            }
        }

        historyList.removeIf(e -> e.equals(input));
        historyList.add(0, input);

        writeToFile(historyPath,historyList);
    }

    void handleSaved(String input) throws IOException {
        String line = "";
        BufferedReader br = new BufferedReader(new FileReader(savedPath));
        while ((line = br.readLine()) != null) {
            if (line.equals(input)) {
                isSaved = true;
            } else {
                if (!line.isEmpty()) {
                    savedList.add(line.trim());
                }
            }

        }
        br.close();

        if (isSaved) {
            saved.setImage(Saved_Image_On);
        } else {
            saved.setImage(Saved_Image_Off);
        }
    }

    void handleFrequency(String input) throws IOException {
        long currentTimeMillis = System.currentTimeMillis();
        Date currentDate = new Date(currentTimeMillis);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String currentDateString = dateFormat.format(currentDate);

        boolean getDate = true;
        String lastDate ="";
        String line = "";
        BufferedReader br = new BufferedReader(new FileReader(frequencyPath));
        while ((line = br.readLine()) != null) {
            if (!line.isEmpty()) {
                if (getDate) {
                    lastDate = line;
                    getDate = false;
                    continue;
                }
                frequencyList.add(Integer.parseInt(line.trim()));
            }
        }
        br.close();

        if (currentDateString.equals(lastDate)) {
            frequencyList.set(0,frequencyList.get(0)+1);
        } else {
            frequencyList.add(0,1);
            frequencyList.remove(frequencyList.size() - 1);
        }

        FileWriter fw = new FileWriter(frequencyPath);
        fw.write(currentDateString);
        fw.write("\n");
        for (Integer lineToWrite : frequencyList) {
            fw.write(lineToWrite.toString());
            fw.write("\n");
        }
        fw.close();
    }

    @FXML
    void Voice(MouseEvent event) {
        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        Voice voice = VoiceManager.getInstance().getVoice("kevin16");
        if(voice!=null) {
            voice.allocate();
            voice.speak(searchBar.getText());
            voice.deallocate();
        }
    }

    @FXML
    void Save(MouseEvent event) throws IOException {
        String input = searchBar.getText().toLowerCase();
        if (isSaved) {
            isSaved = false;
            saved.setImage(Saved_Image_Off);
            savedList.removeIf(e -> e.equals(input));
        } else {
            isSaved = true;
            saved.setImage(Saved_Image_On);
            savedList.add(input);
        }
        writeToFile(savedPath,savedList);
    }

    @FXML
    void Fill(MouseEvent event) {
        int selectedIndex = suggestion.getSelectionModel().getSelectedIndex();

        if (selectedIndex >= 0) {
            String selectedItem = suggestion.getItems().get(selectedIndex);
            searchBar.setText(selectedItem);
        }
    }


    @Override
    public void Entered(MouseEvent event) {
        ImageView enteredImageView = (ImageView) event.getSource();

        if (enteredImageView == getHistory_Button()) {
            setHistory_Button(History_Image);
            setSearch_Button(null);
        } else if (enteredImageView == getSaved_Button()) {
            setSaved_Button(Saved_Image);
            setSearch_Button(null);
        } else if (enteredImageView == getEdit_Button()) {
            setEdit_Button(Edit_Image);
            setSearch_Button(null);
        } else if (enteredImageView == getGoogle_Button()) {
            setGoogle_Button(Google_Image);
            setSearch_Button(null);
        } else if (enteredImageView == getGame_Button()) {
            setGame_Button(Game_Image);
            setSearch_Button(null);
        }
    }

    @Override
    public void Exited(MouseEvent event) {
        ImageView exitedImageView = (ImageView) event.getSource();

        if (exitedImageView == getHistory_Button()) {
            setHistory_Button(null);
            setSearch_Button(Search_Image);
        } else if (exitedImageView == getSaved_Button()) {
            setSaved_Button(null);
            setSearch_Button(Search_Image);
        } else if (exitedImageView == getEdit_Button()) {
            setEdit_Button(null);
            setSearch_Button(Search_Image);
        } else if (exitedImageView == getGoogle_Button()) {
            setGoogle_Button(null);
            setSearch_Button(Search_Image);
        } else if (exitedImageView == getGame_Button()) {
            setGame_Button(null);
            setSearch_Button(Search_Image);
        }
    }
}
