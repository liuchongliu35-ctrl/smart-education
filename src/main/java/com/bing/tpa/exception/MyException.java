package com.bing.tpa.exception;


import com.bing.tpa.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@ResponseBody
public class MyException<T> {
    @Autowired
    private Result<T> result;

    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<T> nullPointerException(NullPointerException e){
        String message = e.getMessage();
        result.setCode("101");
        result.setMessage("出现空值了！");
        return result;

    }

    @ExceptionHandler(LessonException.class)
    public Result<T> unableToMatchException(LessonException e){
        String message=e.getMessage();
        System.out.println(message);
        result.setCode("400");
        result.setData(null);
        result.setMessage(message);
        return result;
    }

    @ExceptionHandler(DuplicateException.class)
    public Result<T> DuplicateException(DuplicateException e){
        String message=e.getMessage();
        System.out.println(message);
        result.setCode("405");
        result.setData(null);
        result.setMessage(message);
        return result;
    }

    @ExceptionHandler(FormatException.class)
    public Result<T> FormatException(FormatException e){
        String message=e.getMessage();
        System.out.println(message);
        result.setCode("406");
        result.setData(null);
        result.setMessage(message);
        return result;
    }

    @ExceptionHandler( RedisException.class)
    public Result<T>  RedisException(RedisException e){
        String message=e.getMessage();
        System.out.println(message);
        result.setCode("407");
        result.setData(null);
        result.setMessage(message);
        return result;
    }

    @ExceptionHandler( IdentityException.class)
    public Result<T>  IdentException(IdentityException e){
        String message=e.getMessage();
        System.out.println(message);
        result.setCode("408");
        result.setData(null);
        result.setMessage(message);
        return result;
    }

    @ExceptionHandler( RuntimeException.class)
    public Result<T>  runException(RuntimeException e){
        String message=e.getMessage();
        System.out.println(message);
        result.setCode("500");
        result.setData(null);
        result.setMessage(message);
        return result;
    }

    @ExceptionHandler( DatabaseException.class)
    public Result<T>  databaseException(DatabaseException e){
        String message=e.getMessage();
        System.out.println(message);
        result.setCode("405");
        result.setData(null);
        result.setMessage(message);
        return result;
    }

}
