package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import manager.GradeManager;
import manager.StudentManager;
import manager.SubjectManager;
import model.Student;
import service.ReportService;

public class ReportPanel extends JPanel {
    private final StudentManager studentManager;
    private final SubjectManager subjectManager;
    private final GradeManager gradeManager;
    private final ReportService reportService;

    // Cụm 1: Bảng điểm chi tiết cá nhân
    private final JComboBox<StudentItem> studentSelectCombo;
    private final DefaultTableModel transcriptTableModel;
    private final JLabel creditsSummaryLabel;
    private final JLabel score10SummaryLabel;
    private final JLabel gpaSummaryLabel;
    private final JLabel rankSummaryLabel;

    // Cụm 2: Tổng hợp GPA toàn khóa
    private final DefaultTableModel allSummaryTableModel;

    // Cụm 3: Thống kê điểm theo môn
    private final DefaultTableModel subjectStatsTableModel;

    private static class StudentItem {
        final String id;
        final String name;

        StudentItem(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return id + " - " + name;
        }
    }

    public ReportPanel() {
        studentManager = new StudentManager();
        subjectManager = new SubjectManager();
        gradeManager = new GradeManager();
        reportService = new ReportService(studentManager, subjectManager, gradeManager);

        setLayout(new BorderLayout(8, 8));

        // ------------------ KHỐI TRÁI: BẢNG ĐIỂM CHI TIẾT 1 SINH VIÊN ------------------
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Bảng điểm chi tiết cá nhân"));

        JPanel studentPickerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        studentSelectCombo = new JComboBox<>();
        studentPickerPanel.add(new JLabel("Chọn sinh viên:"));
        studentPickerPanel.add(studentSelectCombo);

        transcriptTableModel = new DefaultTableModel(
                new Object[]{"Mã môn", "Tên môn học", "Số tín chỉ", "Điểm hệ 10", "Điểm chữ", "Điểm hệ 4", "Kết quả"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable transcriptTable = new JTable(transcriptTableModel);

        JPanel summaryInfoPanel = new JPanel(new GridLayout(4, 1, 4, 4));
        summaryInfoPanel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        creditsSummaryLabel = new JLabel("Tổng tín chỉ tích lũy: 0");
        score10SummaryLabel = new JLabel("Điểm trung bình (Hệ 10): 0.00");
        gpaSummaryLabel = new JLabel("Điểm GPA (Thang 4.0): 0.00");
        rankSummaryLabel = new JLabel("Xếp loại học lực: Chưa có điểm");

        Font boldFont = new Font("SansSerif", Font.BOLD, 12);
        creditsSummaryLabel.setFont(boldFont);
        score10SummaryLabel.setFont(boldFont);
        gpaSummaryLabel.setFont(boldFont);
        rankSummaryLabel.setFont(boldFont);

        summaryInfoPanel.add(creditsSummaryLabel);
        summaryInfoPanel.add(score10SummaryLabel);
        summaryInfoPanel.add(gpaSummaryLabel);
        summaryInfoPanel.add(rankSummaryPanelWrapper(rankSummaryLabel));

        JPanel leftBottomPanel = new JPanel(new BorderLayout());
        leftBottomPanel.add(summaryInfoPanel, BorderLayout.CENTER);

        JButton exportStudentCsvBtn = new JButton("Xuất bảng điểm SV (CSV)");
        exportStudentCsvBtn.addActionListener(e -> exportTranscriptToCsv());
        JPanel leftBtnBar = new FlowLayout(FlowLayout.RIGHT) == null ? new JPanel() : new JPanel(new FlowLayout(FlowLayout.RIGHT));
        leftBtnBar.add(exportStudentCsvBtn);
        leftBottomPanel.add(leftBtnBar, BorderLayout.SOUTH);

        leftPanel.add(studentPickerPanel, BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(transcriptTable), BorderLayout.CENTER);
        leftPanel.add(leftBottomPanel, BorderLayout.SOUTH);

        // ------------------ KHỐI PHẢI: BẢNG TỔNG HỢP & THỐNG KÊ MÔN ------------------
        JPanel rightPanel = new JPanel(new GridLayout(2, 1, 8, 8));

        // Phía trên: Tổng hợp GPA sinh viên
        JPanel rightTopPanel = new JPanel(new BorderLayout(4, 4));
        rightTopPanel.setBorder(BorderFactory.createTitledBorder("Bảng tổng hợp GPA toàn khóa"));

        allSummaryTableModel = new DefaultTableModel(
                new Object[]{"Mã SV", "Họ tên sinh viên", "Tổng tín chỉ", "ĐTB hệ 10", "Điểm GPA (hệ 4)", "Xếp loại"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable allSummaryTable = new JTable(allSummaryTableModel);
        JButton sortGpaBtn = new JButton("Sắp xếp theo GPA giảm dần");
        sortGpaBtn.addActionListener(e -> sortAllSummariesByGpa());
        JButton exportAllCsvBtn = new JButton("Xuất bảng tổng hợp (CSV)");
        exportAllCsvBtn.addActionListener(e -> exportAllReportsToCsv());
        JPanel rightTopBtnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightTopBtnBar.add(sortGpaBtn);
        rightTopBtnBar.add(exportAllCsvBtn);

        rightTopPanel.add(new JScrollPane(allSummaryTable), BorderLayout.CENTER);
        rightTopPanel.add(rightTopBtnBar, BorderLayout.SOUTH);

        // Phía dưới: Thống kê môn học
        JPanel rightBottomPanel = new JPanel(new BorderLayout(4, 4));
        rightBottomPanel.setBorder(BorderFactory.createTitledBorder("Thống kê điểm trung bình theo môn học"));

        subjectStatsTableModel = new DefaultTableModel(
                new Object[]{"Mã môn", "Tên môn học", "Số tín chỉ", "Số SV học", "Điểm TB môn"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable subjectStatsTable = new JTable(subjectStatsTableModel);
        rightBottomPanel.add(new JScrollPane(subjectStatsTable), BorderLayout.CENTER);

        rightPanel.add(rightTopPanel);
        rightPanel.add(rightBottomPanel);

        // Chia đôi SplitPane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(460);
        splitPane.setResizeWeight(0.45);

        add(splitPane, BorderLayout.CENTER);

        studentSelectCombo.addActionListener(e -> updateSelectedStudentTranscript());

        refreshData();
    }

    private JPanel rankSummaryPanelWrapper(JLabel label) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.add(label);
        return p;
    }

    public void refreshData() {
        studentManager.reload();
        subjectManager.reload();
        gradeManager.reload();

        loadStudentCombo();
        updateSelectedStudentTranscript();
        loadAllStudentSummaries();
        loadSubjectStatistics();
    }

    private void loadStudentCombo() {
        StudentItem prevSelection = (StudentItem) studentSelectCombo.getSelectedItem();
        studentSelectCombo.removeAllItems();

        for (Student s : studentManager.getAllStudents()) {
            studentSelectCombo.addItem(new StudentItem(s.getStudentId(), s.getFullName()));
        }

        if (prevSelection != null) {
            for (int i = 0; i < studentSelectCombo.getItemCount(); i++) {
                if (studentSelectCombo.getItemAt(i).id.equalsIgnoreCase(prevSelection.id)) {
                    studentSelectCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void updateSelectedStudentTranscript() {
        transcriptTableModel.setRowCount(0);
        StudentItem selected = (StudentItem) studentSelectCombo.getSelectedItem();

        if (selected == null) {
            creditsSummaryLabel.setText("Tổng tín chỉ tích lũy: 0");
            score10SummaryLabel.setText("Điểm trung bình (Hệ 10): 0.00");
            gpaSummaryLabel.setText("Điểm GPA (Thang 4.0): 0.00");
            rankSummaryLabel.setText("Xếp loại học lực: Chưa có dữ liệu");
            return;
        }

        List<String[]> transcript = reportService.getStudentTranscript(selected.id);
        for (String[] row : transcript) {
            transcriptTableModel.addRow(row);
        }

        int totalCredits = reportService.getTotalCredits(selected.id);
        double avg10 = reportService.calculateAverageScore10(selected.id);
        double gpa = reportService.calculateGPA(selected.id);
        String rank = totalCredits == 0 ? "Chưa có điểm" : reportService.classify(gpa);

        creditsSummaryLabel.setText("Tổng tín chỉ tích lũy: " + totalCredits);
        score10SummaryLabel.setText(String.format("Điểm trung bình (Hệ 10): %.2f", avg10));
        gpaSummaryLabel.setText(String.format("Điểm GPA (Thang 4.0): %.2f", gpa));
        rankSummaryLabel.setText("Xếp loại học lực: " + rank);
    }

    private void loadAllStudentSummaries() {
        allSummaryTableModel.setRowCount(0);
        for (String[] row : reportService.getAllStudentReports()) {
            allSummaryTableModel.addRow(row);
        }
    }

    private void sortAllSummariesByGpa() {
        java.util.List<String[]> reports = new java.util.ArrayList<>(reportService.getAllStudentReports());
        reports.sort((r1, r2) -> {
            double g1 = 0.0, g2 = 0.0;
            try { g1 = Double.parseDouble(r1[4]); } catch (Exception ignored) {}
            try { g2 = Double.parseDouble(r2[4]); } catch (Exception ignored) {}
            return Double.compare(g2, g1);
        });
        allSummaryTableModel.setRowCount(0);
        for (String[] row : reports) {
            allSummaryTableModel.addRow(row);
        }
    }

    private void loadSubjectStatistics() {
        subjectStatsTableModel.setRowCount(0);
        for (String[] row : reportService.getSubjectStatistics()) {
            subjectStatsTableModel.addRow(row);
        }
    }

    private void exportTranscriptToCsv() {
        StudentItem selected = (StudentItem) studentSelectCombo.getSelectedItem();
        String defaultFileName = selected != null ? "bang_diem_" + selected.id + ".csv" : "bang_diem.csv";
        exportTableToCsv(transcriptTableModel, defaultFileName, "Bảng điểm chi tiết sinh viên " + (selected != null ? selected.toString() : ""));
    }

    private void exportAllReportsToCsv() {
        exportTableToCsv(allSummaryTableModel, "bang_tong_hop_gpa.csv", "Bảng tổng hợp GPA toàn khóa");
    }

    private void exportTableToCsv(DefaultTableModel model, String defaultName, String title) {
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Không có dữ liệu để xuất CSV!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn vị trí lưu file CSV (" + title + ")");
        fileChooser.setSelectedFile(new File(defaultName));
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV files (*.csv)", "csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File fileToSave = fileChooser.getSelectedFile();
        if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
            fileToSave = new File(fileToSave.getAbsolutePath() + ".csv");
        }

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(fileToSave), StandardCharsets.UTF_8))) {
            // Write UTF-8 BOM
            writer.write('\ufeff');

            int colCount = model.getColumnCount();
            for (int col = 0; col < colCount; col++) {
                writer.write(escapeCsv(model.getColumnName(col)));
                if (col < colCount - 1) {
                    writer.write(",");
                }
            }
            writer.newLine();

            int rowCount = model.getRowCount();
            for (int row = 0; row < rowCount; row++) {
                for (int col = 0; col < colCount; col++) {
                    Object val = model.getValueAt(row, col);
                    writer.write(escapeCsv(val != null ? val.toString() : ""));
                    if (col < colCount - 1) {
                        writer.write(",");
                    }
                }
                writer.newLine();
            }

            JOptionPane.showMessageDialog(this,
                    "Xuất file CSV thành công!\nĐường dẫn: " + fileToSave.getAbsolutePath(),
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi xuất file CSV: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
