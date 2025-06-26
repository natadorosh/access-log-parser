import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class FileLineAnalyzer {
    public static void main(String[] args) {
        String path = "C:\\Users\\ndoroshenko\\Desktop\\TestProject\\access.log";
        int totalLines = 0;
        int googleBotCount = 0;
        int yandexBotCount = 0;
        Statistics statistics = new Statistics();

        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                totalLines++;
                if (line.length() > 1024) {
                    throw new RuntimeException("Строка длиннее 1024 символов: " + line.length());
                }
                try {
                    LogEntry entry = new LogEntry(line);
                    statistics.addEntry(entry);

                    String ua = entry.getUserAgent() != null
                            ? entry.getUserAgent().getUserAgentString() : "";
                    String uaLower = ua.toLowerCase();

                    if (!uaLower.isEmpty()) {
                        if (uaLower.contains("googlebot")) {
                            googleBotCount++;
                        }
                        if (uaLower.contains("yandexbot")) {
                            yandexBotCount++;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка при обработке строки: " + line);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        System.out.println("Общее количество строк в файле: " + totalLines);

        System.out.println("\nСтатистика использования поисковых ботов:");
        System.out.println("  Запросов от Googlebot: " + googleBotCount);
        System.out.println("  Запросов от YandexBot: " + yandexBotCount);
        if (totalLines > 0) {
            System.out.printf("  Доля Googlebot: %.2f%%%n", (double) googleBotCount / totalLines * 100);
            System.out.printf("  Доля YandexBot: %.2f%%%n", (double) yandexBotCount / totalLines * 100);
        } else {
            System.out.println("  Файл пуст, невозможно вычислить доли");
        }

        System.out.printf("Средний объем трафика за час: %.2f байт%n", statistics.getTrafficRate());

        System.out.println("\nСуществующие страницы, с кодом ответа 200:");
        int counter = 0;
        for (String page : statistics.getExistingPages()) {
            if (counter++ >= 10) break; //уменьшила количество выводимых страниц до 10, для удобства
            System.out.println(page);
        }
        System.out.println("Всего существующих страниц: " + statistics.getExistingPages().size());

        System.out.println("\nНесуществующие страницы, с кодом ответа 404:");
        counter = 0;
        for (String page : statistics.getNonExistingPages()) {
            if (counter++ >= 10) break; //уменьшила количество выводимых страниц до 10, для удобства
            System.out.println(page);
        }
        System.out.println("Всего несуществующих страниц: " + statistics.getNonExistingPages().size());

        System.out.println("\nСтатистика использования операционных систем:");
        HashMap<String, Double> osStats = statistics.getOSStatistics();
        for (Map.Entry<String, Double> entry : osStats.entrySet()) {
            double percentage = entry.getValue() * 100;
            System.out.printf("%s: %.2f%%%n", entry.getKey(), percentage);
        }

        System.out.println("\nСтатистика использования браузеров:");
        HashMap<String, Double> browserStats = statistics.getBrowserStatistics();
        for (Map.Entry<String, Double> entry : browserStats.entrySet()) {
            double percentage = entry.getValue() * 100;
            System.out.printf("%s: %.2f%%%n", entry.getKey(), percentage);
        }
    }
}