package com.hbin.mall.common.event;

import lombok.Data;

import java.io.Serializable;

@Data
public class NotificationEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long memberId;
    private String title;
    private String content;
    private Integer type;
    private String category;
    private String targetUrl;
    private Long relatedId;
}