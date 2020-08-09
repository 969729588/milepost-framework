package com.milepost.api.util;

import com.milepost.test.BaseTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.internet.MimeMessage;
import java.io.File;

/**
 * Created by Ruifu Hua on 2018-12-12.
 */
public class JavaMailSenderTest extends BaseTest<JavaMailSenderTest> {

    private String mailUsername;

    /**
     * 这里直接注入即可，配置文件中这样配置
     * spring:
     mail:
     username: m18310891237@163.com
     password: sqm123456qweasd
     host: smtp.163.com
     properties:
     mail:
     smtp:
     ssl:
     #连接qq邮箱服务器，需要额外配置这个
     enable: true
     */
    private JavaMailSender mailSender;

    @Before
    public void init(){
        mailUsername = getProperty("spring.mail.username");
        mailSender = getBean(JavaMailSender.class);
    }

    /**
     * smtp.163.com
     * 帐号：m18310891237@163.com
     * 登录密码：nuli07011
     * 授权码：sqm123456qweasd(第三方程序登录的密码，本程序中就应该使用这个授权码连接邮箱服务器)
     *
     * 这个腾讯的貌似不能用，好像没申请授权码，可以使用163的
     * smtp.qq.com
     * 帐号：3573700781
     * 登录密码：nuli07011@，记住，要持续登录这个号码，否则会被收回的，要开通smtp
     * 授权码：
     *
     * @throws Exception
     */
    @Test
    public void test1() {
        SimpleMailMessage message = new SimpleMailMessage();
        //邮件设置
        message.setSubject("通知-今晚开会");
        message.setText("今晚7:31开会");

        message.setTo("969729588@qq.com");
        message.setFrom(mailUsername);

        mailSender.send(message);
    }

    @Test
    public void test2() throws  Exception{
        //1、创建一个复杂的消息邮件
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        //邮件设置
        helper.setSubject("通知-今晚开会");
        helper.setText("<b style='color:red'>今天 7:30 开会</b>",true);

        helper.setTo("969729588@qq.com");
        helper.setFrom(mailUsername);

        //上传文件
        helper.addAttachment("1.jpg",new File("F:\\testFile\\1.png"));
        helper.addAttachment("2.jpg",new File("F:\\testFile\\2.png"));

        mailSender.send(mimeMessage);

    }
}
