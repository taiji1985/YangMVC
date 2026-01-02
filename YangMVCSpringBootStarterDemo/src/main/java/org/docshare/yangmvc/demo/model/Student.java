package org.docshare.yangmvc.demo.model;

/**
 * 学生实体类
 * 对应 teach 数据库的 student 表
 */
public class Student {
    private Integer id;
    private String name;
    private Double height;
    private Double weight;

    public Student() {}

    public Student(Integer id, String name, Double height, Double weight) {
        this.id = id;
        this.name = name;
        this.height = height;
        this.weight = weight;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", height=" + height +
                ", weight=" + weight +
                '}';
    }
}
