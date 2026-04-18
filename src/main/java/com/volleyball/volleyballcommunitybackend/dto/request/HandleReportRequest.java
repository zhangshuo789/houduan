package com.volleyball.volleyballcommunitybackend.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HandleReportRequest {
    @NotNull(message = "处理结果不能为空")
    private Boolean approved;  // true=确认举报(删除内容), false=驳回举报

    private String result;  // CONTENT_DELETED, WARN_USER, DISMISS (当 approved=true 时)
}
