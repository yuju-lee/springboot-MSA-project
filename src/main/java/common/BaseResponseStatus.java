package common;


import lombok.Getter;

@Getter
public enum BaseResponseStatus {
    INVALID_JWT,
    REDIS_ERROR
}