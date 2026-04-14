package com.volleyball.volleyballcommunitybackend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "user_privacy")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPrivacy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "follow_list_visible", nullable = false)
    private Boolean followListVisible = true;

    @Column(name = "follower_list_visible", nullable = false)
    private Boolean followerListVisible = true;

    @Column(name = "friends_only_receive", nullable = false)
    private Boolean friendsOnlyReceive = false;
}
