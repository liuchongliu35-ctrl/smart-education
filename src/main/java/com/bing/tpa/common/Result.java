package com.bing.tpa.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.stereotype.Component;

@Setter
@Getter
@ToString(callSuper = true)
@AllArgsConstructor
@Component
public class Result<T>{
    private String code;
    private String message;
//    data装着后端返回给前端的数据
    private T data;

    public Result() {
    }
//    这个build方法用来给data赋值
 protected  <T> Result<T> build(T data){
        Result<T> result=new Result<T>();
        if(data!=null){
            result.setData(data);
        }
        return result;
 }
// 不通过枚举
// 这个build方法不用枚举来设置code和message的值，而是自定义这两个变量的值
 public  <T> Result<T> build(T body,String code,String message){
        Result<T> result=build(body);
        result.setCode(code);
        result.setMessage(message);
        return result;
 }
// 通过枚举
// 这个build方法用来将枚举出来的状态码code和message赋给result这个对象中的code和message，返回一个Result对象
 public  <T> Result<T> build(T body,ResultCodeEnum resultCodeEnum){
        Result<T> result=build(body);
        result.setCode(resultCodeEnum.getCode());
        result.setMessage(resultCodeEnum.getMessage());
        return result;
 }
// 成功获取大模型返回结果是调用success
 public  <T> Result<T> success(T data){
     Result<T> result = build(data);
     return build(data,ResultCodeEnum.SUCCESS);
 }
// 调用大模型出现问题是调用fail
 public <T> Result<T> fail(T data,String msg){
        Result<T> result=build(data);
        result.setMessage(msg);
        return build(data,ResultCodeEnum.FAIL);
 }
 public Result<T> message(String msg){
        this.setMessage(msg);
        return this;
 }
 public Result<T> code(String code){
        this.setCode(code);
        return this;
 }
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
