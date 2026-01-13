package com.bing.tpa.exception.global;

import com.bing.tpa.common.Result;
import com.bing.tpa.exception.DigitalException;
import com.bing.tpa.exception.RepeatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalException {
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 返回 500 错误
    public ResponseEntity<Result> handleAllExceptions(Exception ex) {
        // 可以在这里记录错误日志
        System.out.println(ex.getMessage());
        // 返回统一的错误响应
        Result result=new Result<>();
        result.build(null,"111","客户端繁忙！");
        return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // 捕获 SQLException 及其子类异常
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<Result> handleSQLException(SQLException ex) {
        // 构建固定格式的错误响应体，这里假设 ErrorResponse 是自定义的返回给前端的实体类
        Result<Object> result = new Result<>();
        result.setCode("SQL_ERROR");
        result.setMessage("数据库操作异常：" + ex.getMessage());
        result.setData(ex.getSQLState()); // 获取 SQL 状态码等详细信息，可按需取舍

        // 返回给前端，可根据实际需求设置 HTTP 状态码，这里简单设为 500
        return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @ExceptionHandler(RepeatException.class)
    public ResponseEntity<Result> handleRepeatException(RepeatException ex) {
        Result<Object> result = new Result<>();
        result.setCode("REPEAT_OPTION_ERROR");
        result.setMessage("重复操作异常：" + ex.getMessage());
        result.setData(null);
        return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<Result> handleNullPointerException(NullPointerException ex) {
        Result<Object> result = new Result<>();
        result.setCode("NULL_ERROR");
        result.setMessage("空值异常：" + ex.getMessage());
        result.setData(null);
        return new ResponseEntity<>(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMaxSizeException() {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", 407);
        errorResponse.put("message", "File size exceeds the maximum allowed limit");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(DigitalException.class)
    public ResponseEntity<Map<String, Object>> DigitalException(DigitalException de){
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", 406);
        errorResponse.put("message", de.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> RuntimeException(RuntimeException re){
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("status", 405);
        errorResponse.put("message", re.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

}


