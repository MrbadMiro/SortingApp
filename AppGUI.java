// Imports required for GUI components and file handling
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

// Main class for creating and managing the application's GUI
public class AppGUI {
    // Declare main GUI components
    private JFrame frame;                     // Main application window
    private JTextArea resultArea;            // Text area to display analysis results
    private JComboBox<String> columnSelector; // Dropdown to select a column for analysis
    private JTable dataPreviewTable;         // Table to preview uploaded data
    private File currentFile;                // Currently selected CSV file
    private List<String[]> previewData;      // List to hold preview data from the CSV
    private JButton analyzeButton;           // Button to trigger performance analysis

    // Initializes and displays the main GUI window
    public void createAndShowGUI() {
        frame = new JFrame("Sorting Algorithm Performance Evaluator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        // Create the main layout panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add top, center, and bottom panels to the main layout
        mainPanel.add(createTopPanel(), BorderLayout.NORTH);
        mainPanel.add(createCenterPanel(), BorderLayout.CENTER);
        mainPanel.add(createBottomPanel(), BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    // Creates the top panel for file upload and column selection
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton uploadButton = new JButton("Upload CSV File"); // Button for uploading files
        columnSelector = new JComboBox<>();                   // Dropdown for selecting a column
        columnSelector.setEnabled(false);                     // Disabled until file is uploaded
        analyzeButton = new JButton("Analyze Performance");   // Button for analyzing data
        analyzeButton.setEnabled(false);                      // Disabled until valid column is selected

        // Add action listeners for button functionalities
        uploadButton.addActionListener(e -> handleFileUpload());
        columnSelector.addActionListener(e -> handleColumnSelection());
        analyzeButton.addActionListener(e -> handleAnalysis());

        // Add components to the panel
        panel.add(uploadButton);
        panel.add(new JLabel("Select Column: "));
        panel.add(columnSelector);
        panel.add(analyzeButton);

        return panel;
    }

    // Creates the center panel for data preview
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Data Preview"));

        // Table to display the CSV data
        dataPreviewTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(dataPreviewTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    // Creates the bottom panel for displaying analysis results
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Results"));

        // Text area for displaying results
        resultArea = new JTextArea(8, 50);
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    // Handles file upload and updates the UI accordingly
    private void handleFileUpload() {
        JFileChooser fileChooser = new JFileChooser(); // File chooser dialog
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".csv");
            }
            public String getDescription() {
                return "CSV Files (*.csv)";
            }
        });

        // Handle file selection
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            try {
                previewData = CsvHandler.loadPreviewData(currentFile, 5); // Load preview data
                updateColumnSelector(previewData.get(0));               // Update column dropdown
                updateDataPreview(previewData);                         // Display preview data
                analyzeButton.setEnabled(true);                         // Enable analysis button
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Error reading file: " + ex.getMessage());
            }
        }
    }

    // Updates the column selector with column names
    private void updateColumnSelector(String[] headers) {
        columnSelector.removeAllItems();
        for (int i = 0; i < headers.length; i++) {
            columnSelector.addItem(headers[i] + " (Column " + i + ")");
        }
        columnSelector.setEnabled(true);
    }

    // Updates the data preview table with CSV content
    private void updateDataPreview(List<String[]> data) {
        if (data.isEmpty()) return;

        String[] headers = data.get(0);                      // First row as headers
        String[][] previewRows = data.subList(1, data.size()) // Remaining rows
                              .toArray(new String[0][]);

        DefaultTableModel model = new DefaultTableModel(previewRows, headers);
        dataPreviewTable.setModel(model);
    }

    // Handles column selection changes
    private void handleColumnSelection() {
        analyzeButton.setEnabled(columnSelector.getSelectedIndex() != -1); // Enable if valid column selected
    }

    // Performs sorting performance analysis on selected column data
    private void handleAnalysis() {
        try {
            int selectedColumn = columnSelector.getSelectedIndex();
            List<Integer> data = CsvHandler.loadCsvColumn(currentFile, selectedColumn);

            if (data.isEmpty()) {
                throw new IllegalStateException("No valid numeric data found in the selected column");
            }

            int[] array = data.stream().mapToInt(Integer::intValue).toArray();
            Map<String, Long> results = new TreeMap<>();

            // Test performance of different sorting algorithms
            results.put("Insertion Sort", PerformanceEvaluator.evaluateSortingPerformance(array, "Insertion Sort"));
            results.put("Shell Sort", PerformanceEvaluator.evaluateSortingPerformance(array, "Shell Sort"));
            results.put("Merge Sort", PerformanceEvaluator.evaluateSortingPerformance(array, "Merge Sort"));
            results.put("Quick Sort", PerformanceEvaluator.evaluateSortingPerformance(array, "Quick Sort"));
            results.put("Heap Sort", PerformanceEvaluator.evaluateSortingPerformance(array, "Heap Sort"));

            // Find the fastest algorithm
            Map.Entry<String, Long> bestAlgorithm = results.entrySet()
                .stream()
                .min(Map.Entry.comparingByValue())
                .get();

            // Display performance results
            StringBuilder output = new StringBuilder();
            output.append(String.format("Array size: %,d elements%n%n", array.length));
            results.forEach((algorithm, time) -> output.append(String.format("%s: %,d ns%n", algorithm, time)));
            output.append(String.format("%nBest performing algorithm: %s (%,d ns)", 
                bestAlgorithm.getKey(), bestAlgorithm.getValue()));
            resultArea.setText(output.toString());

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(frame, "Error reading file: " + ex.getMessage());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
        }
    }
}
