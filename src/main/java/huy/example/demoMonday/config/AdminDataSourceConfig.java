package huy.example.demoMonday.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class AdminDataSourceConfig {

    /** DS admin kết nối DB 'postgres' để tạo database nếu thiếu */
    @Bean(name = "adminDataSourcePostgres")
    public DataSource adminDataSourcePostgres(
            @Value("${app.db.admin-url}") String url,
            @Value("${app.db.admin-username}") String username,
            @Value("${app.db.admin-password}") String password
    ) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setMaximumPoolSize(2);
        ds.setPoolName("admin-ds-postgres");
        return ds;
    }

    /** DS admin kết nối trực tiếp DB target (school) — tạo sau khi DB tồn tại */
    @Bean(name = "adminDataSourceTarget")
    public DataSource adminDataSourceTarget(
            @Value("${app.db.target-db}") String db,
            @Value("${app.db.admin-username}") String username,
            @Value("${app.db.admin-password}") String password
    ) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:postgresql://localhost:5432/" + db);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setDriverClassName("org.postgresql.Driver");
        ds.setMaximumPoolSize(2);
        ds.setPoolName("admin-ds-target");
        return ds;
    }
}
