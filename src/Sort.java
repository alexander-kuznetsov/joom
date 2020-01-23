import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Sort {
    private Path path;
    private int heapStringCapacity;
    private final double DEFAULT_LOAD_FACTOR = 0.6d;

    public static void main(String[] args) {
        new Sort(Path.of("C:\\Users\\admin\\test\\main.log")).sort();
    }

    public Sort(Path path) {
        this.path = path;
    }

    public void sort() {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            List<Path> chunks = new ArrayList<>();
            long time= System.currentTimeMillis();
            String s = reader.readLine();
            if (s == null) {
                System.out.println("empty file");
                return;
            }
            init(s.length());
            String[] strings = new String[heapStringCapacity];
            boolean finished = false;
            while (!finished) {
                int linesCount = 0;
                for (; linesCount < heapStringCapacity;) {
                    strings[linesCount] = s;
                    s = reader.readLine();
                    linesCount++;
                    if (s == null) {
                        finished = true;

                        break;
                    }
                }
                Arrays.sort(strings, 0, linesCount);
                chunks.add(write(strings, linesCount));
            }
            System.out.println("Split and sort took " + (System.currentTimeMillis()-time)/1000+ " seconds");
            System.out.println(chunks.size() + " file created total");
            System.out.println("Starting merging process");
             time = System.currentTimeMillis();
            Optional<Path> reduce = chunks.stream().reduce(this::merge);
            System.out.println("Merging took: " + (System.currentTimeMillis()-time)/1000 + " seconds");
            System.out.println("Final result: " + reduce.get());

        } catch (IOException e) {
            System.err.println("sorting failed");
            e.printStackTrace();
        }
    }

    private Path merge(Path first, Path second) {
        Path out;
        try (BufferedReader readerFirst = Files.newBufferedReader(first);
             BufferedReader readerSecond = Files.newBufferedReader(second);
             BufferedWriter writer = Files.newBufferedWriter(out = createFile(first.getParent().toString()))) {

            String s1 = readerFirst.readLine();
            String s2 = readerSecond.readLine();
            while (s1 != null || s2 != null) {
                if (s1 != null && (s2 == null || s1.compareTo(s2) >= 0)) {
                    writer.write(s1);
                    s1 = readerFirst.readLine();
                } else {
                    writer.write(s2);
                    s2 = readerSecond.readLine();
                }
                writer.newLine();
            }
            Files.delete(first);
            Files.delete(second);
        } catch (IOException e) {
            e.printStackTrace();
            return Path.of(first.getParent().toString(), "empty.log");
        }
        return out;
    }

    private static Path createFile(String dir) throws IOException {
        return Files.createFile(Path.of(dir, System.nanoTime() + "_merged.log"));
    }

    private Path write(String[] strings, int linesCount) {
        Path dir = path.getParent();
        Path file = null;
        try (BufferedWriter writer = Files.newBufferedWriter(file = Path.of(dir.toString(), System.nanoTime() + ".log"))) {
            for (int i = 0; i < linesCount; i++) {
                writer.write(strings[i]);
                writer.newLine();
            }
            writer.flush();
//            System.out.println(file.toString() + " created. with " + linesCount + " lines inside");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    private void init(int lineLength) {
        long freeMemory = Runtime.getRuntime().freeMemory();
        int stringObjectSize = calcSize(lineLength);
        heapStringCapacity = (int) (DEFAULT_LOAD_FACTOR * freeMemory / stringObjectSize);

        System.out.println("Free memory in bytes " + freeMemory);
        System.out.println("Line length in file: " + lineLength);
        System.out.println("string object size would be " + stringObjectSize + " bytes");
        System.out.println("Using heap load factor: " + DEFAULT_LOAD_FACTOR);
    }

    // return size in bytes for String object of length @lineLength
    private int calcSize(int lineLength) {
        int charArraySize = padding(8 + //object head
                4 +   //4bytes for int (array size)
                2 * lineLength // chars in array
        );
        return padding(8 + // object head
                4 + // hash
                8 + // object ref to char[]
                charArraySize);
    }

    private int padding(int objectSize) {
        return objectSize % 8 == 0 ? objectSize : 8 * (objectSize / 8) + 8;
    }
}
