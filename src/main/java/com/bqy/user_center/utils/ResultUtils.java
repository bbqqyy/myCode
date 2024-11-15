package com.bqy.user_center.utils;

/**
 * 返回工具类
 */
public class ResultUtils {
    //成功
    public static <T> BaseResponse<T> success(T data){
        return new BaseResponse<>(0,data,"ok");
    }
    //失败
    public static BaseResponse error(ErrorCode errorCode){
        return new BaseResponse(errorCode.getCode(),null,errorCode.getMsg(),errorCode.getDescription());
    }
    public static  BaseResponse error(ErrorCode errorCode,String message ,String description){
        return new BaseResponse(errorCode.getCode(),null,message,description);
    }

    public static  BaseResponse error(ErrorCode errorCode,String description){
        return new BaseResponse(errorCode.getCode(),null,errorCode.getMsg(),description);
    }

    public static  BaseResponse error(int code,String msg,String description){
        return new BaseResponse(code,null,msg,description);
    }
}
