public import java.io.*;
import java.util.*;

class Repository<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<T> items;

    public Repository() {
        items = new ArrayList<>();
    }

    public void add(T item) {
        items.add(item);
        System.out.println(item.getClass().getSimpleName() + " added to the repository.");
    }

    public void remove(T item) {
        if (items.remove(item)) {
            System.out.println(item.getClass().getSimpleName() + " removed from the repository.");
        } else {
            System.out.println(item.getClass().getSimpleName() + " not found in the repository.");
        }
    }

    public List<T> getAll() {
        return new ArrayList<>(items);
    }

    public int size() {
        return items.size();
    }
}

class University {

    private static Repository<Student> studentRepository = new Repository();
    private static Repository<Teacher> teacherRepository = new Repository();
    private static Repository<Course> courseRepository = new Repository();

    public static void displaySystemStats() {
        System.out.println("System Statistics:");
        System.out.println("Total Students: " + Student.getTotalStudents());
        System.out.println("Total Teachers: " + Teacher.getTotalTeachers());
        System.out.println("Total Courses: " + courseRepository.size());
    }

    public static void loadData(String filename) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            studentRepository = (Repository<Student>) ois.readObject();
            teacherRepository = (Repository<Teacher>) ois.readObject();
            courseRepository = (Repository<Course>) ois.readObject();
            System.out.println("Data loaded successfully from " + filename);
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading data from " + filename + ": " + e.getMessage());
        }
    }

    public static void saveData(String filename) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(studentRepository);
            oos.writeObject(teacherRepository);
            oos.writeObject(courseRepository);
            System.out.println("Data saved successfully to " + filename);
        } catch (IOException e) {
            System.out.println("Error saving data to " + filename + ": " + e.getMessage());
        }
    }

    public static Repository<Student> getStudentRepository() {
        return studentRepository;
    }

    public static Repository<Teacher> getTeacherRepository() {
        return teacherRepository;
    }

    public static Repository<Course> getCourseRepository() {
        return courseRepository;
    }

    public static void searchStudentByName(String name) {
        List<Student> students = studentRepository.getAll();
        List<Student> matchingStudents = new ArrayList<>();
        for (Student s : students) {
            if (s.getName().equalsIgnoreCase(name)) {
                matchingStudents.add(s);
            }
        }
        if (matchingStudents.isEmpty()) {
            System.out.println("No students found with the name " + name);
        } else {
            System.out.println("Students found with the name '" + name + "':");
            for (Student s : matchingStudents) {
                System.out.println(s);
            }
        }
    }

    public static void filterCoursesByCredits(int minCredits) {
        List<Course> allCourses = courseRepository.getAll();
        for (Course c : allCourses) {
            if (c.getCredits() >= minCredits) {
                System.out.println(c.getTitle());
            }
        }
    }
}

class Person implements Serializable {
    private String name;
    private String email;
    private Date dateOfBirth;

    public Person() {
    }

    public Person(String name, String email, Date date) {
        this.name = name;
        this.email = email;
        this.dateOfBirth = date;
    }

    public Person(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Name: " + name + ", Email: " + email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setDateOfBirth(Date date) {
        this.dateOfBirth = date;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }
}

class Address implements Serializable {
    private String city;
    private int houseNumber;

    public Address() {
    }

    public Address(int number, String city) {
        this.houseNumber = number;
        this.city = city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCity() {
        return city;
    }

    public void setHouseNumber(int number) {
        this.houseNumber = number;
    }

    public int getHouseNumber() {
        return houseNumber;
    }
}

class Course implements Serializable {
    private static int totalCourses;
    private int courseID;
    private String title;
    private int credits;
    private Teacher assignedTeacher;
    private List<Student> students;

    public Course() {
        totalCourses++;
    }

    public Course(int id, String title, int credit, Teacher teacher) {
        this.courseID = id;
        this.title = title;
        this.credits = credit;
        this.assignedTeacher = teacher;
        this.students = new ArrayList<>();
        totalCourses++;
    }

    public void addStudent(Student s) {
        students.add(s);
    }

    public void removeStudent(Student student) {
        boolean check = false;
        for (Student s : students) {
            if (s.getId() == student.getId()) {
                students.remove(s);
                check = true;
                System.out.println("Student with id: " + student.getId() + " is found and removed");
                break;
            }
        }
        if (!check) {
            System.out.println("Student with id: " + student.getId() + " is not found");
        }
    }

    public double averageGrade() {
        double totalMarks = 0;

        for (Student s : students) {
            totalMarks += s.getScore();
        }
        return totalMarks / students.size();
    }

    public Double calculateMedianGrade() {
        List<Double> doubleList = new ArrayList<>();

        for (Student s : students) {
            doubleList.add(s.getScore());
        }

        if (doubleList.isEmpty()) {
            throw new IllegalArgumentException("No students available to calculate median.");
        }

        Collections.sort(doubleList); // sorting to find the median

        int size = doubleList.size();
        if (size % 2 == 1) {
            return doubleList.get(size / 2); // odd case
        } else {
            // even case
            return (doubleList.get(size / 2 - 1) + doubleList.get(size / 2)) / 2.0;
        }
    }

    public int getId() {
        return courseID;
    }

    public void setId(int id) {
        this.courseID = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credit) {
        this.credits = credit;
    }

    public Teacher getTeacher() {
        return assignedTeacher;
    }

    public void setTeacher(Teacher teacher) {
        this.assignedTeacher = teacher;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }

    public static int getTotalCourses() {
        return totalCourses;
    }
}

class CourseRecord implements Serializable {
    private Course course;
    private Double marks;

    public CourseRecord(Course course, Double marks) {
        this.course = course;
        this.marks = marks;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Double getMarks() {
        return marks;
    }

    public void setMarks(Double marks) {
        this.marks = marks;
    }

    @Override
    public String toString() {
        return "Course: " + course.getTitle() + ", Marks: " + marks;
    }
}

class Student extends Person implements Serializable {
    private int studentID;
    private Address address;
    private List<CourseRecord> courseRecords;
    private static int totalStudents;
    private Double studentScore;

    public Student() {
        totalStudents++;
    }

    public Student(String name, int id, String email, String city, int houseNumber, Date dateOfBirth, Double score) {
        super(name, email, dateOfBirth);
        this.studentID = id;
        this.address = new Address(houseNumber, city);
        this.courseRecords = new ArrayList<>();
        totalStudents++;
    }

    public void addCourse(Course c, Double marks) {
        CourseRecord newRecord = new CourseRecord(c, marks);
        courseRecords.add(newRecord);
        System.out.println("Student " + this.studentID + " successfully enrolled in " + c.getTitle() + " with marks: " + marks);
    }

    public void displayCoursesAndMarks() {
        if (courseRecords.isEmpty()) {
            System.out.println("No courses enrolled for student " + this.studentID);
            return;
        }
        System.out.println("Courses and Marks for student ID: " + this.studentID);
        for (CourseRecord record : courseRecords) {
            System.out.println(record);
        }
    }

    @Override
    public String toString() {
        return "Student ID: " + studentID + ", Name: " + getName() + ", Courses Enrolled: " + courseRecords.size();
    }

    public List<CourseRecord> getCourses() {
        return courseRecords;
    }

    public void setCourses(List<CourseRecord> courses) {
        this.courseRecords = courses;
    }

    public void setId(int id) {
        this.studentID = id;
    }

    public int getId() {
        return studentID;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Address getAddress() {
        return address;
    }

    public static int getTotalStudents() {
        return totalStudents;
    }

    public Double getScore() {
        return studentScore;
    }

    public void setScore(Double score) {
        this.studentScore = score;
    }
}

class Teacher extends Person implements Reportable, Serializable {
    private static int totalTeachers;
    private int teacherID;
    private String specialization;
    private List<Course> courses;

    public Teacher(String name, int id, String specialization) {
        super(name);
        this.teacherID = id;
        this.specialization = specialization;
        courses = new ArrayList<>();
        totalTeachers++;
    }

    public void addCourse(Course c) {
        courses.add(c);
    }

    @Override
    public String toString() {
        StringBuilder assignedCoursesList = new StringBuilder("[ ");
        for (Course course : courses) {
            assignedCoursesList.append(course.getTitle()).append(", ");
        }
        if (!courses.isEmpty()) {
            assignedCoursesList.setLength(assignedCoursesList.length() - 2);
        }
        assignedCoursesList.append(" ]");

        return "Teacher: T" + teacherID + ", Name: " + getName() + ", Specialization: " + specialization + ", Assigned Courses: " + assignedCoursesList;
    }

    public void removeCourse(int id) {
        for (Course c : courses) {
            if (c.getId() == id) {
                courses.remove(c);
                break;
            }
        }
    }

    @Override
    public void exportToFile(String filename, String reportContent) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(reportContent);
            System.out.println("Teacher report saved to file: " + filename);
        } catch (IOException e) {
            System.out.println("Error writing teacher report to file: " + e.getMessage());
        }
    }

    @Override
    public String generateReport(List<Person> people) {
        int studentsCount = 0;
        for (Course c : courses) {
            studentsCount += c.getStudents().size();
        }
        return "Teacher Report:\n" +
               "Total Students assigned: " + studentsCount +
               "\nTotal Courses assigned: " + courses.size();
    }

    public static int getTotalTeachers() {
        return totalTeachers;
    }
}

interface Reportable {
    String generateReport(List<Person> people);
    void exportToFile(String filename, String reportContent);
}

class AdministrativeStaff extends Person implements Reportable {
    private int staffID;
    private String role;
    private String department;
    private List<Person> persons;

    public AdministrativeStaff(int id, String name, String role, String department) {
        super(name);
        this.staffID = id;
        this.role = role;
        this.department = department;
    }

    @Override
    public void exportToFile(String filename, String reportContent) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(reportContent);
            System.out.println("Administrator report saved to file: " + filename);
        } catch (IOException e) {
            System.out.println("Error writing administrator report to file: " + e.getMessage());
        }
    }

    @Override
    public String generateReport(List<Person> people) {
        int totalStudents = 0;
        int totalTeachers = 0;
        Set<Course> uniqueCourses = new HashSet<>(); // Use a Set to store unique courses

        for (Person person : people) {
            if (person instanceof Student) {
                totalStudents++;
                Student s = (Student) person;

                // Add all courses (avoiding duplicates due to Set)
                for (CourseRecord record : s.getCourses()) {
                    uniqueCourses.add(record.getCourse());
                }
            } else if (person instanceof Teacher) {
                totalTeachers++;
            }
        }

        // Build the summary report string
        String report = "Report Summary:\n" +
                "Total Students: " + totalStudents + "\n" +
                "Total Teachers: " + totalTeachers + "\n" +
                "Total Unique Courses: " + uniqueCourses.size();

        return report;
    }

    @Override
    public String toString() {
        return "Admin Staff: " + staffID + ", Name: " + getName() + ", Role: " + role + ", Department: " + department;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getStaffID() {
        return staffID;
    }

    public void setStaffID(int staffID) {
        this.staffID = staffID;
    }
} test {
  
}
