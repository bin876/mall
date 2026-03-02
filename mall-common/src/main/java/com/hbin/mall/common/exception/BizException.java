package com.hbin.mall.common.exception;

/**
 * 业务异常类
 * 用于处理业务逻辑中的异常情况，不会触发事务回滚（除非特别指定）
 */
public class BizException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 错误码
     */
    private final int code;
    
    /**
     * 详细数据（可选）
     */
    private Object data;

    /**
     * 构造方法（使用默认错误码）
     * @param message 错误消息
     */
    public BizException(String message) {
        this(ErrorCode.BIZ_ERROR.getCode(), message);
    }
    
    /**
     * 构造方法（自定义错误码和消息）
     * @param code 错误码
     * @param message 错误消息
     */
    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }
    
    /**
     * 构造方法（自定义错误码、消息和数据）
     * @param code 错误码
     * @param message 错误消息
     * @param data 详细数据
     */
    public BizException(int code, String message, Object data) {
        super(message);
        this.code = code;
        this.data = data;
    }
    
    /**
     * 获取错误码
     * @return 错误码
     */
    public int getCode() {
        return code;
    }
    
    /**
     * 获取详细数据
     * @return 详细数据
     */
    public Object getData() {
        return data;
    }
    
    /**
     * 设置详细数据
     * @param data 详细数据
     */
    public void setData(Object data) {
        this.data = data;
    }
    
    @Override
    public String toString() {
        return "BizException{" +
                "code=" + code +
                ", message='" + getMessage() + '\'' +
                ", data=" + data +
                '}';
    }
}