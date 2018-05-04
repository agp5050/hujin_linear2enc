package com.finup.utils;


import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author Mr.An
 * @Date 18/5/3 下午5:26
 */
public  class SplitCSV {

    public static List<Path> split2(String srcPath) throws IOException {
        ArrayList<String> arrayList = new ArrayList<>();
        ArrayList<Path> listPath = new ArrayList<>();
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(srcPath),Charset.forName("gbk"));
        String line;
        while((line=bufferedReader.readLine())!=null){
            line=line.replaceAll("\"","");
            arrayList.add(line+"\n");
        }
        bufferedReader.close();
        int size = arrayList.size();
        int splitSize=size/2;
        List<String> subList0 = arrayList.subList(1, splitSize);
        List<String> subList1 = arrayList.subList(splitSize, size);
        String pathWithoutSuffix = srcPath.substring(0, srcPath.lastIndexOf(".csv"));
        for(int i=0;i<2;i++){
            String dstPath=pathWithoutSuffix+"_split"+i+".csv";
            BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(dstPath),Charset.forName("gbk"));
            if(i==0){
                for (String line0:subList0){
                    bufferedWriter.write(line0);
                }
                bufferedWriter.flush();
                listPath.add(Paths.get(dstPath));
            }

            if(i==1){
                for (String line1:subList1){
                    bufferedWriter.write(line1);
                }
                bufferedWriter.flush();
            }
            bufferedWriter.close();
            listPath.add(Paths.get(dstPath));
        }

        return listPath;
    }

    public static List<Path> split2ten(String srcPath) throws IOException {
        ArrayList<String> arrayList = new ArrayList<>();
        ArrayList<Path> listPath = new ArrayList<>();
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(srcPath),Charset.forName("gbk"));
        String line;
        while((line=bufferedReader.readLine())!=null){
            line=line.replaceAll("\"","");
            arrayList.add(line+"\n");
        }
        bufferedReader.close();
        int size = arrayList.size();
        int splitSize=size/10;
        System.out.println(size+"///"+splitSize);
        String pathWithoutSuffix = srcPath.substring(0, srcPath.lastIndexOf(".csv"));
        for(int i=0;i<10;i++){
            String dstPath=pathWithoutSuffix+"_split"+i+".csv";
            BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(dstPath), Charset.forName("gbk"));
            if(i==0){
                for (String line0:arrayList.subList(1, splitSize)){
                    bufferedWriter.write(line0);
                }
                bufferedWriter.flush();
                listPath.add(Paths.get(dstPath));
            }else if(i==9){
                for (String line0:arrayList.subList(splitSize*9, size)){
                    bufferedWriter.write(line0);
                }
                bufferedWriter.flush();
                listPath.add(Paths.get(dstPath));
            }else{
                for (String line0:arrayList.subList(splitSize*i, splitSize*(i+1))){
                    bufferedWriter.write(line0);
                }
                bufferedWriter.flush();
                listPath.add(Paths.get(dstPath));
            }


            bufferedWriter.close();
        }
        return listPath;
    }

    public static List<Path> split2N(String srcPath,int n) throws IOException {
        if(n<1||n>999){
            throw new RuntimeException("n must positive and below 999");
        }
        ArrayList<String> arrayList = new ArrayList<>();
        ArrayList<Path> listPath = new ArrayList<>();
        BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(srcPath),Charset.forName("gbk"));
        String line;
        while((line=bufferedReader.readLine())!=null){
            line=line.replaceAll("\"","");
            arrayList.add(line+"\n");
        }
        bufferedReader.close();
        int size = arrayList.size();
        if(size<=n){
            n=size;
        }
        int splitSize=size/n;
        System.out.println(size+"///"+splitSize);
        String pathWithoutSuffix = srcPath.substring(0, srcPath.lastIndexOf(".csv"));
        for(int i=0;i<n;i++){
            String dstPath=pathWithoutSuffix+"_split"+i+".csv";
            BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(dstPath), Charset.forName("gbk"));
            if(i==0){
                for (String line0:arrayList.subList(1, splitSize)){
                    bufferedWriter.write(line0);
                }
                bufferedWriter.flush();
                listPath.add(Paths.get(dstPath));
            }else if(i==n-1){
                for (String line0:arrayList.subList(splitSize*(n-1), size)){
                    bufferedWriter.write(line0);
                }
                bufferedWriter.flush();
                listPath.add(Paths.get(dstPath));
            }else{
                for (String line0:arrayList.subList(splitSize*i, splitSize*(i+1))){
                    bufferedWriter.write(line0);
                }
                bufferedWriter.flush();
                listPath.add(Paths.get(dstPath));
            }


            bufferedWriter.close();
        }
        return listPath;
    }

    public static void main(String[] args) throws IOException {
//        SplitCSV.split2("/Users/finup/Desktop/abc.csv");
//                SplitCSV.split2("/Users/finup/Desktop/gongxiangpingtaishangbao20180430.csv");

//        SplitCSV.split2ten("/Users/finup/Desktop/gongxiangpingtaishangbao20180430.csv");
        SplitCSV.split2N("/Users/finup/Desktop/gongxiangpingtaishangbao20180430.csv",1);
        ArrayList list = new ArrayList();
        list.add("abc");
        List list1 = list.subList(0, 0);
        System.out.println(list1);
    }
}

    