package works.bjh.tools.java.file.checksum;

import com.sun.tools.javac.util.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Calculator {
    private static ThreadPoolExecutor executor;

    public static void main(String[] args) throws IOException {
        String rootPath = "/tmp/mnist_png";

        ThreadFactory threadFactory = new ThreadFactory() {
            private AtomicInteger threadCount = new AtomicInteger(1);

            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("bjh-tools-file-checksum-calculator-" + threadCount.addAndGet(1));
                return thread;
            }
        };
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10, threadFactory);
        List<Future<Pair<String, String>>> digests;
        try {
            digests = Files.walk(Paths.get(rootPath))
                    .filter(Files::isReadable)
                    .map(Path::toFile)
                    .filter(f -> !f.isDirectory())
                    .map(i -> {
                        InputStream stream;
                        try {
                            stream = new FileInputStream(i);
                        } catch (FileNotFoundException e) {
                            stream = null;
                        }
                        return new Pair<>(i.getAbsolutePath(), stream);
                    })
                    .map(Calculator::digestInputStream).collect(Collectors.toList());
//                    .forEach(System.out::println);
        } catch (Exception ignored) {
            digests = new ArrayList<>();
        }
        List<Pair<String, String>> result = new ArrayList<>();
        for (Future<Pair<String, String>> digest : digests) {
            try {
                result.add(digest.get());
            } catch (Exception ignored) {
            }
        }
        System.out.println(result.size());

        File file = new File("/tmp/calc.json");
        FileWriter write = new FileWriter(file);

        JSONArray arr = new JSONArray();
        for (Pair<String, String> line : result){
            JSONObject obj = new JSONObject();
            obj.put("file", line.fst);
            obj.put("hash", line.snd);
            arr.add(obj);
        }

        write.append(arr.toJSONString());
        write.close();

        executor.shutdown();
    }

    private static Future<Pair<String, String>> digestInputStream(Pair<String, InputStream> target) {
        return executor.submit(() -> {
            try {
                byte[] buffer = new byte[1024];
                InputStream stream = target.snd;
                if (stream == null) {
                    return new Pair<>(target.fst, null);
                }
                MessageDigest complete;
                complete = MessageDigest.getInstance("MD5");

                int numRead;
                do {
                    numRead = stream.read(buffer);
                    if (numRead > 0) {
                        complete.update(buffer, 0, numRead);
                    }
                } while (numRead != -1);

                stream.close();
                StringBuilder sb = new StringBuilder();
                for (byte b : complete.digest())
                    sb.append(String.format("%02x", b & 0xFF));
                return new Pair<>(target.fst, sb.toString());
            } catch (Exception e) {
                return new Pair<>(target.fst, null);
            }
        });
    }
}
