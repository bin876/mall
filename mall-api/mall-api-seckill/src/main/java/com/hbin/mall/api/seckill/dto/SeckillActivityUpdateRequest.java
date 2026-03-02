package com.hbin.mall.api.seckill.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SeckillActivityUpdateRequest {
    private Long id;
    
    @NotBlank(message = "活动名称不能为空")
    @Size(max = 50, message = "活动名称不能超过50个字符")
    private String name;
    
    @NotNull(message = "开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;
    
    @NotNull(message = "结束时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;
    
    @NotEmpty(message = "至少需要选择一个商品")
    private List<SeckillSkuCreateDTO> skus;
}