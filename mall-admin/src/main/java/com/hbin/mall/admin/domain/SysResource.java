package com.hbin.mall.admin.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@TableName("sys_resource")
public class SysResource implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 资源ID
     */
    @TableId(value = "resource_id", type = IdType.AUTO)
    private Long resourceId;

    /**
     * API路径
     */
    private String path;

    /**
     * HTTP方法
     */
    private String method;

    /**
     * 权限标识
     */
    private String permissionCode;

    /**
     * 资源名称
     */
    private String name;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
