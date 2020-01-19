package com.milepost.api.util;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

/**
 * Created by Ruifu Hua on 2020/1/16.
 */
public class ZipUtil {
    public static void zip(String srcDir, String targetFile) throws IOException {
        OutputStream fos = new FileOutputStream(targetFile);
        OutputStream bos = new BufferedOutputStream(fos);
        ArchiveOutputStream aos = new ZipArchiveOutputStream(bos);
        try {
            Path dirPath = Paths.get(srcDir);
            Files.walkFileTree(dirPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    ArchiveEntry entry = new ZipArchiveEntry(dir.toFile(), dirPath.relativize(dir).toString());
                    aos.putArchiveEntry(entry);
                    aos.closeArchiveEntry();
                    return super.preVisitDirectory(dir, attrs);
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    InputStream fileIs = null;
                    try {
                        fileIs = new FileInputStream(file.toFile());

                        ArchiveEntry entry = new ZipArchiveEntry(
                                file.toFile(), dirPath.relativize(file).toString());
                        aos.putArchiveEntry(entry);
                        IOUtils.copy(fileIs, aos);
                        aos.closeArchiveEntry();
                        return super.visitFile(file, attrs);
                    }finally {
                        IOUtils.closeQuietly(fileIs);
                    }
                }

            });
        }finally {
            IOUtils.closeQuietly(aos);
            IOUtils.closeQuietly(bos);
            IOUtils.closeQuietly(fos);
        }
    }


    public static void unzip(String zipFileName, String destDir) throws IOException {
        InputStream fis = null;
        InputStream bis = null;
        ArchiveInputStream ais = null;

        try {
            fis = Files.newInputStream(Paths.get(zipFileName));
            bis = new BufferedInputStream(fis);
            ais = new ZipArchiveInputStream(bis);

            ArchiveEntry entry;
            while (Objects.nonNull(entry = ais.getNextEntry())) {
                if (!ais.canReadEntryData(entry)) {
                    continue;
                }

                String name = destDir + File.separator + entry.getName();
                File f = new File(name);
                if (entry.isDirectory()) {
                    if (!f.isDirectory() && !f.mkdirs()) {
                        f.mkdirs();
                    }
                } else {
                    File parent = f.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("failed to create directory " + parent);
                    }
                    try (OutputStream o = Files.newOutputStream(f.toPath())) {
                        IOUtils.copy(ais, o);
                    }
                }
            }
        } finally {
            IOUtils.closeQuietly(ais);
            IOUtils.closeQuietly(bis);
            IOUtils.closeQuietly(fis);
        }
    }
}
