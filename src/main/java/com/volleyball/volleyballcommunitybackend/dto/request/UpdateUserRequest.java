package com.volleyball.volleyballcommunitybackend.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Size(min = 2, max = 20, message = "昵称2-20字符")
    private String nickname;

    private String avatar;

    @Size(max = 255, message = "简介最多255字符")
    private String bio;
}
