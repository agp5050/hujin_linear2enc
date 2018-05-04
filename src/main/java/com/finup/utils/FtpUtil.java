package com.finup.utils;

import com.icfcc.batch.center.PreConditionCheck;
import com.jcraft.jsch.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.*;
import org.slf4j.Logger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by chengyu on 2017/11/7
 */
public class FtpUtil {

    private static final Logger logger = LoggerFactory.getLogger(FtpUtil.class);

    //ftp服务器默认路劲
    private static final String PATH = "/datafile";
    //压缩文件后缀
    private static final String COMPRESSION_SUFFIX = ".tmp";
    //加密文件后缀
    static final String ENCRYPTION_SUFFIX = ".enc";

    private static final String CONFIG_PATH = new Object() {
        String getPath() {
            return this.getClass().getResource("/").getPath();
        }
    }.getPath();

    /**
     * ftp上传
     * @param host ip
     * @param port 端口
     * @param userName 用户名
     * @param password 密码
     * @param timeout 上传超时时间(毫秒)
     * @param filePath zip压缩文件的路径地址
     * @return
     */
    public static void ftpUpload(String host, int port, String userName, String password, int timeout, String filePath) throws Exception {

        filePath = filePath.replaceAll("\\\\","/");

        FtpCallback ftpCallback = FtpCallback.getInstance(FtpCallback.FTP_UPLOAD);
        if(ftpCallback == null) throw new RuntimeException("未能获取到FtpCallback实例");

        if (!ftpCallback.paramValidate(host, port, userName, Collections.singletonList(filePath))) throw new RuntimeException("请求参数验证失败！");

        if(!ftpCallback.existLocalFile(Collections.singletonList(filePath))) throw new RuntimeException("文件不存在！");

        //源文件名路径不带后缀
        String path = ftpCallback.getFilePatn(filePath);

        //文件加密
        ftpCallback.pressAndCryMsg(filePath,path + ENCRYPTION_SUFFIX);

        //上传文件
        ftpCallback.upload(host, port, userName, password, timeout, filePath);

        //删除加密生产的文件
        ftpCallback.deleteLoaclFile(Arrays.asList(path + ENCRYPTION_SUFFIX,path + COMPRESSION_SUFFIX));
    }

    /**
     * ftp下载
     * @param host IP
     * @param port 端口
     * @param userName 用户名
     * @param password 密码
     * @param timeout 超时时间
     * @param sourcePath 源文件路径（服务器下载路径 xxxx/xxxx/xxx.enc）
     * @param targetPath 目标路径（下载到本地路径 xxxx/xxxx/xxx.zip）
     * @throws FileNotFoundException
     * @throws JSchException
     * @throws SftpException
     */
    public static void ftpDownload(String host, int port, String userName, String password, int timeout, String sourcePath, String targetPath) throws FileNotFoundException, JSchException, SftpException {

        sourcePath = sourcePath.replaceAll("\\\\","/");
        targetPath = targetPath.replaceAll("\\\\","/");

        FtpCallback ftpCallback = FtpCallback.getInstance(FtpCallback.FTP_DOWNLOAD);
        if(ftpCallback == null) throw new RuntimeException("未能获取到FtpCallback实例");

        if (!ftpCallback.paramValidate(host, port, userName, Arrays.asList(sourcePath,targetPath))) throw new RuntimeException("请求参数验证失败！");

        ftpCallback.createLocalFile(Collections.singletonList(targetPath.substring(0, targetPath.lastIndexOf("/"))));

        //源文件名路径不带后缀
        String sourceFilePatn = ftpCallback.getFilePatn(sourcePath);

        //目标文件名路径不带后缀
        String targetFilePatn = ftpCallback.getFilePatn(targetPath);

        try {
            //设置连接
            ftpCallback.setConnection(host, port, userName, password, timeout);

            if(!ftpCallback.existFtpFile(Arrays.asList(sourcePath,sourceFilePatn + ".ok"),ftpCallback.getSftp())) throw new RuntimeException("下载源文件路径不存在或者文件尚未生成！");

            //下载
            ftpCallback.download(host, port, userName, password, timeout, sourceFilePatn + ENCRYPTION_SUFFIX, targetFilePatn + ENCRYPTION_SUFFIX);

            //解密文件
            ftpCallback.deCryAndpressMsg(targetFilePatn + ENCRYPTION_SUFFIX,targetPath);

            //删除解密生产的文件
            ftpCallback.deleteLoaclFile(Arrays.asList(targetFilePatn + ENCRYPTION_SUFFIX,targetFilePatn + COMPRESSION_SUFFIX));
        } finally {
            if (ftpCallback.getSftp() != null) ftpCallback.getSftp().quit();
            ftpCallback.closeChannel(ftpCallback.getSftp(),ftpCallback.getSession());
        }
    }

    public static abstract class  FtpCallback {
        //上传
        private static final String FTP_UPLOAD = "upload";
        //下载
        private static final String FTP_DOWNLOAD = "download";

        private ThreadLocal<Session> session = new ThreadLocal<>();
        private ThreadLocal<ChannelSftp> sftp = new ThreadLocal<>();

        public void pressAndCryMsg(String sourceFile, String desFile){}

        public void deCryAndpressMsg(String sourceFile, String desFile){}

        protected void upload(String host, int port, String userName,String password,int timeout,String sourcePath) throws JSchException, IOException, SftpException{}

        protected void download(String host, int port, String userName,String password,int timeout,String sourcePath,String targetPath) throws JSchException, FileNotFoundException, SftpException{}

        protected void download(String host, int port, String userName, String password, int timeout, String sourcePath) throws JSchException, FileNotFoundException, SftpException{
        }

        public static FtpCallback getInstance(String type){
            if(FTP_UPLOAD.equals(type)){
                return new FtpCallback() {
                    @Override
                    public void pressAndCryMsg(String sourceFile, String desFile) {
                        super.pressAndCryMsg(sourceFile, desFile);
                        //初始化的包含batch.xml,.keystore,public.key的conf路径必须通过此进行初始化
                        PreConditionCheck prc = new PreConditionCheck(CONFIG_PATH);
                        String tmpFile = getFilePatn(sourceFile) + COMPRESSION_SUFFIX;
                        //压缩
                        if(!prc.compressFile(sourceFile, tmpFile)) throw new RuntimeException("文件压缩异常！");
                        //加密
                        if(!prc.cryptMsg(tmpFile,desFile)) throw new RuntimeException("文件加密异常！");
                    }

                    @Override
                    public void upload(String host, int port, String userName, String password, int timeout, String sourcePath) throws JSchException, IOException, SftpException {
                        super.upload(host, port, userName, password, timeout, sourcePath);
                        try {
                            setConnection(host, port, userName, password, timeout);
                            //获取ftp服务器路径，没有就创建
                            List<String> targetPath = this.createFtpPath( getSftp(), getFileName(sourcePath));
                            //需要上传的文件
                            List<File> uploadFiles = Arrays.asList(new File(getFilePatn(sourcePath) + ENCRYPTION_SUFFIX), new File(getFilePatn(sourcePath) + ".ok"));
                            //本地创建的.ok文件
                            List<File> createdFile = new ArrayList<>();
                            for (int i = 0; i < uploadFiles.size(); i++) {
                                File file = uploadFiles.get(i);
                                if(!(file.exists() && file.isFile())) {
                                    file.createNewFile();
                                    createdFile.add(file);
                                }
                                getSftp().put(new FileInputStream(file), targetPath.get(i));
                            }
                            for (File file : createdFile) {
                                if(file.exists() && file.isFile()) file.delete();
                            }
                        } finally {
                            if(getSftp() != null) getSftp().quit();
                            closeChannel(getSftp(),getSession());
                        }
                    }
                };
            }else if(FTP_DOWNLOAD.equals(type)){
                return new FtpCallback() {
                    @Override
                    public void deCryAndpressMsg(String sourceFile, String desFile) {
                        super.deCryAndpressMsg(sourceFile, desFile);
                        //初始化的包含batch.xml,.keystore,public.key的conf路径必须通过此进行初始化
                        PreConditionCheck prc = new PreConditionCheck(CONFIG_PATH);
                        String tmpFile = getFilePatn(sourceFile) + COMPRESSION_SUFFIX;
                        //解密
                        if(!prc.decryptMsg(sourceFile, tmpFile)) throw new RuntimeException("文件解密异常！");
                        //解压
                        if(!prc.deCompressFile(tmpFile,desFile)) throw new RuntimeException("文件解压异常！");
                    }

                    @Override
                    protected void download(String host, int port, String userName, String password, int timeout, String sourcePath, String targetPath) throws JSchException, FileNotFoundException, SftpException {
                        super.download(host, port, userName, password, timeout, sourcePath,targetPath);
                        getSftp().get(sourcePath,targetPath);
                    }

                    @Override
                    protected void download(String host, int port, String userName, String password, int timeout, String sourcePath) throws JSchException, FileNotFoundException, SftpException {
                        super.download(host, port, userName, password, timeout, sourcePath);
                        getSftp().get(sourcePath);
                    }
                };
            }
            return null;
        }

        private boolean paramValidate(String host, int port, String userName, List<String> paths) {
            for (String path : paths) {
                if(StringUtils.isBlank(path)) return false;
            }
            return !StringUtils.isBlank(host) && port > 0 && !StringUtils.isBlank(userName) && !paths.isEmpty();
        }

        /**
         * 设置连接
         * @param host
         * @param port
         * @param userName
         * @param password
         * @param timeout
         * @throws JSchException
         */
        void setConnection(String host, int port, String userName, String password, int timeout) throws JSchException {
            //获取链接
            if(session.get() == null){
                session.set(getSession(host, port, userName, password, timeout));
            }
            if(sftp.get() == null){
                sftp.set(getChannel(session.get()));
            }

        }

        /**
         * 获取文件名
         * @param sourcePath
         * @return
         */
        static String getFileName(String sourcePath){
            String fileName = sourcePath.substring(sourcePath.lastIndexOf("/") + 1,sourcePath.lastIndexOf("."));
            if(StringUtils.isBlank(fileName)) throw new RuntimeException("获取文件名失败！");
            return fileName;
        }

        /**
         * 文件名路径不带后缀
         * @param sourcePath
         * @return
         */
        String getFilePatn(String sourcePath){
            String path = sourcePath.substring(0, sourcePath.lastIndexOf("."));
            if(StringUtils.isBlank(path)) throw new RuntimeException("获取文件路径失败！");
            return path;
        }

        /**
         * 文件是否存在
         * @param sourcePath
         * @return
         */
        private boolean existLocalFile(List<String> sourcePath){
            for (String spath : sourcePath) {
                File file = new File(spath);
                if(!file.exists()) return false;
            }
            return true;
        }

        /**
         * 创建本地文件
         * @param sourcePath
         * @return
         */
        private void createLocalFile(List<String> sourcePath){
            for (String spath : sourcePath) {
                File file = new File(spath);
                if(!file.exists()) file.mkdir();
            }
        }

        /**
         * 文件是否存在
         * @param sourcePath
         * @return
         */
        private boolean existFtpFile(List<String> sourcePath,ChannelSftp sftp){
            try {
                String directory = sourcePath.get(0).substring(0,sourcePath.get(0).lastIndexOf("/"));
                //目录是否存在
                sftp.cd(directory);

                //目录文件是否存在
                Vector<ChannelSftp.LsEntry> files = sftp.ls(directory);
                for (String path : sourcePath) {
                    String sourceFileName = path.substring(path.lastIndexOf("/") + 1);
                    boolean exist = false;
                    for (ChannelSftp.LsEntry file : files) {
                        String fileName = file.getFilename();
                        if(fileName.equals(sourceFileName)){
                            exist = true;
                            break;
                        }
                    }
                    if(!exist) return false;
                }
            } catch (SftpException e) {
                logger.error("",e);
                return false;
            }
            return true;
        }

        List<String> createFtpPath(ChannelSftp sftp, String fileName) throws SftpException {
            SimpleDateFormat sdt = new SimpleDateFormat("yyyy-MM-dd");
            String formatDate = sdt.format(new Date());
            String targetPath = PATH + "/" + formatDate + "/";
            try {
                //用是否能进入该目录来判断该目录是否存在
                sftp.cd(targetPath);
            } catch (SftpException e) {
                //上面不能进入则报错，执行以下的创建命令
                sftp.mkdir(targetPath);
            }

            return Arrays.asList(targetPath + fileName + ENCRYPTION_SUFFIX,targetPath + fileName +".ok");
        }

        static Session getSession(String host, int port, String username, String password, int timeout) throws JSchException {
            // 创建JSch对象
            JSch jsch = new JSch();
            // 根据用户名，主机ip，端口获取一个Session对象
            Session session = jsch.getSession(username, host, port);
            // 设置密码
            if(StringUtils.isNotBlank(password)) session.setPassword(password);

            Properties config = new Properties();
            // 设置第一次登陆的时候提示，可选值：(ask | yes | no)
            config.put("StrictHostKeyChecking", "no");
            // 为Session对象设置properties
            session.setConfig(config);
            // 设置timeout时间
            session.setTimeout(timeout);
            // 通过Session建立链接
            session.connect();
            return session;
        }

        static ChannelSftp getChannel(Session session) throws JSchException {
            // 打开SFTP通道
            ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
            // 建立SFTP通道的连接
            channel.connect();
            return channel;
        }

        void deleteLoaclFile(List<String> paths){
            for (String path : paths) {
                File file = new File(path);
                if(file.exists() && file.isFile()) file.delete();
            }
        }

        protected static void deleteFtpFile(List<String> paths,ChannelSftp sftp) throws SftpException {
            for (String path : paths) {
                sftp.rm(path);
            }
        }

        void closeChannel(ChannelSftp sftp, Session session) {
            if (sftp != null) {
                sftp.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }

        Session getSession() {
            return session.get();
        }

        ChannelSftp getSftp() {
            return sftp.get();
        }
    }
}
