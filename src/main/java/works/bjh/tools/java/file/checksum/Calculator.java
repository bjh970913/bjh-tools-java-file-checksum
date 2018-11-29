package works.bjh.tools.java.file.checksum;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;

public interface Calculator {
    Future<Result> calculate(String absolutePath);

    Future<Result> calculate(File file);

    Future<List<Result>> calculateAll(String rootPath) throws IOException;

    Future<List<Result>> calculateAll(List<File> absolutePaths);
}
