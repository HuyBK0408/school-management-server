package Huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.NotNull;

public class UserRoleReq {
    @NotNull
    private java.util.UUID userId;
    @NotNull private java.util.UUID roleId;
    public java.util.UUID getUserId(){ return userId; }
    public void setUserId(java.util.UUID v){ this.userId=v; }
    public java.util.UUID getRoleId(){ return roleId; }
    public void setRoleId(java.util.UUID v){ this.roleId=v; }
}
