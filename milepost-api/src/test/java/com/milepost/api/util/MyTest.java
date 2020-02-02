package com.milepost.api.util;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;

/**
 * Created by Ruifu Hua on 2020/1/16.
 */
public class MyTest {
    /**
     * test
     */
    @Test
    public void test1(){
        FileUtils.deleteQuietly(new File("F:\\testFile\\testZip_"));
    }
}
