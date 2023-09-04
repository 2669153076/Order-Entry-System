package com.example.reggie.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;


public class EmailUtils {
    /**
     * 发送邮件
     * @param email
     * @param authCode
     */
    public static void sendAuthCodeEmail(String email,String authCode){
        try{
            SimpleEmail mail=new SimpleEmail();
            mail.setHostName("smtp.qq.com");    //发送邮件的服务器
            mail.setAuthentication("2669153076@qq.com","wjgosiybwzofebaa");
            mail.setFrom("2669153076@qq.com","mrs");
            mail.setSSLOnConnect(true);
            mail.addTo(email);//接收的邮箱
            mail.setSubject("验证码");//邮件主题
            mail.setMsg("登录验证码为:"+authCode+"\n"+"       (有效期为一分钟)");    //邮件内容
            mail.send();
        }catch (EmailException e){
            e.printStackTrace();
        }
    }
}
