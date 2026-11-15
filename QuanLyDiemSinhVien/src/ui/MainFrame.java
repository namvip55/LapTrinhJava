package ui;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

public class MainFrame extends JFrame {
    public MainFrame() {
        setTitle("Hệ thống quản lý điểm sinh viên - D19CNPM3 - EPU");
        setSize(1020, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        StudentPanel studentPanel = new StudentPanel();
        SubjectPanel subjectPanel = new SubjectPanel();
        GradePanel gradePanel = new GradePanel();
        ReportPanel reportPanel = new ReportPanel();

        tabbedPane.addTab("Sinh viên", studentPanel);
        tabbedPane.addTab("Môn học", subjectPanel);
        tabbedPane.addTab("Điểm", gradePanel);
        tabbedPane.addTab("Báo cáo & Thống kê", reportPanel);

        tabbedPane.addChangeListener(e -> {
            Component selected = tabbedPane.getSelectedComponent();
            if (selected == studentPanel) {
                studentPanel.refreshData();
            } else if (selected == subjectPanel) {
                subjectPanel.refreshData();
            } else if (selected == gradePanel) {
                gradePanel.refreshData();
            } else if (selected == reportPanel) {
                reportPanel.refreshData();
            }
        });

        add(tabbedPane, BorderLayout.CENTER);
    }
}
