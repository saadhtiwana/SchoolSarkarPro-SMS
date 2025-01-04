class Course implements Serializable {
    // ... existing code ...

    private List<Teacher> assignedTeachers; // Change from a single Teacher to a list of Teachers

    public Course(int id, String title, int credit) {
        this.courseID = id;
        this.title = title;
        this.credits = credit;
        this.assignedTeachers = new ArrayList<>(); // Initialize the list
        this.students = new ArrayList<>();
        totalCourses++;
    }

    public void addTeacher(Teacher teacher) {
        if (!assignedTeachers.contains(teacher)) {
            assignedTeachers.add(teacher);
        }
    }

    public void removeTeacher(Teacher teacher) {
        assignedTeachers.remove(teacher);
    }

    public List<Teacher> getTeachers() {
        return assignedTeachers;
    }

    // ... existing code ...
} 