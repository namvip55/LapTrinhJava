package model;

import java.text.Collator;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Locale;

public class Student implements Comparable<Student> {
    private String studentId;
    private String fullName;
    private LocalDate dateOfBirth;
    private String gender;
    private String className;
    private String email;
    private double gpa = 0.0;

    public static final Comparator<Student> GPA_DESCENDING_COMPARATOR = (s1, s2) -> {
        if (s1 == null && s2 == null) return 0;
        if (s1 == null) return 1;
        if (s2 == null) return -1;
        return Double.compare(s2.getGpa(), s1.getGpa());
    };

    public static final Comparator<Student> VIETNAMESE_NAME_COMPARATOR = (s1, s2) -> {
        if (s1 == null && s2 == null) return 0;
        if (s1 == null) return 1;
        if (s2 == null) return -1;
        Collator collator = Collator.getInstance(Locale.forLanguageTag("vi-VN"));
        String name1 = extractLastName(s1.getFullName());
        String name2 = extractLastName(s2.getFullName());
        int res = collator.compare(name1, name2);
        if (res != 0) {
            return res;
        }
        return collator.compare(s1.getFullName(), s2.getFullName());
    };

    public static String extractLastName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "";
        }
        String[] parts = fullName.trim().split("\\s+");
        return parts[parts.length - 1];
    }

    public Student() {
    }

    public Student(String studentId, String fullName, LocalDate dateOfBirth,
                   String gender, String className, String email) {
        this(studentId, fullName, dateOfBirth, gender, className, email, 0.0);
    }

    public Student(String studentId, String fullName, LocalDate dateOfBirth,
                   String gender, String className, String email, double gpa) {
        this.studentId = studentId;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.className = className;
        this.email = email;
        this.gpa = gpa;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getGpa() {
        return gpa;
    }

    public void setGpa(double gpa) {
        this.gpa = gpa;
    }

    @Override
    public int compareTo(Student o) {
        if (o == null) return 1;
        return this.studentId.compareToIgnoreCase(o.studentId);
    }

    @Override
    public String toString() {
        return studentId + " - " + fullName;
    }
}
