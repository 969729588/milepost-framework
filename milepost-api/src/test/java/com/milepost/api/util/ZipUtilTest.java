package com.milepost.api.util;

import org.junit.Test;

import java.io.IOException;

/**
 * Created by Ruifu Hua on 2020/1/16.
 */
public class ZipUtilTest {
    @Test
    public void test2() throws IOException {
        String srcDir = "F:\\testFile\\testZip";
        String targetFile = "F:\\testFile\\testZip.zip";
        ZipUtil.zip(srcDir, targetFile);
    }

    @Test
    public void test3() throws IOException {
        String srcDir = "F:\\testFile\\testZip_";
        String targetFile = "F:\\testFile\\testZip.zip";
        ZipUtil.unzip(targetFile, srcDir);
    }
}
