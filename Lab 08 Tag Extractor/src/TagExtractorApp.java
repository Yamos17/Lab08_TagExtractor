import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class TagExtractorApp {
    private JFrame frame;
    private JTextArea resultTextArea;
    private JButton openTextFileButton;
    private JButton openStopWordsButton;
    private JButton processButton;
    private JButton saveTagsButton;
    private Map<String, Integer> wordFrequencies = new HashMap<>();
    private Set<String> stopWords = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    private File selectedTextFile;
    private File selectedStopWordsFile;

    public TagExtractorApp() {
        frame = new JFrame("Tag Extractor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        openTextFileButton = new JButton("Open Text File");
        openStopWordsButton = new JButton("Open Stop Words File");
        processButton = new JButton("Process Text");
        saveTagsButton = new JButton("Save Tags");

        resultTextArea = new JTextArea(20, 60);
        resultTextArea.setWrapStyleWord(true);
        resultTextArea.setLineWrap(true);

        openTextFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files", "txt");
                fileChooser.setFileFilter(filter);
                int result = fileChooser.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    selectedTextFile = fileChooser.getSelectedFile();
                    resultTextArea.setText("Selected Text File: " + selectedTextFile.getName() + "\n");
                }
            }
        });

        openStopWordsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files", "txt");
                fileChooser.setFileFilter(filter);
                int result = fileChooser.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    selectedStopWordsFile = fileChooser.getSelectedFile();
                    resultTextArea.append("Selected Stop Words File: " + selectedStopWordsFile.getName() + "\n");
                }
            }
        });

        processButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                processText();
            }
        });

        saveTagsButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveTagsToFile();
            }
        });

        frame.add(openTextFileButton, BorderLayout.NORTH);
        frame.add(openStopWordsButton, BorderLayout.NORTH);
        frame.add(new JScrollPane(resultTextArea), BorderLayout.CENTER);
        frame.add(processButton, BorderLayout.SOUTH);
        frame.add(saveTagsButton, BorderLayout.SOUTH);
        frame.pack();
        frame.setVisible(true);
    }

    private void loadStopWords() {
        stopWords.clear();
        try (Scanner scanner = new Scanner(selectedStopWordsFile)) {
            while (scanner.hasNext()) {
                stopWords.add(scanner.next().toLowerCase());
            }
        } catch (FileNotFoundException e) {
            resultTextArea.append("Error loading stop words file: " + e.getMessage() + "\n");
        }
    }

    private void processText() {
        if (selectedTextFile == null || selectedStopWordsFile == null) {
            resultTextArea.append("Please select both a text file and a stop words file.\n");
            return;
        }

        loadStopWords();
        wordFrequencies.clear();

        try (Scanner scanner = new Scanner(selectedTextFile)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] words = line.split("\\s+");

                for (String word : words) {
                    word = word.replaceAll("[^a-zA-Z]", "").toLowerCase();
                    if (!word.isEmpty() && !stopWords.contains(word)) {
                        wordFrequencies.put(word, wordFrequencies.getOrDefault(word, 0) + 1);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            resultTextArea.append("Error processing text file: " + e.getMessage() + "\n");
        }

        displayWordFrequencies();
    }

    private void saveTagsToFile() {
        if (wordFrequencies.isEmpty()) {
            resultTextArea.append("No tags to save.\n");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(selectedFile)) {
                for (Map.Entry<String, Integer> entry : wordFrequencies.entrySet()) {
                    writer.println(entry.getKey() + ": " + entry.getValue());
                }
                resultTextArea.append("Tags saved to " + selectedFile.getName() + "\n");
            } catch (FileNotFoundException e) {
                resultTextArea.append("Error saving tags: " + e.getMessage() + "\n");
            }
        }
    }

    private void displayWordFrequencies() {
        resultTextArea.append("\nWord Frequencies:\n");
        for (Map.Entry<String, Integer> entry : wordFrequencies.entrySet()) {
            resultTextArea.append(entry.getKey() + ": " + entry.getValue() + "\n");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TagExtractorApp());
    }
}
