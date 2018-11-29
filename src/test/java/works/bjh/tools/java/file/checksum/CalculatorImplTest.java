package works.bjh.tools.java.file.checksum;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class CalculatorImplTest {
    private static final String tempDir = System.getProperty("java.io.tmpdir");

    private static final String test1 = "test1.txt";
    @SuppressWarnings("SpellCheckingInspection")
    private static final String test1_data = new String("abcdefg가나다라마바사012345".getBytes(), StandardCharsets.UTF_8);
    private static final String test1_hash = "76a7071e769054d3a3dac2d375c28f3f";

    private static final String test2 = "test2.txt";
    private static final String test2_data = new String("АБВГДЕЁЖЗИЙКЛМНОПРСТУФХ".getBytes(), StandardCharsets.UTF_8);
    private static final String test2_hash = "d73111ebf04bc8d1141cd4e1563b343f";

    private static String test1_absolute_path;
    private static String test2_absolute_path;

    @BeforeClass
    static public void Prepare() throws IOException {
        test1_absolute_path = Paths.get(tempDir, test1).toAbsolutePath().toString();
        test2_absolute_path = Paths.get(tempDir, test2).toAbsolutePath().toString();
        deleteFile(test1_absolute_path);
        deleteFile(test2_absolute_path);
        createFile(test1_absolute_path, test1_data);
        createFile(test2_absolute_path, test2_data);
    }

    @AfterClass
    static public void Teardown() {
        deleteFile(test1_absolute_path);
        deleteFile(test2_absolute_path);
    }

    static private void createFile(String name, String content) throws IOException {
        File file = new File(name);
        FileWriter fileWriter = new FileWriter(file, true);
        fileWriter.write(content);
        fileWriter.close();
    }

    static private void deleteFile(String name) {
        //noinspection ResultOfMethodCallIgnored
        new File(name).delete();
    }


    @org.junit.Test
    public void calculate() throws Exception {
        Calculator calculator = new CalculatorImpl();
        Result result = calculator.calculate(test1_absolute_path).get();
        Assert.assertEquals(test1_absolute_path, result.getFilePath());
        Assert.assertEquals(test1_hash, result.getHash());
    }

    @org.junit.Test
    public void calculateAll() throws Exception {
        Calculator calculator = new CalculatorImpl();
        List<Result> result = calculator.calculateAll(tempDir).get();
        Assert.assertTrue(result.size() >= 2);

        Result result1 = result.stream().filter(it -> it.getFilePath().equals(test1_absolute_path)).collect(Collectors.toList()).get(0);
        Result result2 = result.stream().filter(it -> it.getFilePath().equals(test2_absolute_path)).collect(Collectors.toList()).get(0);

        Assert.assertNotNull(result1);
        Assert.assertNotNull(result2);

        Assert.assertEquals(test1_hash, result1.getHash());
        System.out.println(result2);
        Assert.assertEquals(test2_hash, result2.getHash());
    }
}
