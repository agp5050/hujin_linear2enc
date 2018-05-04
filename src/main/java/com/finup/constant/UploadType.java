package com.finup.constant;

/**
 * @Author Mr.An
 * @Date 18/5/3 下午5:06
 */
public enum UploadType {
    ZHAIQUANRONGZI("12","债券融资","121EXPORTTRADEINFO.txt"),
    SHANCHU("00","删除","000DELETEINFO.txt");

    private UploadType(String id, String name, String fname){
        this.id=id;
        this.name=name;
        this.fname=fname;
    }

    public final String id;
    public final String name;
    public final String fname;


    public static UploadType fromId(String id){
        for(UploadType queryReason: UploadType.values()){
            if(queryReason.id.equals(id)){
                return queryReason;
            }
        }
        return null;
    }

    public static UploadType fromName(String key){
        for(UploadType queryReason: UploadType.values()){
            if(queryReason.name.equals(key)){
                return  queryReason;
            }
        }
        return null;
    }
}
