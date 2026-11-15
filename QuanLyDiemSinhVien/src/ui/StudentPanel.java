package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import manager.StudentManager;
import model.Student;
import util.InputValidator;

public class StudentPanel extends JPanel {
    private final StudentManager studentManager;
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JTextField idField;
    private final JTextField nameField;
    private final JTextField dateField;
    private final JComboBox<String> genderCombo;
    private final JTextField classField;
    private final JTextField emailField;
    private final JTextField searchField;

    public StudentPanel() {
        studentManager = new StudentManager();
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        idField = new JTextField(8);
        nameField = new JTextField(12);
        dateField = new JTextField(8);
        genderCombo = new JComboBox<>(new String[]{"Nam", "Nữ", "Khác"});
        classField = new JTextField(8);
        emailField = new JTextField(15);

        addFormField(formPanel, gbc, 0, 0, new JLabel("Mã SV:"), idField);
        addFormField(formPanel, gbc, 2, 0, new JLabel("Họ tên:"), nameField);
        addFormField(formPanel, gbc, 4, 0, new JLabel("Ngày sinh (yyyy-MM-dd):"), dateField);
        addFormField(formPanel, gbc, 0, 1, new JLabel("Giới tính:"), genderCombo);
        addFormField(formPanel, gbc, 2, 1, new JLabel("Lớp:"), classField);
        addFormField(formPanel, gbc, 4, 1, new JLabel("Email:"), emailField);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(15);
        JButton searchButton = new JButton("Tìm kiếm");
        JButton refreshButton = new JButton("Làm mới");
        JButton sortIdButton = new JButton("Sắp xếp theo Mã SV");
        JButton sortNameButton = new JButton("Sắp xếp Tên A-Z");

        searchPanel.add(new JLabel("Tìm kiếm (Mã/Tên):"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(refreshButton);
        searchPanel.add(sortIdButton);
        searchPanel.add(sortNameButton);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(formPanel, BorderLayout.NORTH);
        topPanel.add(searchPanel, BorderLayout.SOUTH);

        tableModel = new DefaultTableModel(
                new Object[]{"Mã SV", "Họ tên", "Ngày sinh", "Giới tính", "Lớp", "Email"}, 0) {
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

        addButton.addActionListener(e -> addStudent());
        updateButton.addActionListener(e -> updateStudent());
        deleteButton.addActionListener(e -> deleteStudent());
        searchButton.addActionListener(e -> searchStudents());
        refreshButton.addActionListener(e -> {
            clearForm();
            loadStudents();
        });
        sortIdButton.addActionListener(e -> sortStudentsById());
        sortNameButton.addActionListener(e -> sortStudentsByName());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                fillForm();
            }
        });

        loadStudents();
    }

    public void refreshData() {
        studentManager.reload();
        loadStudents();
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

    private Student readForm() {
        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        String date = dateField.getText().trim();
        String gender = genderCombo.getSelectedItem() != null ? genderCombo.getSelectedItem().toString() : "Nam";
        String className = classField.getText().trim();
        String email = emailField.getText().trim();

        if (!InputValidator.isNotEmpty(id) || !InputValidator.isNotEmpty(name) ||
                !InputValidator.isNotEmpty(className) || !InputValidator.isNotEmpty(email)) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng điền đầy đủ tất cả các trường dữ liệu!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        if (!InputValidator.isValidDate(date)) {
            JOptionPane.showMessageDialog(this,
                    "Ngày sinh không hợp lệ! Vui lòng nhập theo định dạng yyyy-MM-dd.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        if (!InputValidator.isValidEmail(email)) {
            JOptionPane.showMessageDialog(this,
                    "Địa chỉ email không đúng định dạng!",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        return new Student(id, name, LocalDate.parse(date), gender, className, email);
    }

    private void addStudent() {
        Student student = readForm();
        if (student == null) {
            return;
        }

        if (!studentManager.addStudent(student)) {
            JOptionPane.showMessageDialog(this,
                    "Mã sinh viên \"" + student.getStudentId() + "\" đã tồn tại trong hệ thống!",
                    "Lỗi trùng lặp", JOptionPane.ERROR_MESSAGE);
            return;
        }

        loadStudents();
        clearForm();
        JOptionPane.showMessageDialog(this,
                "Thêm mới sinh viên thành công!",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateStudent() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn sinh viên cần sửa trong bảng!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Student student = readForm();
        if (student == null) {
            return;
        }

        if (!studentManager.updateStudent(student)) {
            JOptionPane.showMessageDialog(this,
                    "Không tìm thấy sinh viên có mã: " + student.getStudentId(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        loadStudents();
        clearForm();
        JOptionPane.showMessageDialog(this,
                "Cập nhật thông tin sinh viên thành công!",
                "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void deleteStudent() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn sinh viên cần xóa trong bảng!",
                    "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String id = tableModel.getValueAt(selectedRow, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc chắn muốn xóa sinh viên có mã: " + id + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            studentManager.deleteStudent(id);
            new manager.GradeManager().deleteGradesByStudent(id);
            loadStudents();
            clearForm();
            JOptionPane.showMessageDialog(this,
                    "Xóa sinh viên và toàn bộ điểm liên quan thành công!",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void searchStudents() {
        String keyword = searchField.getText().trim();
        List<Student> results = studentManager.searchByNameOrId(keyword);
        displayStudents(results);
    }

    private void sortStudentsByName() {
        List<Student> sorted = studentManager.getStudentsSortedByName();
        displayStudents(sorted);
    }

    private void sortStudentsById() {
        List<Student> sorted = studentManager.getStudentsSortedById();
        displayStudents(sorted);
    }

    private void loadStudents() {
        displayStudents(studentManager.getAllStudents());
    }

    private void displayStudents(List<Student> list) {
        tableModel.setRowCount(0);
        for (Student student : list) {
            tableModel.addRow(new Object[]{
                    student.getStudentId(),
                    student.getFullName(),
                    student.getDateOfBirth(),
                    student.getGender(),
                    student.getClassName(),
                    student.getEmail()
            });
        }
    }

    private void fillForm() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            idField.setText(tableModel.getValueAt(selectedRow, 0).toString());
            nameField.setText(tableModel.getValueAt(selectedRow, 1).toString());
            dateField.setText(tableModel.getValueAt(selectedRow, 2).toString());
            genderCombo.setSelectedItem(tableModel.getValueAt(selectedRow, 3).toString());
            classField.setText(tableModel.getValueAt(selectedRow, 4).toString());
            emailField.setText(tableModel.getValueAt(selectedRow, 5).toString());
            idField.setEditable(false);
        }
    }

    private void clearForm() {
        idField.setText("");
        nameField.setText("");
        dateField.setText("");
        genderCombo.setSelectedIndex(0);
        classField.setText("");
        emailField.setText("");
        searchField.setText("");
        idField.setEditable(true);
        table.clearSelection();
    }
}
