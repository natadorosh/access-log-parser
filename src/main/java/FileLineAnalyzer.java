import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FileLineAnalyzer {
    public static void main(String[] args) {
        String path = "C:\\Users\\ndoroshenko\\Desktop\\TestProject\\access.log";

        try {
            File file = new File(path);
            if (!file.exists()) {
                throw new IOException("Файл не существует: " + path);
            }
            if (!file.isFile()) {
                throw new IOException("Указанный путь не является файлом: " + path);
            }

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            int totalLines = 0;
            int googleBotCount = 0;
            int yandexBotCount = 0;

            while ((line = reader.readLine()) != null) {
                totalLines++;

                int userAgentStartIndex = line.lastIndexOf('"', line.lastIndexOf('"') - 1) + 1;
                int userAgentEndIndex = line.lastIndexOf('"');
                if (userAgentStartIndex == -1 || userAgentEndIndex == -1) continue;

                String userAgent = line.substring(userAgentStartIndex, userAgentEndIndex).trim();

                int openBracket = userAgent.indexOf('(');
                int closeBracket = userAgent.indexOf(')');

                if (openBracket == -1 || closeBracket == -1 || openBracket >= closeBracket) {
                    continue;
                }

                String bracketContent = userAgent.substring(openBracket + 1, closeBracket);

                String[] parts = bracketContent.split(";");
                if (parts.length < 2) continue;

                String secondPart = parts[1].trim();

                String program = secondPart.split("/")[0].trim();

                if ("Googlebot".equals(program)) {
                    googleBotCount++;
                } else if ("YandexBot".equals(program)) {
                    yandexBotCount++;
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

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}