import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

class Statistics {
    private long totalTraffic;
    private LocalDateTime minTime;
    private LocalDateTime maxTime;

    private final HashSet<String> existingPages = new HashSet<>();
    private final HashSet<String> nonExistingPages = new HashSet<>();
    private final HashMap<String, Integer> osFrequency = new HashMap<>();
    private final HashMap<String, Integer> browserFrequency = new HashMap<>();

    private int botVisits;
    private int errorRequests;
    private final HashSet<String> uniqueUserIPs = new HashSet<>();

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

        String ip = entry.getIpAddr();

        // Проверяем, является ли обращение от бота
        boolean isBot = entry.getUserAgent().getUserAgentString().toLowerCase().contains("bot");

        if (!isBot) {
            uniqueUserIPs.add(ip); // Добавляем уникальный IP
        } else {
            botVisits++; // Увеличиваем количество обращений от ботов
        }

        if (entry.getResponseCode() == 200) {
            existingPages.add(entry.getPath());
        } else if (entry.getResponseCode() / 100 == 4 || entry.getResponseCode() / 100 == 5) {
            nonExistingPages.add(entry.getPath());
            errorRequests++; // Увеличиваем количество ошибочных запросов
        }

        String os = entry.getUserAgent().getOs();
        osFrequency.put(os, osFrequency.getOrDefault(os, 0) + 1);

        String browser = entry.getUserAgent().getBrowser();
        browserFrequency.put(browser, browserFrequency.getOrDefault(browser, 0) + 1);
    }

    public double getTrafficRate() {
        if (minTime == null || maxTime == null || minTime.equals(maxTime)) {
            return 0.0;
        }
        double hours = Duration.between(minTime, maxTime).toMillis() / (1000.0 * 3600);
        return hours > 0 ? totalTraffic / hours : 0.0;
    }

    public double getAverageVisitsPerHour() {
        if (minTime == null || maxTime == null || minTime.equals(maxTime)) {
            return 0.0;
        }
        double hours = Duration.between(minTime, maxTime).toMillis() / (1000.0 * 3600);
        return hours > 0 ? (totalTraffic - botVisits) / hours : 0.0; // Не учитываем ботов
    }

    public double getAverageErrorRequestsPerHour() {
        if (minTime == null || maxTime == null || minTime.equals(maxTime)) {
            return 0.0;
        }
        double hours = Duration.between(minTime, maxTime).toMillis() / (1000.0 * 3600);
        return hours > 0 ? errorRequests / hours : 0.0; // Среднее за период
    }

    public double getAverageVisitsPerUser() {
        if (uniqueUserIPs.isEmpty()) {
            return 0.0;
        }
        return (totalTraffic - botVisits) / (double) uniqueUserIPs.size(); // Не учитываем ботов
    }

    public HashSet<String> getExistingPages() {
        return existingPages;
    }

    public HashSet<String> getNonExistingPages() {
        return nonExistingPages;
    }

    public HashMap<String, Double> getOSStatistics() {
        HashMap<String, Double> result = new HashMap<>();
        int totalOSCount = osFrequency.values().stream().mapToInt(Integer::intValue).sum();
        if (totalOSCount > 0) {
            for (Map.Entry<String, Integer> entry : osFrequency.entrySet()) {
                result.put(entry.getKey(), (double) entry.getValue() / totalOSCount);
            }
        }
        return result;
    }

    public HashMap<String, Double> getBrowserStatistics() {
        HashMap<String, Double> result = new HashMap<>();
        int totalBrowserCount = browserFrequency.values().stream().mapToInt(Integer::intValue).sum();
        if (totalBrowserCount > 0) {
            for (Map.Entry<String, Integer> entry : browserFrequency.entrySet()) {
                result.put(entry.getKey(), (double) entry.getValue() / totalBrowserCount);
            }
        }
        return result;
    }
}