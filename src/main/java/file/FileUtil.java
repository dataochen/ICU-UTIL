package file;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUtil.class);

    public static List<String> listAllDirFiles(String url) {
        Path start = Paths.get(url);
        try (Stream<Path> stream = Files.walk(start, Integer.MAX_VALUE)) {
            List<String> collect = stream
                    .map(String::valueOf)
                    .filter(s -> !new File(s).isDirectory())
                    .sorted()
                    .collect(Collectors.toList());

            return collect;
        } catch (Exception e) {
            LOGGER.error("listAllDirFiles e={}", e);
            return null;
        }
    }

    /**
     * 优雅的删除文件
     *
     * @param file
     */
    public static void deleteOnExist(File file) {
        if (null == file) {
            return;
        }
        if (!file.exists()) {
            LOGGER.warn("deleteOnExist file not exist,then just return ,fileName={}", file.getName());
            return;
        }
        try {
            boolean delete = file.delete();
            if (delete) {
                LOGGER.info("deleteOnExist success,fileName={}", file.getName());
            } else {
                LOGGER.warn("deleteOnExist fail,fileName={}", file.getName());
            }
        } catch (Exception e) {
            LOGGER.warn("deleteOnExist fail,fileName={},e={}", file.getName(), e);
        }
    }
}
