package org.docshare.yangmvc.demo.model;

/**
 * 课程实体类
 * 对应 teach 数据库的 course 表
 */
public class Course {
    private Integer id;
    private String courseName;
    private Integer credit;

    public Course() {}

    public Course(Integer id, String courseName, Integer credit) {
        this.id = id;
        this.courseName = courseName;
        this.credit = credit;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Integer getCredit() {
        return credit;
    }

    public void setCredit(Integer credit) {
        this.credit = credit;
    }

    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", courseName='" + courseName + '\'' +
                ", credit=" + credit +
                '}';
    }
}
