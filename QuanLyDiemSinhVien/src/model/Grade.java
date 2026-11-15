package model;

public class Grade {
    private String studentId;
    private String subjectId;
    private double score;

    public Grade() {
    }

    public Grade(String studentId, String subjectId, double score) {
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.score = score;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public boolean isValidScore() {
        return this.score >= 0.0 && this.score <= 10.0;
    }
}
