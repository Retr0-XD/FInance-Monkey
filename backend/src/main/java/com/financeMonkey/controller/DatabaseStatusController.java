package com.financeMonkey.controller;

import com.financeMonkey.config.DatabaseConnectivityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for database status and diagnostics
 * Provides comprehensive information about database connectivity
 * and network resolution for troubleshooting
 */
@RestController
@RequestMapping("/api/status")
public class DatabaseStatusController {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseStatusController.class);
    
    @Value("${DATABASE_URL:unknown}")
    private String databaseUrl;
    
    @Autowired(required = false)
    private DataSource dataSource;
    
    @Autowired
    private Environment env;
    
    @Autowired(required = false)
    private DatabaseConnectivityManager connectivityManager;
    
    @Value("${app.database.resilient-mode:true}")
    private boolean resilientMode;
    
    /**
     * Provides database connectivity status and diagnostics
     */
    @GetMapping("/database")
    public ResponseEntity<Map<String, Object>> getDatabaseStatus() {
        Map<String, Object> status = new HashMap<>();
        String maskedUrl = databaseUrl.replaceAll(":[^:@]+@", ":****@");
        status.put("database_url_format", maskedUrl);
        status.put("resilientMode", resilientMode);
        
        // Add connectivity manager status if available
        if (connectivityManager != null) {
            status.put("managed", true);
            status.put("available", connectivityManager.isDatabaseAvailable());
            status.put("connectionStatus", connectivityManager.getConnectionStatus());
        } else {
            status.put("managed", false);
        }
        
        try {
            // Extract host from the URL
            String host = extractHostFromUrl(maskedUrl);
            if (host != null) {
                status.put("host", host);
                
                // Try to resolve the hostname
                try {
                    InetAddress address = InetAddress.getByName(host);
                    status.put("resolved_ip", address.getHostAddress());
                    status.put("resolved", true);
                    status.put("hostname_resolvable", true);
                    logger.info("Successfully resolved database hostname {} to {}", host, address.getHostAddress());
                } catch (UnknownHostException e) {
                    status.put("resolved", false);
                    status.put("hostname_resolvable", false);
                    status.put("error", "Cannot resolve hostname: " + e.getMessage());
                    logger.warn("Failed to resolve database hostname {}: {}", host, e.getMessage());
                }
            }
            
            // Test direct database connection if dataSource is available
            if (dataSource != null) {
                try (java.sql.Connection conn = dataSource.getConnection()) {
                    status.put("connection_test", "success");
                    status.put("database_product", conn.getMetaData().getDatabaseProductName());
                    status.put("database_version", conn.getMetaData().getDatabaseProductVersion());
                } catch (Exception e) {
                    status.put("connection_test", "failed");
                    status.put("connection_error", e.getMessage());
                    logger.warn("Database connection test failed", e);
                }
            } else {
                status.put("dataSource", "Not available - application may be running in database-resilient mode");
            }
            
        } catch (Exception e) {
            status.put("error", "Error processing database information: " + e.getMessage());
            logger.error("Error processing database information", e);
        }
        
        // Add additional diagnostic information
        addNetworkDiagnostics(status);
        
        return ResponseEntity.ok(status);
    }
    
    /**
     * Extracts host from database URL
     */
    private String extractHostFromUrl(String url) {
        try {
            if (url.contains("@")) {
                String hostPart = url.split("@")[1].split("/")[0];
                return hostPart.contains(":") ? hostPart.split(":")[0] : hostPart;
            } else if (url.contains("//")) {
                String hostPart = url.split("//")[1].split("/")[0];
                return hostPart.contains(":") ? hostPart.split(":")[0] : hostPart;
            }
        } catch (Exception e) {
            logger.warn("Failed to extract host from URL", e);
        }
        return null;
    }
    
    /**
     * Adds network diagnostic information to the status map
     */
    private void addNetworkDiagnostics(Map<String, Object> status) {
        Map<String, Object> networkInfo = new HashMap<>();
        
        try {
            // Get local host info
            InetAddress localhost = InetAddress.getLocalHost();
            networkInfo.put("hostname", localhost.getHostName());
            networkInfo.put("hostAddress", localhost.getHostAddress());
            
            // Test common DNS servers
            Map<String, Object> dnsTests = new HashMap<>();
            testHostResolution(dnsTests, "google-dns", "8.8.8.8");
            testHostResolution(dnsTests, "cloudflare-dns", "1.1.1.1");
            testHostResolution(dnsTests, "render-api", "api.render.com");
            
            // Test Render internal hostnames
            testHostResolution(dnsTests, "render-internal", "internal");
            testHostResolution(dnsTests, "db-hostname", "dpg-d1v6rqemcj7s73f4fr0g-a");
            
            networkInfo.put("dnsTests", dnsTests);
            
            // Get network interfaces
            networkInfo.put("interfaces", getNetworkInterfaces());
            
            status.put("networkDiagnostics", networkInfo);
        } catch (Exception e) {
            status.put("networkDiagnosticsError", e.getMessage());
        }
    }
    
    /**
     * Tests hostname resolution and adds result to the provided map
     */
    private void testHostResolution(Map<String, Object> results, String key, String hostname) {
        Map<String, Object> result = new HashMap<>();
        try {
            InetAddress address = InetAddress.getByName(hostname);
            result.put("resolved", true);
            result.put("ip", address.getHostAddress());
        } catch (Exception e) {
            result.put("resolved", false);
            result.put("error", e.getMessage());
        }
        results.put(key, result);
    }
    
    /**
     * Gets information about network interfaces
     */
    private Map<String, Object> getNetworkInterfaces() {
        Map<String, Object> interfaces = new HashMap<>();
        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface netint : Collections.list(nets)) {
                if (netint.isUp()) {
                    Map<String, Object> interfaceInfo = new HashMap<>();
                    interfaceInfo.put("displayName", netint.getDisplayName());
                    interfaceInfo.put("name", netint.getName());
                    
                    // Get IP addresses
                    Map<String, String> addresses = new HashMap<>();
                    Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
                    int addrCount = 0;
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress addr = inetAddresses.nextElement();
                        addresses.put("addr" + (++addrCount), addr.getHostAddress());
                    }
                    interfaceInfo.put("addresses", addresses);
                    
                    interfaces.put(netint.getName(), interfaceInfo);
                }
            }
        } catch (Exception e) {
            interfaces.put("error", e.getMessage());
        }
        return interfaces;
    }
    
    /**
     * Get system information for diagnostics
     */
    @GetMapping("/system")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();
        
        // Add Java information
        Map<String, String> java = new HashMap<>();
        java.put("version", System.getProperty("java.version"));
        java.put("vendor", System.getProperty("java.vendor"));
        java.put("vm", System.getProperty("java.vm.name"));
        
        // Add OS information
        Map<String, String> os = new HashMap<>();
        os.put("name", System.getProperty("os.name"));
        os.put("version", System.getProperty("os.version"));
        os.put("arch", System.getProperty("os.arch"));
        
        // Add memory information
        Map<String, Object> memory = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();
        memory.put("total", runtime.totalMemory());
        memory.put("free", runtime.freeMemory());
        memory.put("max", runtime.maxMemory());
        
        // Add application information
        Map<String, Object> application = new HashMap<>();
        application.put("profiles", env.getActiveProfiles());
        application.put("startTime", System.getProperty("app.startTime", "unknown"));
        
        info.put("java", java);
        info.put("os", os);
        info.put("memory", memory);
        info.put("application", application);
        
        return ResponseEntity.ok(info);
    }
}
