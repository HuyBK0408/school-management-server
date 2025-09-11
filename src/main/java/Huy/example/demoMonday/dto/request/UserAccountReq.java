package Huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.NotNull;

public class UserAccountReq {
    @NotNull
    private String username;
    @NotNull private String passwordHash;
    @NotNull private String email;
    @NotNull private String phone;
    @NotNull private Boolean enabled;
    private java.util.UUID schoolId;
    public String getUsername(){ return username; }
    public void setUsername(String v){ this.username=v; }
    public String getPasswordHash(){ return passwordHash; }
    public void setPasswordHash(String v){ this.passwordHash=v; }
    public String getEmail(){ return email; }
    public void setEmail(String v){ this.email=v; }
    public String getPhone(){ return phone; }
    public void setPhone(String v){ this.phone=v; }
    public Boolean getEnabled(){ return enabled; }
    public void setEnabled(Boolean v){ this.enabled=v; }
    public java.util.UUID getSchoolId(){ return schoolId; }
    public void setSchoolId(java.util.UUID v){ this.schoolId=v; }
}
