package com.finup.test;
import com.finup.utils.DateUtil;
import com.finup.utils.FtpUtil;
import org.junit.jupiter.api.Test;
//import com.icfcc.batch.compress.CompressUtil;
//import com.icfcc.batch.crypt.MsgDecryptImpl2;
//import com.icfcc.foundation.exception.CFCCException;
public class Test_web {
    /**
     * 将txt文件压缩并加密为Enc文件
     * 由于FtpUtil没有直接将txt文件压缩为Zip包的方法
     * 需要手动压缩为特定名称的zip包
     */
//    @Test
    public void testZipAndENC(){
        FtpUtil.FtpCallback upUtil=FtpUtil.FtpCallback.getInstance("upload");
        // 新增更新数据
        upUtil.pressAndCryMsg("/Users/finup/PycharmProjects/untitled/080450561201805031659120001.zip","/Users/finup/PycharmProjects/untitled/080450561201805031659120001.enc");
        //删除数据
//        upUtil.pressAndCryMsg("/Users/finup/PycharmProjects/untitled/080450561201801121833120001.zip","/Users/finup/PycharmProjects/untitled/080450561201801121833120001.enc");

    }

    /**
     * 解压web下载的Enc文件为txt文件
     */
    //
//    @Test
    public void testUnzipAndDec(){
        FtpUtil.FtpCallback downUtil=FtpUtil.FtpCallback.getInstance("download");
//        downUtil.deCryAndpressMsg("/Users/finup/PycharmProjects/untitled/0804505612018011109581200011.enc","/Users/finup/PycharmProjects/untitled/0804505612018011109581200011.txt");
        downUtil.deCryAndpressMsg("/Users/finup/PycharmProjects/untitled/0804505612018032214181200021.enc","/Users/finup/PycharmProjects/untitled/0804505612018032214181200021.txt");

    }

    public static void main(String[] args) {
        String currentDateTimeUtilMinute = DateUtil.getCurrentDateTimeUtilMinute();
        System.out.println(currentDateTimeUtilMinute);
    }

}
