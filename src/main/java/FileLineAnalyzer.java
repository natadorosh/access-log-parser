import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

            System.out.println("Существующие страницы:");
            for (java.lang.String page : statistics.getExistingPages()) {
                System.out.println(page);
            }
            System.out.println("\nСтатистика использования операционных систем:");
            HashMap<java.lang.String, Double> osStats = statistics.getOSStatistics();
            for (Map.Entry<java.lang.String, Double> entry : osStats.entrySet()) {
                double percentage = entry.getValue() * 100;
                System.out.printf("%s: %.2f%%%n", entry.getKey(), percentage);
            }

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