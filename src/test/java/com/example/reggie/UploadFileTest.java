package com.example.reggie;

import org.junit.Test;

public class UploadFileTest {
    @Test
    public void test1(){
        String fileName="ererwe.jpg";
        String suffix=fileName.substring(fileName.lastIndexOf("."));
        System.out.println(suffix);
    }
}
