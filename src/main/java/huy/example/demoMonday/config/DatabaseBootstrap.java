package huy.example.demoMonday.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class DatabaseBootstrap implements InitializingBean {

    private final DataSource adminDsPostgres;
    private final DataSource adminDsTarget;
    private final DataSource appDs;

    @Value("${app.db.target-db}")
    private String targetDb;

    @Value("${app.db.target-schema:public}")
    private String targetSchema;

    @Value("${app.db.app-username}")
    private String appUsername;

    public DatabaseBootstrap(@Qualifier("adminDataSourcePostgres") DataSource adminDsPostgres,
                             @Qualifier("adminDataSourceTarget") DataSource adminDsTarget,
                             DataSource appDs) {
        this.adminDsPostgres = adminDsPostgres;
        this.adminDsTarget = adminDsTarget;
        this.appDs = appDs;
    }

    @Override
    public void afterPropertiesSet() {
        // 1) Tạo DB nếu thiếu (chạy trên postgres)
        JdbcTemplate adminPg = new JdbcTemplate(adminDsPostgres);
        adminPg.execute("""
            DO $$
            BEGIN
              IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = %s) THEN
                EXECUTE format('CREATE DATABASE %s', %s);
              END IF;
            END$$;
            """.formatted(quoteLiteral(targetDb), ident(targetDb), quoteLiteral(targetDb))
        );

        // 2) Trong DB target: tạo schema app (nếu khác public) + cấp quyền cho user ứng dụng
        JdbcTemplate adminTarget = new JdbcTemplate(adminDsTarget);

        if (!"public".equalsIgnoreCase(targetSchema)) {
            adminTarget.execute("CREATE SCHEMA IF NOT EXISTS " + ident(targetSchema));
            adminTarget.execute("ALTER SCHEMA " + ident(targetSchema) + " OWNER TO " + ident(appUsername));
            adminTarget.execute("GRANT USAGE, CREATE ON SCHEMA " + ident(targetSchema) + " TO " + ident(appUsername));
        } else {
            // public: đảm bảo user có quyền tạo đối tượng
            adminTarget.execute("GRANT USAGE, CREATE ON SCHEMA public TO " + ident(appUsername));
        }

        // Đảm bảo quyền cơ bản trong DB
        adminTarget.execute("GRANT CONNECT ON DATABASE " + ident(targetDb) + " TO " + ident(appUsername));
        adminTarget.execute("GRANT TEMP ON DATABASE " + ident(targetDb) + " TO " + ident(appUsername));

        // Set search_path cho user ứng dụng trong DB target
        adminTarget.execute("""
            ALTER ROLE %s IN DATABASE %s SET search_path TO %s, public;
            """.formatted(ident(appUsername), ident(targetDb), ident(targetSchema))
        );

        // 3) Chạy schema.sql bằng app DataSource
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.setSqlScriptEncoding("UTF-8");
        populator.addScript(new ClassPathResource("schema.sql"));
        populator.execute(appDs);
    }

    private static String ident(String name) {
        return "\"" + name.replace("\"", "\"\"") + "\"";
    }

    private static String quoteLiteral(String s) {
        return "'" + s.replace("'", "''") + "'";
    }
}
