package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import manager.GradeManager;
import manager.StudentManager;
import manager.SubjectManager;
import model.Grade;
import model.Student;
import model.Subject;
import util.InputValidator;

public class GradePanel extends JPanel {
    private final GradeManager gradeManager;
    private final StudentManager studentManager;
    private final SubjectManager subjectManager;

    private final JComboBox<StudentItem> studentCombo;
    private final JComboBox<SubjectItem> subjectCombo;
    private final JTextField scoreField;
    private final DefaultTableModel tableModel;
    private final JTable table;

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

    private static class SubjectItem {
        final String id;
        final String name;

        SubjectItem(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return id + " - " + name;
        }
    }

    public GradePanel() {
        gradeManager = new GradeManager();
        studentManager = new StudentManager();
        subjectManager = new SubjectManager();

        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        studentCombo = new JComboBox<>();
        subjectCombo = new JComboBox<>();
        scoreField = new JTextField(8);

        addFormField(formPanel, gbc, 0, 0, new JLabel("Sinh viên:"), studentCombo);
        addFormField(formPanel, gbc, 2, 0, new JLabel("Môn học:"), subjectCombo);
        addFormField(formPanel, gbc, 4, 0, new JLabel("Điểm số [0.0 - 10.0]:"), scoreField);

        tableModel = new DefaultTableModel(
                new Object[]{"Mã SV", "Họ tên sinh viên", "Mã môn", "Tên môn học", "Số tín chỉ", "Điểm số"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton saveButton = new JButton("Lưu điểm");
        JButton deleteButton = new JButton("Xóa điểm");
        JButton refreshButton = new JButton("Làm mới");
        JButton exportCsvButton = new JButton("Xuất file CSV");

        buttonPanel.add(saveButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(exportCsvButton);

        add(formPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        saveButton.addActionListener(e -> saveGrade());
        deleteButton.addActionListener(e -> deleteGrade());
        refreshButton.addActionListener(e -> refreshData());
        exportCsvButton.addActionListener(e -> exportToCsv());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillFormFromTable();
            }
        });

        loadComboBoxes();
        loadGrades();
    }

    public void refreshData() {
        studentManager.reload();
        subjectManager.reload();
        gradeManager.reload();
        loadComboBoxes();
        loadGrades();
    }

    private void addFormField(JPanel panel, GridBagConstraints gbc, int x, int y, JLabel label, java.awt.Component field) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.weightx = 0;
        gbc.gridwidth = 1;
        panel.add(label, gbc);

        gbc.gridx = x + 1;
        gbc.weightx = 1.0;
        panel.add(field, gbc);
    }

    private void loadComboBoxes() {
        StudentItem currentStudent = (StudentItem) studentCombo.getSelectedItem();
        SubjectItem currentSubject = (SubjectItem) subjectCombo.getSelectedItem();

        studentCombo.removeAllItems();
        for (Student s : studentManager.getAllStudents()) {
            studentCombo.addItem(new StudentItem(s.getStudentId(), s.getFullName()));
        }

        subjectCombo.removeAllItems();
        for (Subject sub : subjectManager.getAllSubjects()) {
            subjectCombo.addItem(new SubjectItem(sub.getSubjectId(), sub.getSubjectName()));
        }

        if (currentStudent != null) {
            for (int i = 0; i < studentCombo.getItemCount(); i++) {
                if (studentCombo.getItemAt(i).id.equalsIgnoreCase(currentStudent.id)) {
                    studentCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        if (currentSubject != null) {
            for (int i = 0; i < subjectCombo.getItemCount(); i++) {
                if (subjectCombo.getItemAt(i).id.equalsIgnoreCase(currentSubject.id)) {
                    subjectCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void loadGrades() {
        tableModel.setRowCount(0);
        for (Grade grade : gradeManager.getAllGrades()) {
            Student s = studentManager.findById(grade.getStudentId());
            Subject sub = subjectManager.findById(grade.getSubjectId());

            String studentName = s != null ? s.getFullName() : "Không xác định";
            String subjectName = sub != null ? sub.getSubjectName() : "Không xác định";
            String credits = sub != null ? String.valueOf(sub.getCredits()) : "0";

            tableModel.addRow(new Object[]{
                    grade.getStudentId(),
                    studentName,
                    grade.getSubjectId(),
                    subjectName,
                    credits,
                    String.format("%.2f", grade.getScore())
            });
        }
    }

    private void saveGrade() {
        StudentItem selectedStudent = (StudentItem) studentCombo.getSelectedItem();
        SubjectItem selectedSubject = (SubjectItem) subjectCombo.getSelectedItem();
        String scoreText = scoreField.getText().trim();

        if (selectedStudent == null || selectedSubject == null) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn Sinh viên và Môn học hợp lệ!",
                    "Thiếu dữ liệu", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!InputValidator.isNotEmpty(scoreText)) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng nhập điểm số!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double score;
        try {
            score = Double.parseDouble(scoreText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Điểm số phải là một số thực!",
                    "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!InputValidator.isValidScore(score)) {
            JOptionPane.showMessageDialog(this,
                    "Điểm số không hợp lệ! Thang điểm quy định từ 0.0 đến 10.0.",
                    "Lỗi ràng buộc", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Tự động ghi đè nếu đã tồn tại cặp (maSV, maMon)
        gradeManager.saveGrade(selectedStudent.id, selectedSubject.id, score);
        loadGrades();
        scoreField.setText("");
        JOptionPane.showMessageDialog(this,
                "Lưu điểm học phần thành công!",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteGrade() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn bản ghi điểm cần xóa trong bảng!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String studentId = tableModel.getValueAt(selectedRow, 0).toString();
        String subjectId = tableModel.getValueAt(selectedRow, 2).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa điểm của sinh viên " + studentId + " ở môn " + subjectId + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            gradeManager.deleteGrade(studentId, subjectId);
            loadGrades();
            scoreField.setText("");
            JOptionPane.showMessageDialog(this,
                    "Xóa điểm thành công!",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void fillFormFromTable() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            String studentId = tableModel.getValueAt(selectedRow, 0).toString();
            String subjectId = tableModel.getValueAt(selectedRow, 2).toString();
            String score = tableModel.getValueAt(selectedRow, 5).toString();

            for (int i = 0; i < studentCombo.getItemCount(); i++) {
                if (studentCombo.getItemAt(i).id.equalsIgnoreCase(studentId)) {
                    studentCombo.setSelectedIndex(i);
                    break;
                }
            }

            for (int i = 0; i < subjectCombo.getItemCount(); i++) {
                if (subjectCombo.getItemAt(i).id.equalsIgnoreCase(subjectId)) {
                    subjectCombo.setSelectedIndex(i);
                    break;
                }
            }

            scoreField.setText(score);
        }
    }

    private void exportToCsv() {
        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Bảng hiện không có dữ liệu để xuất CSV.",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Chọn vị trí lưu file CSV");
        fileChooser.setSelectedFile(new File("danh_sach_diem.csv"));
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
            // Write UTF-8 BOM so Excel opens Vietnamese characters correctly
            writer.write('\ufeff');

            int colCount = tableModel.getColumnCount();
            for (int col = 0; col < colCount; col++) {
                writer.write(escapeCsv(tableModel.getColumnName(col)));
                if (col < colCount - 1) {
                    writer.write(",");
                }
            }
            writer.newLine();

            int rowCount = tableModel.getRowCount();
            for (int row = 0; row < rowCount; row++) {
                for (int col = 0; col < colCount; col++) {
                    Object val = tableModel.getValueAt(row, col);
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
