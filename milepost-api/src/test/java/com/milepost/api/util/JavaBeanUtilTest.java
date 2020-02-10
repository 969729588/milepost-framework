package com.milepost.api.util;

import org.junit.Test;

/**
 * Created by Ruifu Hua on 2019/7/25.
 */
public class JavaBeanUtilTest {

    @Test
    public void test1(){
        Person person = new Person("张三", 20);
        Student student = new Student("李四", 30, "20190725-001", "三年2班");
        System.out.println(person);
        System.out.println(student);

        JavaBeanUtil.copyBean(person, student, "age");
        System.out.println(person);
        System.out.println(student);
    }
}

class Person{
    private String name;
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }

    public Person() {
    }

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }
}

class Student extends Person{
    private String studentID;
    private String class_;

    public String getStudentID() {
        return studentID;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public String getClass_() {
        return class_;
    }

    public void setClass_(String class_) {
        this.class_ = class_;
    }

    @Override
    public String toString() {
        return "Student{" +
                "studentID='" + studentID + '\'' +
                ", class_='" + class_ + '\'' +
                ", name='" + super.getName() + '\'' +
                ", age='" + super.getAge() + '\'' +
                '}';
    }

    public Student(String studentID, String class_) {
        this.studentID = studentID;
        this.class_ = class_;
    }

    public Student(String name, int age, String studentID, String class_) {
        super(name, age);
        this.studentID = studentID;
        this.class_ = class_;
    }

    public Student() {
        super();
    }
}
