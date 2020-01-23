import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public class LinesGenerator {
    private int linesCount;
    private int length;
    private static final char[] DEFAULT_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    public LinesGenerator(int linesCount, int length) {
        this.linesCount = linesCount;
        this.length = length;
    }

    public void fill(Path path) {
        System.out.println("Generating " + linesCount + " lines of strings " + length + " chars");
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            Random random = new Random();
            char[] generatedLine = new char[length];
            for (int i = 0; i < linesCount; i++) {
                for (int j = 0; j < length; j++) {
                    generatedLine[j] = DEFAULT_ALPHABET[random.nextInt(DEFAULT_ALPHABET.length)];
                }
                writer.write(generatedLine);
                writer.newLine();
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new LinesGenerator(100_000_000, 20).fill(Path.of("C:\\Users\\admin\\test\\main.log"));
    }
}
