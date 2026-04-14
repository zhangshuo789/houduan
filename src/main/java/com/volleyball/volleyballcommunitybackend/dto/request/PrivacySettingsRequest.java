package com.volleyball.volleyballcommunitybackend.dto.request;

import lombok.Data;

@Data
public class PrivacySettingsRequest {
    private Boolean followListVisible;
    private Boolean followerListVisible;
    private Boolean friendsOnlyReceive;
}
