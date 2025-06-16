import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class FileLineAnalyzer {
    public static void main(java.lang.String[] args) {
        java.lang.String path = "C:\\Users\\ndoroshenko\\Desktop\\TestProject\\access.log";

        try {
            File file = new File(path);
            if (!file.exists()) {
                throw new IOException("Файл не существует: " + path);
            }
            if (!file.isFile()) {
                throw new IOException("Указанный путь не является файлом: " + path);
            }

            BufferedReader reader = new BufferedReader(new FileReader(file));
            java.lang.String line;
            int totalLines = 0;
            int googleBotCount = 0;
            int yandexBotCount = 0;
            Statistics statistics = new Statistics();

            while ((line = reader.readLine()) != null) {
                totalLines++;
                try {
                    LogEntry entry = new LogEntry(line);
                    statistics.addEntry(entry);

                    java.lang.String uaString = entry.getUserAgent().getUserAgentString();
                    java.lang.String program = extractProgram(uaString);
                    if ("Googlebot".equals(program)) {
                        googleBotCount++;
                    } else if ("YandexBot".equals(program)) {
                        yandexBotCount++;
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка при обработке строки: " + line);
                    e.printStackTrace();
                }
            }

            reader.close();

            System.out.println("Общее количество строк в файле: " + totalLines);
            System.out.println("Количество запросов от Googlebot: " + googleBotCount);
            System.out.println("Количество запросов от YandexBot: " + yandexBotCount);

            if (totalLines > 0) {
                System.out.printf("Доля запросов от Googlebot: %.2f%%%n",
                        (double) googleBotCount / totalLines * 100);
                System.out.printf("Доля запросов от YandexBot: %.2f%%%n",
                        (double) yandexBotCount / totalLines * 100);
            } else {
                System.out.println("Файл пуст, невозможно вычислить доли");
            }

            System.out.printf("Средний объем трафика за час: %.2f байт%n", statistics.getTrafficRate());

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static java.lang.String extractProgram(java.lang.String userAgent) {
        int openBracket = userAgent.indexOf('(');
        int closeBracket = userAgent.indexOf(')');
        if (openBracket == -1 || closeBracket == -1 || openBracket >= closeBracket) {
            return null;
        }
        java.lang.String bracketContent = userAgent.substring(openBracket + 1, closeBracket);
        java.lang.String[] parts = bracketContent.split(";");
        if (parts.length < 2) return null;
        java.lang.String secondPart = parts[1].trim();
        return secondPart.split("/")[0].trim();
    }
}

enum HttpMethod {
    GET, POST, PUT, DELETE, HEAD, OPTIONS, PATCH, TRACE
}

class String {
    private final java.lang.String os;
    private final java.lang.String browser;
    private final java.lang.String userAgentString;

    public String(java.lang.String userAgentString) {
        this.userAgentString = userAgentString;
        this.os = parseOS(userAgentString);
        this.browser = parseBrowser(userAgentString);
    }

    private java.lang.String parseOS(java.lang.String ua) {
        if (ua.contains("Windows")) return "Windows";
        else if (ua.contains("Mac OS")) return "macOS";
        else if (ua.contains("Linux")) return "Linux";
        return "Other";
    }

    private java.lang.String parseBrowser(java.lang.String ua) {
        if (ua.contains("Edge")) return "Edge";
        else if (ua.contains("Firefox")) return "Firefox";
        else if (ua.contains("Chrome")) return "Chrome";
        else if (ua.contains("Opera")) return "Opera";
        return "Other";
    }

    public java.lang.String getOs() {
        return os;
    }

    public java.lang.String getBrowser() {
        return browser;
    }

    public java.lang.String getUserAgentString() {
        return userAgentString;
    }
}

class LogEntry {
    private final java.lang.String ipAddr;
    private final LocalDateTime time;
    private final HttpMethod method;
    private final java.lang.String path;
    private final int responseCode;
    private final int responseSize;
    private final java.lang.String referer;
    private final String userAgent;

    public LogEntry(java.lang.String line) {
        int idx = line.indexOf(' ');
        this.ipAddr = line.substring(0, idx);

        for (int i = 0; i < 2; i++) {
            idx = line.indexOf(' ', idx + 1);
        }

        int bracketStart = line.indexOf('[', idx);
        int bracketEnd = line.indexOf(']', bracketStart);
        java.lang.String dateStr = line.substring(bracketStart + 1, bracketEnd);
        this.time = parseDateTime(dateStr);

        int quoteStart = line.indexOf('"', bracketEnd);
        int quoteEnd = line.indexOf('"', quoteStart + 1);
        java.lang.String request = line.substring(quoteStart + 1, quoteEnd);
        java.lang.String[] requestParts = request.split("\\s+", 3);
        this.method = parseHttpMethod(requestParts[0]);
        this.path = requestParts.length > 1 ? requestParts[1] : "";

        idx = quoteEnd + 1;
        int codeStart = idx = nextNonSpace(line, idx);
        int codeEnd = line.indexOf(' ', codeStart);
        this.responseCode = Integer.parseInt(line.substring(codeStart, codeEnd));

        int sizeStart = nextNonSpace(line, codeEnd);
        int sizeEnd = line.indexOf(' ', sizeStart);
        if (sizeEnd == -1) sizeEnd = line.length();
        java.lang.String sizeStr = line.substring(sizeStart, sizeEnd);
        this.responseSize = sizeStr.equals("-") ? 0 : Integer.parseInt(sizeStr);

        int refStart = line.indexOf('"', sizeEnd);
        int refEnd = line.indexOf('"', refStart + 1);
        this.referer = line.substring(refStart + 1, refEnd);

        int uaStart = line.indexOf('"', refEnd + 1);
        int uaEnd = line.indexOf('"', uaStart + 1);
        java.lang.String uaString = line.substring(uaStart + 1, uaEnd);
        this.userAgent = new String(uaString);
    }

    private int nextNonSpace(java.lang.String s, int start) {
        while (start < s.length() && Character.isWhitespace(s.charAt(start))) {
            start++;
        }
        return start;
    }

    private LocalDateTime parseDateTime(java.lang.String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z", Locale.US);
        ZonedDateTime zdt = ZonedDateTime.parse(dateStr, formatter);
        return zdt.toLocalDateTime();
    }

    private HttpMethod parseHttpMethod(java.lang.String methodStr) {
        try {
            return HttpMethod.valueOf(methodStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public java.lang.String getIpAddr() { return ipAddr; }
    public LocalDateTime getTime() { return time; }
    public HttpMethod getMethod() { return method; }
    public java.lang.String getPath() { return path; }
    public int getResponseCode() { return responseCode; }
    public int getResponseSize() { return responseSize; }
    public java.lang.String getReferer() { return referer; }
    public String getUserAgent() { return userAgent; }
}

class Statistics {
    private long totalTraffic;
    private LocalDateTime minTime;
    private LocalDateTime maxTime;

    public Statistics() {
        totalTraffic = 0;
        minTime = null;
        maxTime = null;
    }

    public void addEntry(LogEntry entry) {
        totalTraffic += entry.getResponseSize();
        LocalDateTime time = entry.getTime();
        if (minTime == null || time.isBefore(minTime)) {
            minTime = time;
        }
        if (maxTime == null || time.isAfter(maxTime)) {
            maxTime = time;
        }
    }

    public double getTrafficRate() {
        if (minTime == null || maxTime == null || minTime.equals(maxTime)) {
            return 0.0;
        }
        double hours = Duration.between(minTime, maxTime).toMillis() / (1000.0 * 3600);
        return totalTraffic / hours;
    }
}