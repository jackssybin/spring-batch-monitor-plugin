package com.springbatch.monitor.model;

/**
 * 数据源配置模型
 */
public class DataSourceConfig {
    private String id;
    private String name;
    private String driverClassName;
    private String url;
    private String username;
    private String password;
    private DatabaseType databaseType;
    private boolean active;
    private String description;

    public DataSourceConfig() {
    }

    public DataSourceConfig(String id, String name, DatabaseType databaseType, String url, String username, String password) {
        this.id = id;
        this.name = name;
        this.databaseType = databaseType;
        this.url = url;
        this.username = username;
        this.password = password;
        this.driverClassName = databaseType.getDriverClassName();
        this.active = true;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    public void setDatabaseType(DatabaseType databaseType) {
        this.databaseType = databaseType;
        if (databaseType != null) {
            this.driverClassName = databaseType.getDriverClassName();
        }
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return name + " (" + databaseType + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DataSourceConfig that = (DataSourceConfig) obj;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    /**
     * 数据库类型枚举
     */
    public enum DatabaseType {
        MYSQL("MySQL", "com.mysql.cj.jdbc.Driver", "jdbc:mysql://localhost:3306/batch_db"),
        POSTGRESQL("PostgreSQL", "org.postgresql.Driver", "jdbc:postgresql://localhost:5432/batch_db"),
        SQL_SERVER("SQL Server", "com.microsoft.sqlserver.jdbc.SQLServerDriver", "jdbc:sqlserver://localhost:1433;databaseName=batch_db"),
        ORACLE("Oracle", "oracle.jdbc.OracleDriver", "jdbc:oracle:thin:@localhost:1521:xe"),
        H2("H2", "org.h2.Driver", "jdbc:h2:mem:batch_db"),
        SQLITE("SQLite", "org.sqlite.JDBC", "jdbc:sqlite:batch_db.db");

        private final String displayName;
        private final String driverClassName;
        private final String defaultUrl;

        DatabaseType(String displayName, String driverClassName, String defaultUrl) {
            this.displayName = displayName;
            this.driverClassName = driverClassName;
            this.defaultUrl = defaultUrl;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDriverClassName() {
            return driverClassName;
        }

        public String getDefaultUrl() {
            return defaultUrl;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}
