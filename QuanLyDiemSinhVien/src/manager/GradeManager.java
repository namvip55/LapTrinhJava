package manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import model.Grade;
import storage.FileStorage;

public class GradeManager {
    private final List<Grade> grades = new ArrayList<>();
    private final FileStorage fileStorage;
    private final String fileName = "data/grades.txt";

    public GradeManager() {
        fileStorage = new FileStorage();
        loadGrades();
    }

    public void reload() {
        grades.clear();
        loadGrades();
    }

    public List<Grade> getAllGrades() {
        return new ArrayList<>(grades);
    }

    public List<Grade> getGradesByStudent(String studentId) {
        List<Grade> result = new ArrayList<>();
        if (studentId == null) return result;
        for (Grade grade : grades) {
            if (grade.getStudentId().equalsIgnoreCase(studentId.trim())) {
                result.add(grade);
            }
        }
        return result;
    }

    public Grade findGrade(String studentId, String subjectId) {
        if (studentId == null || subjectId == null) return null;
        for (Grade grade : grades) {
            if (grade.getStudentId().equalsIgnoreCase(studentId.trim()) &&
                    grade.getSubjectId().equalsIgnoreCase(subjectId.trim())) {
                return grade;
            }
        }
        return null;
    }

    public void saveGrade(String studentId, String subjectId, double score) {
        if (studentId == null || subjectId == null) return;
        Grade existing = findGrade(studentId, subjectId);

        if (existing != null) {
            existing.setScore(score);
        } else {
            grades.add(new Grade(studentId.trim(), subjectId.trim(), score));
        }

        saveGrades();
    }

    public boolean deleteGrade(String studentId, String subjectId) {
        Grade grade = findGrade(studentId, subjectId);
        if (grade == null) {
            return false;
        }

        grades.remove(grade);
        saveGrades();
        return true;
    }

    public void deleteGradesByStudent(String studentId) {
        if (studentId == null) return;
        boolean removed = grades.removeIf(g -> g.getStudentId().equalsIgnoreCase(studentId.trim()));
        if (removed) {
            saveGrades();
        }
    }

    public void deleteGradesBySubject(String subjectId) {
        if (subjectId == null) return;
        boolean removed = grades.removeIf(g -> g.getSubjectId().equalsIgnoreCase(subjectId.trim()));
        if (removed) {
            saveGrades();
        }
    }

    private void loadGrades() {
        try {
            List<String> lines = fileStorage.readLines(fileName);

            for (String line : lines) {
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 3) {
                    String studentId = parts[0].trim();
                    String subjectId = parts[1].trim();
                    double score = Double.parseDouble(parts[2].trim());

                    grades.add(new Grade(studentId, subjectId, score));
                }
            }
        } catch (Exception e) {
            System.err.println("Không thể đọc tệp grades.txt: " + e.getMessage());
        }
    }

    private void saveGrades() {
        List<String> lines = new ArrayList<>();

        for (Grade grade : grades) {
            lines.add(String.join("|",
                    grade.getStudentId(),
                    grade.getSubjectId(),
                    String.valueOf(grade.getScore())
            ));
        }

        try {
            fileStorage.writeLines(fileName, lines);
        } catch (IOException e) {
            System.err.println("Không thể ghi tệp grades.txt: " + e.getMessage());
        }
    }
}
