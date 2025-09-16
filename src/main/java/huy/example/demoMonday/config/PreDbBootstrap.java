package huy.example.demoMonday.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public final class PreDbBootstrap {   // <- phải public

    private static final String ADMIN_URL  = System.getenv().getOrDefault(
            "PG_BOOTSTRAP_URL", "jdbc:postgresql://localhost:5432/postgres");
    private static final String ADMIN_USER = System.getenv().getOrDefault(
            "PG_BOOTSTRAP_USER", "postgres");
    private static final String ADMIN_PASS = System.getenv().getOrDefault(
            "PG_BOOTSTRAP_PASS", "root");

    private static final String APP_DB   = System.getenv().getOrDefault("APP_DB_NAME", "school");
    private static final String APP_USER = System.getenv().getOrDefault("APP_DB_USER", "school");
    private static final String APP_PASS = System.getenv().getOrDefault("APP_DB_PASS", "school");

    public static void run() {         // <- có thể để public cho rõ ràng
        try {
            try (Connection c = DriverManager.getConnection(ADMIN_URL, ADMIN_USER, ADMIN_PASS);
                 Statement st = c.createStatement()) {

                if (!exists(st, "SELECT 1 FROM pg_roles WHERE rolname = '" + APP_USER + "'")) {
                    st.executeUpdate("CREATE ROLE " + q(APP_USER) + " WITH LOGIN PASSWORD '" + APP_PASS + "'");
                    System.out.println("[PreDb] Created role: " + APP_USER);
                }
                if (!exists(st, "SELECT 1 FROM pg_database WHERE datname = '" + APP_DB + "'")) {
                    st.executeUpdate("CREATE DATABASE " + q(APP_DB) + " OWNER " + q(APP_USER));
                    System.out.println("[PreDb] Created database: " + APP_DB);
                } else {
                    st.executeUpdate("ALTER DATABASE " + q(APP_DB) + " OWNER TO " + q(APP_USER));
                }
                st.executeUpdate("GRANT ALL PRIVILEGES ON DATABASE " + q(APP_DB) + " TO " + q(APP_USER));
            }
        } catch (Exception e) {
            System.err.println("[PreDb] Bootstrap skipped/failed: " + e.getMessage());
        }
    }

    private static boolean exists(Statement st, String sql) throws Exception {
        try (ResultSet rs = st.executeQuery(sql)) { return rs.next(); }
    }
    private static String q(String ident) { return "\"" + ident.replace("\"", "\"\"") + "\""; }
}
