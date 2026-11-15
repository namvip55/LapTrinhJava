package manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import model.Subject;
import storage.FileStorage;

public class SubjectManager {
    private final List<Subject> subjects = new ArrayList<>();
    private final FileStorage fileStorage;
    private final String fileName = "data/subjects.txt";

    public SubjectManager() {
        fileStorage = new FileStorage();
        loadSubjects();
    }

    public void reload() {
        subjects.clear();
        loadSubjects();
    }

    public List<Subject> getAllSubjects() {
        return new ArrayList<>(subjects);
    }

    public Subject findById(String subjectId) {
        if (subjectId == null) return null;
        for (Subject subject : subjects) {
            if (subject.getSubjectId().equalsIgnoreCase(subjectId.trim())) {
                return subject;
            }
        }
        return null;
    }

    public boolean addSubject(Subject subject) {
        if (subject == null || findById(subject.getSubjectId()) != null) {
            return false;
        }

        subjects.add(subject);
        saveSubjects();
        return true;
    }

    public boolean updateSubject(Subject subject) {
        if (subject == null) {
            return false;
        }

        Subject existing = findById(subject.getSubjectId());
        if (existing == null) {
            return false;
        }

        existing.setSubjectName(subject.getSubjectName());
        existing.setCredits(subject.getCredits());

        saveSubjects();
        return true;
    }

    public boolean deleteSubject(String subjectId) {
        Subject subject = findById(subjectId);
        if (subject == null) {
            return false;
        }

        subjects.remove(subject);
        saveSubjects();
        return true;
    }

    public List<Subject> searchByNameOrId(String keyword) {
        List<Subject> results = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllSubjects();
        }

        String lower = keyword.trim().toLowerCase();
        for (Subject s : subjects) {
            if (s.getSubjectId().toLowerCase().contains(lower) ||
                    s.getSubjectName().toLowerCase().contains(lower)) {
                results.add(s);
            }
        }
        return results;
    }

    private void loadSubjects() {
        try {
            List<String> lines = fileStorage.readLines(fileName);

            for (String line : lines) {
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 3) {
                    String id = parts[0].trim();
                    String name = parts[1].trim();
                    int credits = Integer.parseInt(parts[2].trim());

                    subjects.add(new Subject(id, name, credits));
                }
            }
        } catch (Exception e) {
            System.err.println("Không thể đọc tệp subjects.txt: " + e.getMessage());
        }
    }

    private void saveSubjects() {
        List<String> lines = new ArrayList<>();

        for (Subject subject : subjects) {
            lines.add(String.join("|",
                    subject.getSubjectId(),
                    subject.getSubjectName(),
                    String.valueOf(subject.getCredits())
            ));
        }

        try {
            fileStorage.writeLines(fileName, lines);
        } catch (IOException e) {
            System.err.println("Không thể ghi tệp subjects.txt: " + e.getMessage());
        }
    }
}
