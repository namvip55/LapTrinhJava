package manager;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import model.Student;
import storage.FileStorage;

public class StudentManager {
    private final List<Student> students = new ArrayList<>();
    private final FileStorage fileStorage;
    private final String fileName = "data/students.txt";

    public StudentManager() {
        fileStorage = new FileStorage();
        loadStudents();
    }

    public void reload() {
        students.clear();
        loadStudents();
    }

    public List<Student> getAllStudents() {
        return new ArrayList<>(students);
    }

    public Student findById(String studentId) {
        if (studentId == null) return null;
        for (Student student : students) {
            if (student.getStudentId().equalsIgnoreCase(studentId.trim())) {
                return student;
            }
        }
        return null;
    }

    public boolean addStudent(Student student) {
        if (student == null || findById(student.getStudentId()) != null) {
            return false;
        }

        students.add(student);
        saveStudents();
        return true;
    }

    public boolean updateStudent(Student student) {
        if (student == null) {
            return false;
        }

        Student existing = findById(student.getStudentId());
        if (existing == null) {
            return false;
        }

        existing.setFullName(student.getFullName());
        existing.setDateOfBirth(student.getDateOfBirth());
        existing.setGender(student.getGender());
        existing.setClassName(student.getClassName());
        existing.setEmail(student.getEmail());

        saveStudents();
        return true;
    }

    public boolean deleteStudent(String studentId) {
        Student student = findById(studentId);
        if (student == null) {
            return false;
        }

        students.remove(student);
        saveStudents();
        return true;
    }

    public List<Student> searchByNameOrId(String keyword) {
        List<Student> results = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllStudents();
        }

        String lowerKeyword = keyword.trim().toLowerCase();
        for (Student student : students) {
            if (student.getStudentId().toLowerCase().contains(lowerKeyword) ||
                    student.getFullName().toLowerCase().contains(lowerKeyword)) {
                results.add(student);
            }
        }

        return results;
    }

    public List<Student> getStudentsSortedByName() {
        List<Student> sortedList = new ArrayList<>(students);
        sortedList.sort(Student.VIETNAMESE_NAME_COMPARATOR);
        return sortedList;
    }

    public List<Student> getStudentsSortedById() {
        List<Student> sortedList = new ArrayList<>(students);
        Collections.sort(sortedList);
        return sortedList;
    }

    private void loadStudents() {
        try {
            List<String> lines = fileStorage.readLines(fileName);

            for (String line : lines) {
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 6) {
                    String id = parts[0].trim();
                    String name = parts[1].trim();
                    LocalDate dob = LocalDate.parse(parts[2].trim());
                    String gender = parts[3].trim();
                    String className = parts[4].trim();
                    String email = parts[5].trim();

                    students.add(new Student(id, name, dob, gender, className, email));
                }
            }
        } catch (Exception e) {
            System.err.println("Không thể đọc tệp students.txt: " + e.getMessage());
        }
    }

    private void saveStudents() {
        List<String> lines = new ArrayList<>();

        for (Student student : students) {
            lines.add(String.join("|",
                    student.getStudentId(),
                    student.getFullName(),
                    student.getDateOfBirth().toString(),
                    student.getGender(),
                    student.getClassName(),
                    student.getEmail()
            ));
        }

        try {
            fileStorage.writeLines(fileName, lines);
        } catch (IOException e) {
            System.err.println("Không thể ghi tệp students.txt: " + e.getMessage());
        }
    }
}
