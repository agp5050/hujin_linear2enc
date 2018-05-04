package com.finup.test;

import com.finup.utils.*;
import com.icfcc.batch.compress.CompressUtil;
import com.icfcc.batch.crypt.MsgDecryptImpl2;
import com.icfcc.foundation.exception.CFCCException;
//import org.junit4.Test;
/**
 * SFTP地址为221.238.206.12 ，端口号27980
 * Created by wangxy on 2017/12/27.
 */
public class TestOld {

    public String upload(String filePath){
        String sourcePath = "/Users/finup/PycharmProjects/untitled/" + filePath; try {
            System.out.println(sourcePath);
            FtpUtil.ftpUpload("221.238.206.12",27980,"aqj_mgr","password",60000,sourcePath);
//            model.addAttribute("message", "上传成功!");
        } catch (Exception e) {
            e.printStackTrace();
//            System.out.println(e.printStackTrace(););
//            logger.error("",e);
//            model.addAttribute("message", "上传失败:"+e+"!");
        }
        return "upload";
    }

    public String download(String encFile,String zipFile){
        try {
            FtpUtil.ftpDownload("221.238.206.12",27980,"aqj_mgr","password",60000,"/feedbackfile/2017-12-28/" + encFile,"/Users/wangxy/Desktop/test223/" + zipFile);
//            model.addAttribute("message", "下载成功!");
        } catch (Exception e) {
            e.printStackTrace();
//            logger.error("",e);
//            model.addAttribute("message", "下载失败:"+e+"!");
        }
        return "download";
    }
    public void zipAndEnc(String srcF,String dstF){

    }
    public static void main(String[] args){
        //上传方法一：先调用调用FtpUtil的ftpUpload包装后的upload方法，会先生成enc文件，到zip同目录下。然后手动终止FTP连接尝试
        //再手动执行web上传ENC文件。
//        Test t = new Test();
//        String res = t.upload("080450561201801110958120001.zip");
//        System.out.println(res);
//        System.exit(0);
        //上传方法二：直接修改FtpUtil的代码，直接调用FtpCallBack方法pressAndCryMsg为public：
        FtpUtil.FtpCallback upUtil=FtpUtil.FtpCallback.getInstance("upload");
        upUtil.pressAndCryMsg("/Users/finup/PycharmProjects/untitled/080450561201801111058120001.zip","/Users/finup/PycharmProjects/untitled/080450561201801111058120001.enc");
        System.exit(0);
//  if(args[0].equals("upload")){
//            String res = t.upload("121EXPORTTRADEINFO.txt");
//            System.out.println(res);
//        }else {
//            String res = t.download("91110101080450561920171228240011.enc", "91110101080450561920171228240011.zip");
//            System.out.println(res);
//        }


//        FtpUtil.ftpDownload("/Users/wangxy/Desktoptesttest/test123/haha.enc","/Users/wangxy/Desktop/test223/haha1.txt");

//1. 直接抽取对应的方法解密为tmp文件然后从tmp文件解压缩为txt文件
/*        MsgDecryptImpl2 decUtil=new MsgDecryptImpl2();
        try {

            decUtil.decryptMsg("/Users/finup/PycharmProjects/untitled/0804505612018010918131200011.enc","/Users/finup/PycharmProjects/untitled/0804505612018010918131200011.tmp",1);
            CompressUtil.deCompress("/Users/finup/PycharmProjects/untitled/0804505612018010918131200011.tmp","/Users/finup/PycharmProjects/untitled/0804505612018010918131200011.txt");
        } catch (CFCCException e) {
            e.printStackTrace();
        }*/
//2. 直接修改FtpUtil源码将修改deCryAndpressMsg的访问权限为public，然后调用解压缩解密程序
        FtpUtil.FtpCallback downUtil=FtpUtil.FtpCallback.getInstance("download");
        downUtil.deCryAndpressMsg("/Users/finup/PycharmProjects/untitled/0804505612018011109581200011.enc","/Users/finup/PycharmProjects/untitled/0804505612018011109581200011.txt");

    }
}
