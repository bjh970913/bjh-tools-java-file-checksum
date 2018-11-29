package works.bjh.tools.java.file.checksum;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class CalculatorImpl implements Calculator {
    private ThreadPoolExecutor executor;

    public CalculatorImpl() {
        ThreadFactory threadFactory = new ThreadFactory() {
            private AtomicInteger atomicInteger = new AtomicInteger(1);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("bjh-tools-file-checksum-calculator-" + atomicInteger.addAndGet(1));
                return thread;
            }
        };
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10, threadFactory);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        executor.shutdown();
    }

    @Override
    public Future<Result> calculate(String absolutePath) {
        return calculate(new File(absolutePath));
    }

    @Override
    public Future<Result> calculate(File file) {
        return calculateFileHash(file);
    }

    @Override
    public Future<List<Result>> calculateAll(String rootPath) throws IOException {
        List<File> files = Files.walk(Paths.get(rootPath))
                .filter(Files::isReadable)
                .map(Path::toFile)
                .filter(f -> !f.isDirectory())
                .collect(Collectors.toList());
        return calculateAll(files);
    }

    @Override
    public Future<List<Result>> calculateAll(List<File> files) {
        return executor.submit(() -> {
            List<Future<Result>> futures = files.stream()
                    .map(this::calculate)
                    .collect(Collectors.toList());
            List<Result> resultList = new ArrayList<>();

            for (Future<Result> future : futures) {
                resultList.add(future.get());
            }
            return resultList;
        });
    }

    private Future<Result> calculateFileHash(File file) {
        return executor.submit(() -> {
            try {
                byte[] buffer = new byte[1024];
                InputStream inputStream = new FileInputStream(file);
                MessageDigest digest = MessageDigest.getInstance("MD5");

                int read;
                do {
                    read = inputStream.read(buffer);
                    if (read > 0) {
                        digest.update(buffer, 0, read);
                    }
                } while (read != -1);
                inputStream.close();

                StringBuilder sb = new StringBuilder();
                for (byte b : digest.digest()) {
                    sb.append(String.format("%02x", b));
                }

                return new Result(file.getAbsolutePath(), sb.toString());
            } catch (Exception e) {
                return new Result(file.getAbsolutePath(), "");
            }
        });
    }
}
