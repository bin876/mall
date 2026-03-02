package com.hbin.mall.product.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class Attr implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 属性ID
     */
    @TableId(value = "attr_id", type = IdType.AUTO)
    private Long attrId;

    /**
     * 属性名称
     */
    private String attrName;

    /**
     * 类型（0-销售属性，1-基本属性）
     */
    private Integer attrType;

    /**
     * 值类型（0-单选，1-多选）
     */
    private Integer valueType;

    /**
     * 可选值列表（逗号分隔）
     */
    private String valueList;

    /**
     * 分类ID
     */
    private Long categoryId;
}
