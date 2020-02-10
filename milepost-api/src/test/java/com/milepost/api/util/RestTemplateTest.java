package com.milepost.api.util;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.milepost.api.vo.response.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ruifu Hua on 2020/2/9.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(value = {"spring.main.allow-bean-definition-overriding=true",
})
public class RestTemplateTest {
    @Autowired
    private RestTemplate restTemplate;

    private String BASE_URL = "http://192.168.1.104:8080/restServer";

    /**
     * 显示响应头
     * @param headers
     */
    private void printHeaders(HttpHeaders headers) {
        for(Map.Entry<String, List<String>> entry : headers.entrySet()){
            String key = entry.getKey();
            System.out.print(key + " = ");
            List<String> valueList = entry.getValue();
            for(String value : valueList){
                System.out.println(value + "、");
            }
            System.out.println("------------");
        }
    }

    /**
     * @param map
     */
    private void printMap(Map<String, Object> map) {
        for(Map.Entry<String, Object> entry : map.entrySet()){
            System.out.print(entry.getKey() + ":" + entry.getValue() + ",");
        }
        System.out.println("");
    }

    /**
     * getForEntity，有三个重载的方法，
     * @throws IOException
     */
    @Test
    public void getForEntity() throws IOException {
        //1.没有参数的，或者所直接在url上拼接参数的
//        ResponseEntity<String> responseEntity = restTemplate.getForEntity(BASE_URL + "/23", String.class);
        //2.不定个数参数，通过位置匹配，url中的{}与uriVariables按照位置对应，下面的方法会生成"http://192.168.1.104:8080/student/23/aa"
//        ResponseEntity<String> responseEntity = restTemplate.getForEntity(BASE_URL + "/{p1}/{p2}", String.class, 23,"aa");
//        ResponseEntity<String> responseEntity = restTemplate.getForEntity(BASE_URL + "/{p1}", String.class, 23);

        //3.map，用url中的{key}去获取map中的值
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("id", 23);
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(BASE_URL + "/{id}", String.class, paramMap);

        HttpHeaders headers = responseEntity.getHeaders();
        printHeaders(headers);
        HttpStatus statusCode = responseEntity.getStatusCode();
        int code = statusCode.value();
        System.out.println(code);
        String result = responseEntity.getBody();
        System.out.println(result);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.readValue(result, Map.class);
        printMap(map);

        Map<String, Object> payload = (Map<String, Object>)map.get("payload");
        printMap(payload);
    }

    @Test
    public void getForEntity_() throws IOException {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("page", 1);
        paramMap.put("limit", 10);
        ResponseEntity<String> responseEntity = restTemplate.getForEntity("https://www.layui.com/demo/table/user", String.class, paramMap);

        HttpHeaders headers = responseEntity.getHeaders();
        printHeaders(headers);
        HttpStatus statusCode = responseEntity.getStatusCode();
        int code = statusCode.value();
        System.out.println(code);
        String result = responseEntity.getBody();
        System.out.println(result);
    }

    /**
     * getForObject与getForEntity类似，只是少了getHeaders、getStatusCode、getBody，
     * 而他的返回值就是getBody
     * @throws IOException
     */
    @Test
    public void getForObject() throws IOException {
        String result = restTemplate.getForObject(BASE_URL + "/23", String.class);
        System.out.println(result);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.readValue(result, Map.class);
        printMap(map);

        Map<String, Object> payload = (Map<String, Object>)map.get("payload");
        printMap(payload);
    }

    /**
     * http://localhost:8080/student?pageNum=1&pageSize=3&name=1&birth=1990-01-11%2000%3A00%3A00
     * @throws IOException
     */
    @Test
    public void getForEntity1() throws IOException {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("pageNum", 1);
        paramMap.put("pageSize", 3);
        paramMap.put("name", "1");
        paramMap.put("birth", "1990-01-11 00:00:00");
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(BASE_URL + "?pageNum={pageNum}&pageSize={pageSize}&name={name}&birth={birth}", String.class, paramMap);
        HttpHeaders headers = responseEntity.getHeaders();
        printHeaders(headers);
        HttpStatus statusCode = responseEntity.getStatusCode();
        int code = statusCode.value();
        System.out.println(code);
        String result = responseEntity.getBody();
        System.out.println(result);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.readValue(result, Map.class);
        printMap(map);

        Map<String, Object> payload = (Map<String, Object>)map.get("payload");
        printMap(payload);

        List<Object> list = (List<Object>)payload.get("list");
        for(Object object : list){
            Map<String, Object> map_ = (Map<String, Object>)object;
            printMap(map_);
        }
    }

    /**
     * post请求中，可以传递POJO类型的参数，服务端要使用@RequestBody接收，
     * 即public Response<Student> add(@ApiParam(value = "学生") @RequestBody Student record)
     */
    @Test
    public void postForEntity(){
        Student student = new Student();
        student.setName("李四");
        student.setBirth(new Date());
        student.setClassesId("01");
        student.setRemark("努力学习");
        student.setScore(33f);
        student.setStuNo("zhangsan");
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(BASE_URL, student, String.class);
        HttpHeaders headers = responseEntity.getHeaders();
        printHeaders(headers);
        HttpStatus statusCode = responseEntity.getStatusCode();
        int code = statusCode.value();
        System.out.println(code);
        String result = responseEntity.getBody();
        System.out.println(result);
    }

    /**
     * post请求中，可以传递File类型的参数，服务端要使MultipartFile multipartFile接收，
     * 即public Response<Student> add(@ApiParam(value = "学生") @RequestBody Student record)
     */
    @Test
    public void postForEntity1(){
        MultiValueMap<String, Object> paramMap = new LinkedMultiValueMap<>();
        paramMap.add("name", "张三");
        paramMap.add("stuNo", "zhangsan");
        paramMap.add("file", new FileSystemResource(new File("F:\\TestPicture\\1.jpg")));

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(BASE_URL + "/file", paramMap, String.class);
        HttpHeaders headers = responseEntity.getHeaders();
        printHeaders(headers);
        HttpStatus statusCode = responseEntity.getStatusCode();
        int code = statusCode.value();
        System.out.println(code);
        String result = responseEntity.getBody();
        System.out.println(result);
    }


    /**
     * 请求一下不返回json数据而返回一个资源地址的服务端，如登录接口，登录成功之后，重定向到系统首页，
     */
    @Test
    public void postForLocation(){
        Student student = new Student();
        student.setName("李四");
        student.setBirth(new Date());
        student.setClassesId("01");
        student.setRemark("努力学习");
        student.setScore(33f);
        student.setStuNo("zhangsan");
        URI uri = restTemplate.postForLocation(BASE_URL + "/login", student, String.class);
        System.out.println(uri.toString());//http://192.168.1.104:8080/index.html
    }

    /**
     * put请求中，可以传递POJO类型的参数，服务端要使用@RequestBody接收，
     * 即public Response<Student> add(@ApiParam(value = "学生") @RequestBody Student record)
     */
    @Test
    public void put(){
        Student student = new Student();
        student.setId("9");
        student.setName("李四");
        student.setBirth(new Date());
        student.setClassesId("01");
        student.setRemark("努力学习");
        student.setScore(33f);
        student.setStuNo("zhangsan");
        restTemplate.put(BASE_URL, student, String.class);
    }


    /**
     * delete传参的方式与get相同
     * @throws IOException
     */
    @Test
    public void delete() throws IOException {
        //1.没有参数的，或者所直接在url上拼接参数的
//        ResponseEntity<String> responseEntity = restTemplate.getForEntity(BASE_URL + "/23", String.class);
        //2.不定个数参数，通过位置匹配，url中的{}与uriVariables按照位置对应，下面的方法会生成"http://192.168.1.104:8080/student/23/aa"
//        ResponseEntity<String> responseEntity = restTemplate.getForEntity(BASE_URL + "/{p1}/{p2}", String.class, 23,"aa");
//        ResponseEntity<String> responseEntity = restTemplate.getForEntity(BASE_URL + "/{p1}", String.class, 23);

        //3.map，用url中的{key}去获取map中的值
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("id", "9");
        restTemplate.delete(BASE_URL + "/{id}", paramMap);
    }

    /**
     * RequestEntity中设置body
     * @throws URISyntaxException
     */
    @Test
    public void exchange1() throws URISyntaxException {
        Student student = new Student();
        student.setName("李四");
        student.setBirth(new Date());
        student.setClassesId("01");
        student.setRemark("努力学习");
        student.setScore(33f);
        student.setStuNo("zhangsan");

        RequestEntity request = RequestEntity
                .post(new URI(BASE_URL))
                .accept(MediaType.APPLICATION_JSON)
                .body(student);
        ParameterizedTypeReference parameterizedTypeReference = new ParameterizedTypeReference<Response<Student>>() {};
        ResponseEntity<Response<Student>> responseEntity = restTemplate.exchange(request, parameterizedTypeReference);
        HttpHeaders headers = responseEntity.getHeaders();
        printHeaders(headers);
        HttpStatus statusCode = responseEntity.getStatusCode();
        int code = statusCode.value();
        System.out.println(code);
        Response<Student> response = responseEntity.getBody();
        System.out.println(response);
        System.out.println(response.getPayload().getName());
    }


    /**
     * HttpEntity中设置body和header
     * @throws URISyntaxException
     */
    @Test
    public void exchange2() throws URISyntaxException, MalformedURLException {
        Student student = new Student();
        student.setName("王五");
        student.setBirth(new Date());
        student.setClassesId("01");
        student.setRemark("努力学习");
        student.setScore(33f);
        student.setStuNo("zhangsan");

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("header1","header1-value_1");
        httpHeaders.add("header1","header1-value_2");//多值的map，不会覆盖
        httpHeaders.add("header2","header2-value");
        HttpEntity<Student> httpEntity = new HttpEntity(student, httpHeaders);

        ParameterizedTypeReference<Response<Student>> parameterizedTypeReference = new ParameterizedTypeReference() {};
        ResponseEntity<Response<Student>> responseEntity =
                restTemplate.exchange(BASE_URL, HttpMethod.POST, httpEntity, parameterizedTypeReference);
        HttpHeaders headers = responseEntity.getHeaders();
        printHeaders(headers);
        HttpStatus statusCode = responseEntity.getStatusCode();
        int code = statusCode.value();
        System.out.println(code);
        Response<Student> response = responseEntity.getBody();
        System.out.println(response);
        System.out.println(response.getPayload().getName());
    }

    class Student implements Serializable {
        /**
         * student.ID
         *
         *
         * @mbggenerated
         */
        private String id;

        /**
         * student.NAME
         * 姓名
         *
         * @mbggenerated
         */
        private String name;

        /**
         * student.BIRTH
         * 出生日期
         *
         * @mbggenerated
         */
        //如果再主配置文件中配置了spring.mvc.date-format属性之后，springboot会自动配置日期类型转换器并制定日期转换格式，所以这里就不用写DateTimeFormat注解了。
        //@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")//将前端传过来的字符串形式的日期转换成Date
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", locale = "zh", timezone = "GMT+8")//返回给前端时候，格式化Date的格式
        private Date birth;

        /**
         * student.STU_NO
         * 学号
         *
         * @mbggenerated
         */
        private String stuNo;

        /**
         * student.SCORE
         * 分数
         *
         * @mbggenerated
         */
        private Float score;

        /**
         * student.REMARK
         * 评价
         *
         * @mbggenerated
         */
        private String remark;

        /**
         * student.CLASSES_ID
         * 班级id
         *
         * @mbggenerated
         */
        private String classesId;

        private Classes classes;

        public Classes getClasses() {
            return classes;
        }

        public void setClasses(Classes classes) {
            this.classes = classes;
        }

        private static final long serialVersionUID = 1L;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id == null ? null : id.trim();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name == null ? null : name.trim();
        }

        public String getStuNo() {
            return stuNo;
        }

        public void setStuNo(String stuNo) {
            this.stuNo = stuNo == null ? null : stuNo.trim();
        }

        public Date getBirth() {
            return birth;
        }

        public void setBirth(Date birth) {
            this.birth = birth;
        }

        public Float getScore() {
            return score;
        }

        public void setScore(Float score) {
            this.score = score;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark == null ? null : remark.trim();
        }

        public String getClassesId() {
            return classesId;
        }

        public void setClassesId(String classesId) {
            this.classesId = classesId == null ? null : classesId.trim();
        }

        @Override
        public String toString() {
            return "Student{" +
                    "id='" + id + '\'' +
                    ", name='" + name + '\'' +
                    ", stuNo='" + stuNo + '\'' +
                    ", birth=" + birth +
                    ", score=" + score +
                    ", remark='" + remark + '\'' +
                    ", classesId='" + classesId + '\'' +
                    ", classes=" + classes +
                    '}';
        }
    }


    class Classes implements Serializable {
        /**
         * classes.ID
         *
         *
         * @mbggenerated
         */
        private String id;

        /**
         * classes.NAME
         *
         *
         * @mbggenerated
         */
        private String name;

        private static final long serialVersionUID = 1L;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id == null ? null : id.trim();
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name == null ? null : name.trim();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(getClass().getSimpleName());
            sb.append(" [");
            sb.append("Hash = ").append(hashCode());
            sb.append(", id=").append(id);
            sb.append(", name=").append(name);
            sb.append("]");
            return sb.toString();
        }
    }
}
