import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

class LogEntry {
    private final String ipAddr;
    private final LocalDateTime time;
    private final HttpMethod method;
    private final String path;
    private final int responseCode;
    private final int responseSize;
    private final String referer;
    private final UserAgent userAgent;

    public LogEntry(String line) {
        try {
            String[] parts = splitLine(line);
            this.ipAddr = parts[0];
            this.time = parseDateTime(parts[1]);
            this.method = parseHttpMethod(parts[2]);
            this.path = parts[3];
            this.responseCode = Integer.parseInt(parts[4]);
            this.responseSize = parts[5].equals("-") ? 0 : Integer.parseInt(parts[5]);
            this.referer = parts[6];
            this.userAgent = new UserAgent(parts[7]);
        } catch (Exception e) {
            System.err.println("Ошибка при парсинге строки лога: " + line);
            throw new RuntimeException("Ошибка при обработке строки лога: " + line, e);
        }
    }

    // Парсер строки лога для основных nginx/apache combined лога
    private String[] splitLine(String line) throws Exception {
        // Формат: ip - - [date] "METHOD /path proto" code size "referer" "UA"
        int firstSpace = line.indexOf(' ');
        String ip = line.substring(0, firstSpace);

        int lBr = line.indexOf('[', firstSpace);
        int rBr = line.indexOf(']', lBr);
        String date = line.substring(lBr + 1, rBr);

        int reqStart = line.indexOf('"', rBr) + 1;
        int reqEnd = line.indexOf('"', reqStart);
        String req = line.substring(reqStart, reqEnd);
        String[] reqParts = req.split("\\s+");
        String method = reqParts.length > 0 ? reqParts[0] : "";
        String path = reqParts.length > 1 ? reqParts[1] : "";

        int afterReq = reqEnd + 1;
        String[] rest = line.substring(afterReq).trim().split(" ");
        String code = rest[0];
        String size = rest[1];

        int refStart = line.indexOf('"', reqEnd + 1);
        int refEnd = line.indexOf('"', refStart + 1);
        String referer = "";
        if (refStart != -1 && refEnd != -1) {
            referer = line.substring(refStart + 1, refEnd);
        }

        int uaStart = line.indexOf('"', refEnd + 1);
        int uaEnd = line.indexOf('"', uaStart + 1);
        String uaString = "";
        if (uaStart != -1 && uaEnd != -1) {
            uaString = line.substring(uaStart + 1, uaEnd);
        }

        return new String[]{ip, date, method, path, code, size, referer, uaString};
    }

    private LocalDateTime parseDateTime(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z", Locale.US);
        ZonedDateTime zdt = ZonedDateTime.parse(dateStr, DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z", Locale.US));
        return zdt.toLocalDateTime();
    }

    private HttpMethod parseHttpMethod(String methodStr) {
        try {
            return HttpMethod.valueOf(methodStr);
        } catch (IllegalArgumentException e) {
            System.err.println("Неизвестный HTTP метод: " + methodStr);
            return null;
        }
    }

    public LocalDateTime getTime() { return time; }
    public String getPath() { return path; }
    public int getResponseCode() { return responseCode; }
    public int getResponseSize() { return responseSize; }
    public UserAgent getUserAgent() { return userAgent; }
    public String getIpAddr() { return ipAddr; }
}