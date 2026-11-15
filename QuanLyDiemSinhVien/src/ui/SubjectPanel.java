package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import manager.SubjectManager;
import model.Subject;
import util.InputValidator;

public class SubjectPanel extends JPanel {
    private final SubjectManager subjectManager;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField idField;
    private final JTextField nameField;
    private final JTextField creditsField;
    private final JTextField searchField;

    public SubjectPanel() {
        subjectManager = new SubjectManager();
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        idField = new JTextField(10);
        nameField = new JTextField(18);
        creditsField = new JTextField(8);

        addFormField(formPanel, gbc, 0, 0, new JLabel("Mã môn:"), idField);
        addFormField(formPanel, gbc, 2, 0, new JLabel("Tên môn:"), nameField);
        addFormField(formPanel, gbc, 4, 0, new JLabel("Số tín chỉ (1 - 6):"), creditsField);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(15);
        JButton searchButton = new JButton("Tìm kiếm");
        JButton refreshButton = new JButton("Làm mới");

        searchPanel.add(new JLabel("Tìm kiếm (Mã/Tên môn):"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        tableModel = new DefaultTableModel(
                new Object[]{"Mã môn", "Tên môn", "Số tín chỉ"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Thêm");
        JButton updateButton = new JButton("Sửa");
        JButton deleteButton = new JButton("Xóa");

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addSubject());
        updateButton.addActionListener(e -> updateSubject());
        deleteButton.addActionListener(e -> deleteSubject());
        searchButton.addActionListener(e -> searchSubjects());
        refreshButton.addActionListener(e -> {
            clearForm();
            loadSubjects();
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillForm();
            }
        });

        loadSubjects();
    }

    public void refreshData() {
        subjectManager.reload();
        loadSubjects();
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

    private Subject readForm() {
        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        String creditsText = creditsField.getText().trim();

        if (!InputValidator.isNotEmpty(id) || !InputValidator.isNotEmpty(name) || !InputValidator.isNotEmpty(creditsText)) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng điền đầy đủ các thông tin môn học!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        int credits;
        try {
            credits = Integer.parseInt(creditsText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Số tín chỉ phải là một số nguyên!",
                    "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        if (!InputValidator.isValidCredits(credits)) {
            JOptionPane.showMessageDialog(this,
                    "Số tín chỉ không hợp lệ! Quy định từ 1 đến 6 tín chỉ.",
                    "Lỗi ràng buộc", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        return new Subject(id, name, credits);
    }

    private void addSubject() {
        Subject subject = readForm();
        if (subject == null) {
            return;
        }

        if (!subjectManager.addSubject(subject)) {
            JOptionPane.showMessageDialog(this,
                    "Mã môn học \"" + subject.getSubjectId() + "\" đã tồn tại trong hệ thống!",
                    "Lỗi trùng lặp", JOptionPane.ERROR_MESSAGE);
            return;
        }

        loadSubjects();
        clearForm();
        JOptionPane.showMessageDialog(this,
                "Thêm môn học thành công!",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateSubject() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn môn học cần sửa trong bảng!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Subject subject = readForm();
        if (subject == null) {
            return;
        }

        if (!subjectManager.updateSubject(subject)) {
            JOptionPane.showMessageDialog(this,
                    "Không tìm thấy môn học có mã: " + subject.getSubjectId(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        loadSubjects();
        clearForm();
        JOptionPane.showMessageDialog(this,
                "Cập nhật môn học thành công!",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteSubject() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn môn học cần xóa trong bảng!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String id = tableModel.getValueAt(selectedRow, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa môn học có mã: " + id + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            subjectManager.deleteSubject(id);
            new manager.GradeManager().deleteGradesBySubject(id);
            loadSubjects();
            clearForm();
            JOptionPane.showMessageDialog(this,
                    "Xóa môn học và các điểm liên quan thành công!",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void searchSubjects() {
        String keyword = searchField.getText().trim();
        List<Subject> results = subjectManager.searchByNameOrId(keyword);
        displaySubjects(results);
    }

    private void loadSubjects() {
        displaySubjects(subjectManager.getAllSubjects());
    }

    private void displaySubjects(List<Subject> list) {
        tableModel.setRowCount(0);
        for (Subject subject : list) {
            tableModel.addRow(new Object[]{
                    subject.getSubjectId(),
                    subject.getSubjectName(),
                    subject.getCredits()
            });
        }
    }

    private void fillForm() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            idField.setText(tableModel.getValueAt(selectedRow, 0).toString());
            nameField.setText(tableModel.getValueAt(selectedRow, 1).toString());
            creditsField.setText(tableModel.getValueAt(selectedRow, 2).toString());
            idField.setEditable(false);
        }
    }

    private void clearForm() {
        idField.setText("");
        nameField.setText("");
        creditsField.setText("");
        searchField.setText("");
        idField.setEditable(true);
        table.clearSelection();
    }
}
