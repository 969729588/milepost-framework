package com.milepost.api.util;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.*;

/**
 * Created by Ruifu Hua on 2020/1/16.
 * commons-compress还是不太好用
 */
@Deprecated
public class ZipUtilByCommonCompress {

    //zip和unzip有时候会卡死
//    public static void zip(String srcDir, String targetFile) throws IOException {
//        OutputStream fos = new FileOutputStream(targetFile);
//        OutputStream bos = new BufferedOutputStream(fos);
//        ArchiveOutputStream aos = new ZipArchiveOutputStream(bos);
//        try {
//            Path dirPath = Paths.get(srcDir);
//            Files.walkFileTree(dirPath, new SimpleFileVisitor<Path>() {
//                @Override
//                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
//                    ArchiveEntry entry = new ZipArchiveEntry(dir.toFile(), dirPath.relativize(dir).toString());
//                    aos.putArchiveEntry(entry);
//                    aos.closeArchiveEntry();
//                    return super.preVisitDirectory(dir, attrs);
//                }
//
//                @Override
//                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//                    InputStream fileIs = null;
//                    try {
//                        fileIs = new FileInputStream(file.toFile());
//
//                        ArchiveEntry entry = new ZipArchiveEntry(
//                                file.toFile(), dirPath.relativize(file).toString());
//                        aos.putArchiveEntry(entry);
//                        IOUtils.copy(fileIs, aos);
//                        aos.closeArchiveEntry();
//                        return super.visitFile(file, attrs);
//                    }finally {
//                        IOUtils.closeQuietly(fileIs);
//                    }
//                }
//
//            });
//        }finally {
//            IOUtils.closeQuietly(aos);
//            IOUtils.closeQuietly(bos);
//            IOUtils.closeQuietly(fos);
//        }
//    }
//
//
//    public static void unzip(String zipFileName, String destDir) throws IOException {
//        InputStream fis = null;
//        InputStream bis = null;
//        ArchiveInputStream ais = null;
//
//        try {
//            fis = Files.newInputStream(Paths.get(zipFileName));
//            bis = new BufferedInputStream(fis);
//            ais = new ZipArchiveInputStream(bis);
//
//            ArchiveEntry entry;
//            while (Objects.nonNull(entry = ais.getNextEntry())) {
//                if (!ais.canReadEntryData(entry)) {
//                    continue;
//                }
//
//                String name = destDir + File.separator + entry.getName();
//                File f = new File(name);
//                if (entry.isDirectory()) {
//                    if (!f.isDirectory() && !f.mkdirs()) {
//                        f.mkdirs();
//                    }
//                } else {
//                    File parent = f.getParentFile();
//                    if (!parent.isDirectory() && !parent.mkdirs()) {
//                        throw new IOException("failed to create directory " + parent);
//                    }
//                    try (OutputStream o = Files.newOutputStream(f.toPath())) {
//                        IOUtils.copy(ais, o);
//                    }
//                }
//            }
//        } finally {
//            IOUtils.closeQuietly(ais);
//            IOUtils.closeQuietly(bis);
//            IOUtils.closeQuietly(fis);
//        }
//    }


    /**
     * 把文件夹或文件压缩成zip
     * @param srcFilePath   可以是一个文件，也可以是一个文件夹
     * @param targetFilePath    压缩包保存的位置
     * @throws Exception
     */
    public static void compress2Zip(String srcFilePath, String targetFilePath) throws Exception {
        File srcFile = new File(srcFilePath);
        File[] srcFiles = null;
        if(srcFile.isDirectory()){
            srcFiles = srcFile.listFiles();
        }else{
            srcFiles = new File[]{srcFile};
        }

        if(srcFiles==null || srcFiles.length<=0){
            throw new Exception("源目录下无内容。");
        }
        if(StringUtils.isBlank(targetFilePath) || !targetFilePath.endsWith("zip")){
            throw new Exception("目标文件不是以\".zip\"结尾。");
        }

        ZipArchiveOutputStream zaos = null;
        try {
            File targetFile = new File(targetFilePath);
            zaos = new ZipArchiveOutputStream(targetFile);
            //Use Zip64 extensions for all entries where they are required
            zaos.setUseZip64(Zip64Mode.AsNeeded);

            //将每个文件用ZipArchiveEntry封装
            //再用ZipArchiveOutputStream写到压缩文件中
            for(File file : srcFiles) {
                if(file != null) {
                    ZipArchiveEntry zipArchiveEntry  = new ZipArchiveEntry(file,file.getName());
                    zaos.putArchiveEntry(zipArchiveEntry);
                    InputStream is = null;
                    try {
                        is = new BufferedInputStream(new FileInputStream(file));
                        byte[] buffer = new byte[1024 * 5];
                        int len = -1;
                        while((len = is.read(buffer)) != -1) {
                            //把缓冲区的字节写入到ZipArchiveEntry
                            zaos.write(buffer, 0, len);
                        }
                        //Writes all necessary data for this entry.
                        zaos.closeArchiveEntry();
                    }finally {
                        IOUtils.closeQuietly(is);
                    }
                }
            }
            zaos.finish();
        }finally {
            IOUtils.closeQuietly(zaos);
        }
    }

    /**
     * 把zip文件解压到指定的文件夹
     * @param zipFilePath   zip文件路径
     * @param saveFileDir   保存加压出来的文件的位置，必须是一个文件夹，如果不存在则自动创建
     * @throws Exception
     */
    public static void decompressZip(String zipFilePath,String saveFileDir) throws Exception {
        File saveFileDirFile = new File(saveFileDir);
        if(!saveFileDirFile.exists()){
            saveFileDirFile.mkdirs();
        }

        if(StringUtils.isBlank(zipFilePath) || !zipFilePath.endsWith("zip")){
            throw new Exception("压缩文件不是以\".zip\"结尾。");
        }

        File file = new File(zipFilePath);
        if(!file.exists()){
            throw new Exception("压缩文件不存在。");
        }

        InputStream is = null;
        //can read Zip archives
        ZipArchiveInputStream zais = null;
        try {
            is = new FileInputStream(file);
            zais = new ZipArchiveInputStream(is);
            ArchiveEntry  archiveEntry = null;
            //把zip包中的每个文件读取出来
            //然后把文件写到指定的文件夹
            while((archiveEntry = zais.getNextEntry()) != null) {
                //获取文件名
                String entryFileName = archiveEntry.getName();
                //构造解压出来的文件存放路径
                String entryFilePath = saveFileDir + entryFileName;
                byte[] content = new byte[(int) archiveEntry.getSize()];
                zais.read(content);
                OutputStream os = null;
                try {
                    //把解压出来的文件写到指定路径
                    File entryFile = new File(entryFilePath);
                    os = new BufferedOutputStream(new FileOutputStream(entryFile));
                    os.write(content);
                }finally {
                    if(os != null) {
                        os.flush();
                        IOUtils.closeQuietly(os);
                    }
                }
            }
        }finally {
            IOUtils.closeQuietly(zais);
            IOUtils.closeQuietly(is);
        }
    }
}
