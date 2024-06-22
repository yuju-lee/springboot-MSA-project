package com.sparta.springproject.execption;

public abstract class BaseException extends RuntimeException{
    public abstract BaseExceptionType getExceptionType();

}
