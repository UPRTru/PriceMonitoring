package general.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Controller
public final class DashboardController {

    private static final Duration CONNECTION_TIMEOUT = Duration.ofSeconds(1);
    private static final String HEALTH_PATH = "/health";

    private final String bankAgentUrl;
    private final String userServiceUrl;
    private final String bankAgentOutUrl;
    private final String userServiceOutUrl;

    public DashboardController(@Value("${bank.agent.url}") String bankAgentUrl,
                               @Value("${user.service.url}") String userServiceUrl,
                               @Value("${bank.agent.out-url}") String bankAgentOutUrl,
                               @Value("${user.service.out-url}") String userServiceOutUrl) {
        this.bankAgentUrl = validateUrl(bankAgentUrl, "bank.agent.url");
        this.userServiceUrl = validateUrl(userServiceUrl, "user.service.url");
        this.bankAgentOutUrl = validateUrl(bankAgentOutUrl, "bank.agent.out-url");
        this.userServiceOutUrl = validateUrl(userServiceOutUrl, "user.service.out-url");
    }

    private String validateUrl(String url, String paramName) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException(paramName + " cannot be empty");
        }
        return url.trim();
    }

    @GetMapping("/")
    public String dashboard() {
        Map<String, Boolean> status = moduleStatus();
        boolean bankAgentAvailable = Boolean.TRUE.equals(status.get("bankAgent"));
        boolean userServiceAvailable = Boolean.TRUE.equals(status.get("userService"));

        if (bankAgentAvailable && userServiceAvailable) {
            return "redirect:" + userServiceOutUrl + "?all=true";
        } else if (bankAgentAvailable) {
            return "redirect:" + bankAgentOutUrl;
        } else if (userServiceAvailable) {
            return "redirect:" + userServiceOutUrl;
        }
        return "unavailable";
    }

    @GetMapping("/api/bank_module")
    public String getBankModule() {
        if (isReachable(bankAgentUrl + HEALTH_PATH)) {
            return "redirect:" + bankAgentOutUrl;
        }
        return "unavailable";
    }

    @GetMapping("/api/modules/status")
    @ResponseBody
    public Map<String, Boolean> moduleStatus() {
        Map<String, Boolean> status = new HashMap<>(2);
        status.put("bankAgent", isReachable(bankAgentUrl + HEALTH_PATH));
        status.put("userService", isReachable(userServiceUrl + HEALTH_PATH));
        return Collections.unmodifiableMap(status);
    }

    private boolean isReachable(String url) {
        Objects.requireNonNull(url, "URL cannot be null");

        HttpURLConnection connection = null;
        try {
            URL u = new URL(url);
            connection = (HttpURLConnection) u.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout((int) CONNECTION_TIMEOUT.toMillis());
            connection.setReadTimeout((int) CONNECTION_TIMEOUT.toMillis());
            connection.setUseCaches(false);
            return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}