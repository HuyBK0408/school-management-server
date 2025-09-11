package Huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.NotNull;

public class RoleReq {
    @NotNull
    private String code;
    public String getCode(){ return code; }
    public void setCode(String v){ this.code=v; }
}
