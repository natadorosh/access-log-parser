class UserAgent {
    private final String os;
    private final String browser;
    private final String userAgentString;

    public UserAgent(String userAgentString) {
        this.userAgentString = userAgentString;
        this.os = parseOS(userAgentString);
        this.browser = parseBrowser(userAgentString);
    }

    private String parseOS(String ua) {
        ua = ua.toLowerCase();
        if (ua.contains("windows")) {
            return "Windows";
        } else if (ua.contains("mac os") || ua.contains("darwin")) {
            return "macOS";
        } else if (ua.contains("linux")) {
            return "Linux";
        } else if (ua.contains("android")) {
            return "Android";
        } else if (ua.contains("iphone") || ua.contains("ipad")) {
            return "iOS";
        }
        return "Other";
    }

    private String parseBrowser(String ua) {
        ua = ua.toLowerCase();
        if (ua.contains("edge") || ua.contains("edg/")) { // Edge новые user-agent
            return "Edge";
        } else if (ua.contains("firefox")) {
            return "Firefox";
        } else if (ua.contains("opera") || ua.contains("opr/")) {
            return "Opera";
        } else if (ua.contains("chrome")) {
            // Не путать с Edge, который иногда содержит Chrome и Edg
            if (ua.contains("edg/")) {
                return "Edge";
            }
            return "Chrome";
        } else if (ua.contains("safari")) {
            return "Safari";
        }
        return "Other";
    }

    public String getOs() {
        return os;
    }

    public String getBrowser() {
        return browser;
    }

    public String getUserAgentString() {
        return userAgentString;
    }
}