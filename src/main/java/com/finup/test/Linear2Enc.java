package com.finup.test;

import com.finup.constant.BankCode9;
import com.finup.constant.UploadType;
import com.finup.utils.DateUtil;
import com.finup.utils.FtpUtil;
import com.finup.utils.SplitCSV;
import com.finup.utils.ZipUtil;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @Author Mr.An
 * @Date 18/5/4 上午10:09
 * 从csv文件－》分割为小文件－》更名121EXPORTTRADEINFO.txt－》压缩包->生成ENC压缩文件列表
 */
public class Linear2Enc {
    private String prefix_Rongzi_Name= BankCode9.BANK_CODE_9.code+ DateUtil.getCurrentDateTimeUtilMinute()+ UploadType.fromName("债券融资").id;
    private String prefix_Shanchu_Name= BankCode9.BANK_CODE_9.code+ DateUtil.getCurrentDateTimeUtilMinute()+ UploadType.fromName("删除").id;
    private FtpUtil.FtpCallback upUtil=FtpUtil.FtpCallback.getInstance("upload");
    FtpUtil.FtpCallback downUtil=FtpUtil.FtpCallback.getInstance("download");

    /**
     * @param subPath receive non－split csv Path.
     *                this method mainly used when csv file is below 50 MB.
     * @return   zipfile Path.
     */
    private Path getZipFilePath(Path subPath,boolean isRongzi){

        if(isRongzi){
            String newZipName = subPath.getParent().toString() + "/" + prefix_Rongzi_Name + "0001.zip";
            return Paths.get(newZipName);
        }else{
            String newZipName = subPath.getParent().toString() + "/" + prefix_Shanchu_Name + "0001.zip";
            return Paths.get(newZipName);
        }

    }

    /**
     * @param subPath  non-split csv Path with default Rongzi style.
     * @return
     */
    private Path getDefaultZipFilePath(Path subPath){
        return getZipFilePath(subPath,true);
    }

    /**
     * @param csvPaths (csv split into small splits and return a path list used in here)
     * @return  new Split csv files related zipfile path list.
     */
    private List<Path> getZipListPaths(List<Path> csvPaths,boolean isRongzi){
        ArrayList<Path> listZipPath = new ArrayList<>();
        int len=csvPaths.size();
        if(len>9999){
            throw new RuntimeException("csvPaths is over long,can not support so many splits");
        }
        if(isRongzi){

            for(int i=0;i<len;i++){
                String newZipName = csvPaths.get(i).getParent().toString() + "/" + prefix_Rongzi_Name + generateIndex(i) + ".zip";
                listZipPath.add(Paths.get(newZipName));
            }

        }else {

            for(int i=0;i<len;i++){
                String newZipName = csvPaths.get(i).getParent().toString() + "/" + prefix_Shanchu_Name + generateIndex(i) + ".zip";
                listZipPath.add(Paths.get(newZipName));
            }

        }

        return listZipPath;
    }


    /**
     * @param csvPaths 根据切分后的csv path列表，获取对应的默认zip path列表
     * @return
     */
    private List<Path> getDefaultZipListPaths(List<Path> csvPaths){
        return getZipListPaths( csvPaths,true);
    }


    /**
     * @param srcPath
     * @param isRongzi
     * @param splitNum
     * @throws IOException
     * the main method do enc .
     */
    public void doEnc(String srcPath,boolean isRongzi, int splitNum) throws IOException {
        List<Path> pathList=SplitCSV.split2N(srcPath,splitNum);
        List<Path> listZipPaths=null;
        if(isRongzi){
            listZipPaths = getDefaultZipListPaths(pathList);
        }else {
            listZipPaths = getZipListPaths(pathList,false);
        }
        int len=pathList.size();
        for (int i=0;i<len;i++){
            Path tempTxtPath = rename(pathList.get(i), isRongzi);
            Path newZipFile = ZipUtil.newInstance().newZipFile(tempTxtPath, listZipPaths.get(i));
            String newZipPathStr = newZipFile.toAbsolutePath().toString();
            String newEncStr = newZipPathStr.substring(0, newZipPathStr.lastIndexOf(".zip")) + ".enc";
            upUtil.pressAndCryMsg(newZipPathStr,newEncStr);
            Files.delete(Paths.get(newZipPathStr.substring(0, newZipPathStr.lastIndexOf(".zip"))+ ".tmp"));
        }

    }

    public void doDefaultEnc(String srcPath) throws IOException {
        doEnc(srcPath,true,1);
    }

    /**
     * @param encPath 传入压缩文件的路径
     * @return 返回解压文件的地址
     */
    public String doDec(String encPath){
        String newTxtFile = encPath.substring(0, encPath.lastIndexOf(".enc")) + ".txt";
        if(!encPath.endsWith(".enc")){
            throw new RuntimeException("dec file must end with .enc");
        }
        downUtil.deCryAndpressMsg(encPath,newTxtFile);
        return newTxtFile;
    }

    /**
     * @param path
     * @param isRongzi
     */
    private Path rename(Path path,boolean isRongzi) {
        try {

            if(isRongzi){
                return Files.move(path,Paths.get(path.getParent().toString()+"/"+UploadType.fromId("12").fname));
            }else {
                return Files.move(path,Paths.get(path.getParent().toString()+"/"+UploadType.fromId("00").fname));
            }

        } catch (IOException e) {
                e.printStackTrace();
        }
        return null;
    }


    private String generateIndex(int i) {
        String newI = i + 1 + "";
        for(int l=newI.length();l<4;l++){
            newI=0+newI;
        }

        return newI;
    }


    public static void main(String[] args) {
//        new Linear2Enc().doDec("/Users/finup/Desktop/0804505612018032214181200021.enc");
//        System.exit(0);
        try {
//            new Linear2Enc().doDefaultEnc("/Users/finup/Desktop/gongxiangpingtaishangbao20180430.csv");
              new Linear2Enc().doEnc("/Users/finup/Desktop/gongxiangpingtaishangbao20180430.csv",true,4);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
