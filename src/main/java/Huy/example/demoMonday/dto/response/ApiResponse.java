package Huy.example.demoMonday.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.domain.Page;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private String errorCode;
    private T data;
    private PageMeta page;
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public String getErrorCode() { return errorCode; }
    public T getData() { return data; }
    public PageMeta getPage() { return page; }

    public static class Build<T> {
        private final ApiResponse<T> r = new ApiResponse<>();
        public Build<T> success(boolean v){ r.success=v; return this; }
        public Build<T> message(String m){ r.message=m; return this; }
        public Build<T> errorCode(String c){ r.errorCode=c; return this; }
        public Build<T> data(T d){ r.data=d; return this; }
        public Build<T> page(PageMeta p){ r.page=p; return this; }
        public Build<T> ok(){ r.success=true; return this; }
        public Build<T> ok(T d){ r.success=true; r.data=d; return this; }
        public Build<T> fail(String code,String msg){ r.success=false; r.errorCode=code; r.message=msg; return this; }
        public ApiResponse<T> done(){ return r; }
    }
    public static <T> Build<T> build(){ return new Build<>(); }

    public static class PageMeta {
        private final int page, size, totalPages;
        private final long totalElements;
        public PageMeta(int page,int size,long totalElements,int totalPages){
            this.page=page; this.size=size; this.totalElements=totalElements; this.totalPages=totalPages;
        }
        public int getPage(){ return page; } public int getSize(){ return size; }
        public long getTotalElements(){ return totalElements; } public int getTotalPages(){ return totalPages; }
        public static PageMeta from(Page<?> p){ return new PageMeta(p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages()); }
    }
}
