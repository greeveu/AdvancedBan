package me.leoko.advancedban.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.leoko.advancedban.MethodInterface;
import me.leoko.advancedban.Universal;

public class DynamicDataSource {
    private final HikariConfig config = new HikariConfig();

    public DynamicDataSource() throws ClassNotFoundException {
        MethodInterface mi = Universal.get().getMethods();

        String ip = mi.getString(mi.getMySQLFile(), "MySQL.IP", "Unknown");
        String dbName = mi.getString(mi.getMySQLFile(), "MySQL.DB-Name", "Unknown");
        String usrName = mi.getString(mi.getMySQLFile(), "MySQL.Username", "Unknown");
        String password = mi.getString(mi.getMySQLFile(), "MySQL.Password", "Unknown");
        String properties = mi.getString(mi.getMySQLFile(), "MySQL.Properties", "verifyServerCertificate=false&useSSL=false&useUnicode=true&characterEncoding=utf8");
        int port = mi.getInteger(mi.getMySQLFile(), "MySQL.Port", 3306);

        Class.forName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl("jdbc:mysql://" + ip + ":" + port + "/" + dbName + "?" + properties);
        config.setUsername(usrName);
        config.setPassword(password);
    }

    public HikariDataSource generateDataSource() {
        return new HikariDataSource(config);
    }
}
