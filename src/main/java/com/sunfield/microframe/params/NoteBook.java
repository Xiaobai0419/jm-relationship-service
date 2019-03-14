package com.sunfield.microframe.params;

import com.sunfield.microframe.domain.JmAppUser;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@ApiModel(value="NoteBook", description="")
public class NoteBook {

    @ApiModelProperty(value="登录用户id", dataType="String")
    private String userId;

    @ApiModelProperty(value="通讯录用户信息：mobile：手机号（必传），nickName：昵称", dataType="List<JmAppUser>")
    private List<JmAppUser> noteBookUsers;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<JmAppUser> getNoteBookUsers() {
        return noteBookUsers;
    }

    public void setNoteBookUsers(List<JmAppUser> noteBookUsers) {
        this.noteBookUsers = noteBookUsers;
    }
}
