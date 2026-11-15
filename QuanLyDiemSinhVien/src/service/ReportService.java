package service;

import java.util.ArrayList;
import java.util.List;

import manager.GradeManager;
import manager.StudentManager;
import manager.SubjectManager;
import model.Grade;
import model.Student;
import model.Subject;

public class ReportService {
    private final StudentManager studentManager;
    private final SubjectManager subjectManager;
    private final GradeManager gradeManager;

    public ReportService(StudentManager studentManager,
                         SubjectManager subjectManager,
                         GradeManager gradeManager) {
        this.studentManager = studentManager;
        this.subjectManager = subjectManager;
        this.gradeManager = gradeManager;
    }

    /**
     * Quy đổi điểm số thang 10 sang thang điểm 4 theo Quy chế đào tạo đại học tín chỉ:
     * 8.5 - 10.0: 4.0 (A)
     * 8.0 - 8.4:  3.5 (B+)
     * 7.0 - 7.9:  3.0 (B)
     * 6.5 - 6.9:  2.5 (C+)
     * 5.5 - 6.4:  2.0 (C)
     * 5.0 - 5.4:  1.5 (D+)
     * 4.0 - 4.9:  1.0 (D)
     * < 4.0:      0.0 (F)
     */
    public static double convertToScale4(double score10) {
        if (score10 >= 8.5) return 4.0;
        if (score10 >= 8.0) return 3.5;
        if (score10 >= 7.0) return 3.0;
        if (score10 >= 6.5) return 2.5;
        if (score10 >= 5.5) return 2.0;
        if (score10 >= 5.0) return 1.5;
        if (score10 >= 4.0) return 1.0;
        return 0.0;
    }

    /**
     * Quy đổi điểm số thang 10 sang Điểm chữ (Letter Grade)
     */
    public static String convertToLetterGrade(double score10) {
        if (score10 >= 8.5) return "A";
        if (score10 >= 8.0) return "B+";
        if (score10 >= 7.0) return "B";
        if (score10 >= 6.5) return "C+";
        if (score10 >= 5.5) return "C";
        if (score10 >= 5.0) return "D+";
        if (score10 >= 4.0) return "D";
        return "F";
    }

    /**
     * Tính điểm GPA theo Thang điểm 4 (kết quả <= 4.0) theo trọng số tín chỉ chuẩn quy chế tín chỉ.
     */
    public double calculateGPA(String studentId) {
        if (studentId == null) return 0.0;
        double totalWeightedScale4 = 0.0;
        int totalCredits = 0;

        for (Grade grade : gradeManager.getAllGrades()) {
            if (grade.getStudentId().equalsIgnoreCase(studentId.trim())) {
                Subject subject = subjectManager.findById(grade.getSubjectId());
                if (subject != null) {
                    double score4 = convertToScale4(grade.getScore());
                    totalWeightedScale4 += score4 * subject.getCredits();
                    totalCredits += subject.getCredits();
                }
            }
        }

        if (totalCredits == 0) {
            return 0.0;
        }

        double gpa = totalWeightedScale4 / totalCredits;
        return Math.round(gpa * 100.0) / 100.0;
    }

    /**
     * Tính điểm trung bình tích lũy theo thang điểm 10 (ĐTB hệ 10)
     */
    public double calculateAverageScore10(String studentId) {
        if (studentId == null) return 0.0;
        double totalWeightedScore = 0.0;
        int totalCredits = 0;

        for (Grade grade : gradeManager.getAllGrades()) {
            if (grade.getStudentId().equalsIgnoreCase(studentId.trim())) {
                Subject subject = subjectManager.findById(grade.getSubjectId());
                if (subject != null) {
                    totalWeightedScore += grade.getScore() * subject.getCredits();
                    totalCredits += subject.getCredits();
                }
            }
        }

        if (totalCredits == 0) {
            return 0.0;
        }

        double avg10 = totalWeightedScore / totalCredits;
        return Math.round(avg10 * 100.0) / 100.0;
    }

    public int getTotalCredits(String studentId) {
        if (studentId == null) return 0;
        int totalCredits = 0;

        for (Grade grade : gradeManager.getAllGrades()) {
            if (grade.getStudentId().equalsIgnoreCase(studentId.trim())) {
                Subject subject = subjectManager.findById(grade.getSubjectId());
                if (subject != null) {
                    totalCredits += subject.getCredits();
                }
            }
        }

        return totalCredits;
    }

    /**
     * Xếp loại học lực theo GPA Thang 4 (Quy chế đào tạo đại học tín chỉ):
     * 3.60 - 4.00: Xuất sắc
     * 3.20 - 3.59: Giỏi
     * 2.50 - 3.19: Khá
     * 2.00 - 2.49: Trung bình
     * 1.00 - 1.99: Yếu
     * < 1.00:      Kém
     */
    public String classify(double gpa) {
        if (gpa >= 3.60) {
            return "Xuất sắc";
        }
        if (gpa >= 3.20) {
            return "Giỏi";
        }
        if (gpa >= 2.50) {
            return "Khá";
        }
        if (gpa >= 2.00) {
            return "Trung bình";
        }
        if (gpa >= 1.00) {
            return "Yếu";
        }
        return "Kém";
    }

    public List<String[]> getStudentTranscript(String studentId) {
        List<String[]> transcript = new ArrayList<>();
        if (studentId == null) return transcript;

        for (Grade grade : gradeManager.getAllGrades()) {
            if (grade.getStudentId().equalsIgnoreCase(studentId.trim())) {
                Subject subject = subjectManager.findById(grade.getSubjectId());
                String subjectName = subject != null ? subject.getSubjectName() : "Không xác định";
                String credits = subject != null ? String.valueOf(subject.getCredits()) : "0";
                double score10 = grade.getScore();
                String letter = convertToLetterGrade(score10);
                double score4 = convertToScale4(score10);
                String status = score10 >= 4.0 ? "Đạt" : "Học lại";

                transcript.add(new String[]{
                        grade.getSubjectId(),
                        subjectName,
                        credits,
                        String.format("%.2f", score10),
                        letter,
                        String.format("%.1f", score4),
                        status
                });
            }
        }

        return transcript;
    }

    public List<String[]> getSubjectStatistics() {
        List<String[]> stats = new ArrayList<>();

        for (Subject subject : subjectManager.getAllSubjects()) {
            double totalScore = 0.0;
            int count = 0;

            for (Grade grade : gradeManager.getAllGrades()) {
                if (grade.getSubjectId().equalsIgnoreCase(subject.getSubjectId())) {
                    totalScore += grade.getScore();
                    count++;
                }
            }

            double avgScore = count == 0 ? 0.0 : Math.round((totalScore / count) * 100.0) / 100.0;

            stats.add(new String[]{
                    subject.getSubjectId(),
                    subject.getSubjectName(),
                    String.valueOf(subject.getCredits()),
                    String.valueOf(count),
                    String.format("%.2f", avgScore)
            });
        }

        return stats;
    }

    public List<String[]> getAllStudentReports() {
        List<String[]> reports = new ArrayList<>();

        for (Student student : studentManager.getAllStudents()) {
            int totalCredits = getTotalCredits(student.getStudentId());
            double gpa = calculateGPA(student.getStudentId());
            double avg10 = calculateAverageScore10(student.getStudentId());
            String rank = totalCredits == 0 ? "Chưa có điểm" : classify(gpa);

            reports.add(new String[]{
                    student.getStudentId(),
                    student.getFullName(),
                    String.valueOf(totalCredits),
                    String.format("%.2f", avg10),
                    String.format("%.2f", gpa),
                    rank
            });
        }

        return reports;
    }
}
