package Huy.example.demoMonday.dto.request;

import jakarta.validation.constraints.NotNull;

public class TermReq {
    @NotNull
    private String name;
    @NotNull private Integer orderNo;
    @NotNull private java.util.UUID schoolYearId;
    public String getName(){ return name; }
    public void setName(String v){ this.name=v; }
    public Integer getOrderNo(){ return orderNo; }
    public void setOrderNo(Integer v){ this.orderNo=v; }
    public java.util.UUID getSchoolYearId(){ return schoolYearId; }
    public void setSchoolYearId(java.util.UUID v){ this.schoolYearId=v; }
}
