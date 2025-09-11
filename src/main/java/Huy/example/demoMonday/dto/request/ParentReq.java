package Huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.NotNull;

public class ParentReq {
    @NotNull
    private String fullName;
    @NotNull private Huy.example.demoMonday.enums.RelationType relationType;
    @NotNull private String phone;
    @NotNull private String email;
    @NotNull private String address;
    private java.util.UUID userId;
    public String getFullName(){ return fullName; }
    public void setFullName(String v){ this.fullName=v; }
    public Huy.example.demoMonday.enums.RelationType getRelationType(){ return relationType; }
    public void setRelationType(Huy.example.demoMonday.enums.RelationType v){ this.relationType=v; }
    public String getPhone(){ return phone; }
    public void setPhone(String v){ this.phone=v; }
    public String getEmail(){ return email; }
    public void setEmail(String v){ this.email=v; }
    public String getAddress(){ return address; }
    public void setAddress(String v){ this.address=v; }
    public java.util.UUID getUserId(){ return userId; }
    public void setUserId(java.util.UUID v){ this.userId=v; }
}
