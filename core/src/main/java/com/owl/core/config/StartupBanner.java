package com.owl.core.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

@Slf4j
@Component
public class StartupBanner implements CommandLineRunner {

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${spring.application.name:OWL Core}")
    private String applicationName;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Autowired(required = false)
    private DataSource dataSource;

    @Override
    public void run(String... args) {
        printBanner();
        printSystemInfo();
    }

    private void printBanner() {
        String banner = """
                \u001B[36m  ╔══════════════════════════════════╗\u001B[0m
                \u001B[36m  ║                                  ║\u001B[0m
                \u001B[36m  ║     OWL - AI Agent Framework  ║\u001B[0m
                \u001B[36m  ║                                  ║\u001B[0m
                \u001B[36m  ╚══════════════════════════════════╝\u001B[0m
                """;
        System.out.println(banner);
    }

    private void printSystemInfo() {
        String green = "\u001B[32m";
        String cyan = "\u001B[36m";
        String yellow = "\u001B[33m";
        String white = "\u001B[37m";
        String reset = "\u001B[0m";
        String sep = green + "  " + "═".repeat(40) + reset;

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("  %s🚀 Application Started Successfully!%n", green));
        sb.append(sep).append("\n");
        
        sb.append(row(green, reset, white, "📦 Application", applicationName));
        sb.append(row(green, reset, yellow, "🏷️  Profile", activeProfile));
        sb.append(row(green, reset, white, "🌐 Port", serverPort));
        sb.append(row(green, reset, cyan, "🔗 Local URL", "http://localhost:" + serverPort));
        sb.append(row(green, reset, cyan, "🌍 External URL", "http://your-ip:" + serverPort));
        sb.append(row(green, reset, cyan, "📊 H2 Console", "http://localhost:" + serverPort + "/h2-console"));
        
        sb.append(sep).append("\n");
        
        sb.append(row(green, reset, white, "⚡ Java Version", System.getProperty("java.version")));
        sb.append(row(green, reset, white, "🕒 Start Time", java.time.LocalDateTime.now().toString().substring(0, 19)));
        sb.append(row(green, reset, white, "💾 Database", getDatabaseStatus()));

        System.out.println(sb.toString());
        log.info("✅ {} is ready to serve!", applicationName);
    }

    private String row(String color, String reset, String valColor, String label, String value) {
        return String.format("  %s%s:%s %s%s%n", color, label, reset, valColor, value);
    }

    private String getDatabaseStatus() {
        if (dataSource == null) {
            return "Not Configured";
        }
        
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            String dbName = metaData.getDatabaseProductName();
            String dbVersion = metaData.getDatabaseProductVersion();
            return dbName + " " + dbVersion;
        } catch (Exception e) {
            log.warn("Failed to get database status", e);
            return "Connection Failed";
        }
    }

    private String truncate(String str, int maxLength) {
        if (str == null) {
            return "";
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}
