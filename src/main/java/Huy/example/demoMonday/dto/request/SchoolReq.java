package Huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.NotNull;

public class SchoolReq {
    @NotNull
    private String name;
    @NotNull private String address;
    @NotNull private String phone;
    @NotNull private String email;
    public String getName(){ return name; }
    public void setName(String v){ this.name=v; }
    public String getAddress(){ return address; }
    public void setAddress(String v){ this.address=v; }
    public String getPhone(){ return phone; }
    public void setPhone(String v){ this.phone=v; }
    public String getEmail(){ return email; }
    public void setEmail(String v){ this.email=v; }
}
