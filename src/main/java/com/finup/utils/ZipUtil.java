package com.finup.utils;

/**
 * @Author Mr.An
 * @Date 18/4/27 下午7:07
 */

import com.finup.constant.UploadType;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


public class ZipUtil {

    public static final int BUFFER_SIZE = 1024;
    private static final String TAG = "ZipUtil";

    /**
     * Method that get input file path, zip it and return File based on passed outputFilePath.
     * @param inputFilePath Path to file for compression.
     * @param outputFilePath Path to output compressed file.
     * @return Compressed file.
     * compress only one file to dst zipfile.
     */
    public static File zipFile(String inputFilePath, String outputFilePath) throws IOException {
        ZipOutputStream zos = null;

        try {
            zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outputFilePath)));
            ZipEntry entry = new ZipEntry(outputFilePath);
            zos.putNextEntry(entry);
            FileInputStream in = new FileInputStream(inputFilePath);
            int len;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((len = in.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
            zos.closeEntry();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (zos != null) {
                    zos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new File(outputFilePath);
    }

    public  Path newZipFile(Path inputFilePath, Path outputFilePath) throws IOException {
        ZipOutputStream zos = null;

        try {
            zos = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(outputFilePath)));
            ZipEntry entry = new ZipEntry(inputFilePath.getFileName().toString());
            zos.putNextEntry(entry);
           InputStream in =Files.newInputStream(inputFilePath);
            int len;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((len = in.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
            zos.closeEntry();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (zos != null) {
                    zos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("zip file:"+inputFilePath.toString()+" succeed!!");
        if(inputFilePath.getFileName().toString().equals(UploadType.fromId("12").fname)){
            Files.delete(inputFilePath);
        }
        return outputFilePath;
    }
    /**
     * Method that get input directory path, zip it and return File based on passed outputFilePath.
     * @param pathToDirectory Path to directory for compression.
     * @param outputFilePath Path to output compressed file.
     * @return Compressed file.
     * this method can only zip 1 depth Dir , can not recursively zip dirs.
     */
    public static File zipDirectory(String pathToDirectory, String outputFilePath) {
        ZipOutputStream zos = null;
        byte[] buffer = new byte[BUFFER_SIZE];
        try {
            zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outputFilePath)));

            File[] fileList = new File(pathToDirectory).listFiles();
            for (int i=0;i<fileList.length;i++) {
                if(fileList[i].isDirectory()){
                    continue;
                }
                ZipEntry entry = new ZipEntry(fileList[i].getName());
                zos.putNextEntry(entry);
                FileInputStream in = new FileInputStream(fileList[i].getAbsolutePath());
                int len;
                while ((len = in.read(buffer)) !=-1) {
                    zos.write(buffer, 0, len);
                }
                zos.closeEntry();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (zos != null) {
                    zos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new File(outputFilePath);
    }

    /**
     * @param srcPath
     * @param dstPath
     * @return
     * @throws IOException
     * java 1.7
     */
    public Path newZipDirectory(Path srcPath,Path dstPath) throws IOException {
        if(srcPath.toFile().isFile()){
            throw new RuntimeException("ERROR:srcPath must be a directory!");
        }
        final ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(dstPath));
        final byte[] buffer=new byte[1024];
        Files.walkFileTree(srcPath,new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path relativePath = srcPath.relativize(file);
                System.out.println(relativePath.toString());
                zos.putNextEntry(new ZipEntry(relativePath.toString()));
                InputStream inputStream = Files.newInputStream(file.toAbsolutePath()); //这里不能用relativePath这个，只能用file。因为relativePath主要是用来形成zip文件里面的路径tree。不是真实的文件。
                int len=-1;
                while ((len=inputStream.read(buffer))!=-1){
                    zos.write(buffer,0,len);
                }
                zos.flush();
                zos.closeEntry();
                inputStream.close();
                System.out.println("file zip : " + srcPath.getFileName().toString()+" Succeed!!");
//                Files.delete(srcPath);
                return super.visitFile(file, attrs);
            }
        });
        zos.close();

        return  dstPath;
    }
    public static File unZip(String pathToZip, String pathToOutputDir) {
        byte[] buffer = new byte[1024];
        File outputDirectory = null;
        try {

            outputDirectory = new File(pathToOutputDir);
            if (!outputDirectory.exists()) {
                outputDirectory.mkdir();
            }

            ZipInputStream zis = new ZipInputStream(new FileInputStream(pathToZip));
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(pathToOutputDir + File.separator + fileName);

                System.out.println("file unzip : " + newFile.getAbsoluteFile());

                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();

            System.out.println("Done");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return outputDirectory;
    }

    public static ZipUtil newInstance(){
        return new ZipUtil();
    }





    public static void main(String[] args) {
//        ZipUtil.zipDirectory("/Users/finup/HUJINMornitor","/Users/finup/test/abc2.zip");
//        try {
//            ZipUtil.newZipFile("/Users/finup/HUJINMornitor/target","/Users/finup/test/abc.zip");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
      /*  try {
            ZipUtil.newInstance().newZipFile("/Users/finup/Documents/test","/Users/finup/test/abc.zip");
        } catch (IOException e) {
            e.printStackTrace();
        }*/

//      ZipUtil.unZip("/Users/finup/test/abc2.zip","/Users/finup/aaaaa");
        try {
            ZipUtil.newInstance().newZipFile(Paths.get("/Users/finup/Desktop/gongxiangpingtaishangbao20180430.csv"),Paths.get("/Users/finup/Desktop/121.txt"));
//            ZipUtil.newInstance().newZipFile(Paths.get("/Users/finup/Desktop/tianchi"),Paths.get("/Users/finup/Desktop/121.zip"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


