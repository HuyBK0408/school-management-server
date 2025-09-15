package huy.example.demoMonday.config;
import com.zaxxer.hikari.HikariConfig;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.url}")
    private String targetUrl;

    @Value("${spring.datasource.username}")
    private String targetUser;

    @Value("${spring.datasource.password}")
    private String targetPass;

    @Value("${app.db.admin-url}")
    private String adminUrl;        // jdbc:postgresql://localhost:5432/postgres

    @Value("${app.db.admin-username}")
    private String adminUser;       // postgres

    @Value("${app.db.admin-password}")
    private String adminPass;       // mật khẩu

    @Primary
    @Bean
    public DataSource dataSource() throws Exception {
        String dbName = extractDbName(targetUrl);

        // 1) Thử kết nối DB mục tiêu
        try (Connection ignored = DriverManager.getConnection(targetUrl, targetUser, targetPass)) {
            // OK -> database đã tồn tại
        } catch (SQLException e) { // <--- dùng SQLException
            // SQLState "3D000" = database does not exist (PostgreSQL)
            if ("3D000".equals(e.getSQLState())) {
                // 2) Tạo DB bằng admin connection
                try (Connection admin = DriverManager.getConnection(adminUrl, adminUser, adminPass);
                     Statement st = admin.createStatement()) {
                    st.execute("CREATE DATABASE \"" + dbName + "\"");
                }
                // 3) Tạo extension trong DB mới
                try (Connection target = DriverManager.getConnection(targetUrl, targetUser, targetPass);
                     Statement st2 = target.createStatement()) {
                    st2.execute("CREATE EXTENSION IF NOT EXISTS pgcrypto");
                }
            } else {
                throw e; // lỗi khác -> ném ra
            }
        }

        // 4) Trả về Hikari DataSource
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(targetUrl);
        cfg.setUsername(targetUser);
        cfg.setPassword(targetPass);
        return new HikariDataSource(cfg);
    }

    private String extractDbName(String jdbcUrl) {
        // jdbc:postgresql://host:port/dbname?params
        String s = jdbcUrl.substring("jdbc:postgresql://".length());
        int slash = s.indexOf('/');
        String after = s.substring(slash + 1);
        int q = after.indexOf('?');
        return (q >= 0) ? after.substring(0, q) : after;
    }
}