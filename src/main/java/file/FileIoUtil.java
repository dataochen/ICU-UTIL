package file;

import org.apache.poi.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * @author dataochen
 * @Description 文件io操作类
 * @date: 2019/5/15 16:23
 */
public class FileIoUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileIoUtil.class);
    /**
     * 待删除的文件
     */
    public static List<File> files = new LinkedList<>();

    /**
     * 强制gc 关闭流 删除文件
     */
    public static void deleteFiles4Gc() {
        if (CollectionUtils.isEmpty(files)) {
            return;
        }
        System.gc();
        files.forEach(s -> s.delete());
    }

    /**
     * zip解压到指定路径
     *
     * @param srcFile     zip源文件
     * @param destDirPath 解压后的目标文件夹
     * @throws RuntimeException 解压失败会抛出运行时异常
     */
    public static void unZip(File srcFile, String destDirPath) throws RuntimeException {
        long start = System.currentTimeMillis();
        // 判断源文件是否存在
        if (!srcFile.exists()) {
            throw new RuntimeException(srcFile.getPath() + "this file not exists");
        }
        // 开始解压
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(srcFile);
            Enumeration<?> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                LOGGER.debug("解压" + entry.getName());
                // 如果是文件夹，就创建个文件夹
                if (entry.isDirectory()) {
                    String dirPath = "";
                    String property = System.getProperty("os.name");
                    if (property != null && (property.contains("windows") || property.contains("Windows"))) {
                        dirPath = destDirPath + "\\" + entry.getName();
                    } else {
                        dirPath = destDirPath + "/" + entry.getName();
                    }
                    File dir = new File(dirPath);
                    dir.mkdirs();
                } else {
                    // 如果是文件，就先创建一个文件，然后用io流把内容copy过去
                    File targetFile = new File(destDirPath + "/" + entry.getName());
                    // 保证这个文件的父文件夹必须要存在
                    if (!targetFile.getParentFile().exists()) {
                        targetFile.getParentFile().mkdirs();
                    }
                    targetFile.createNewFile();
                    // 将压缩文件内容写入到这个文件中
                    InputStream is = zipFile.getInputStream(entry);
                    FileOutputStream fos = new FileOutputStream(targetFile);
                    IOUtils.copy(is, fos);
                    // 关流顺序，先打开的后关闭
                    fos.close();
                    is.close();
                }
            }
            long end = System.currentTimeMillis();
            LOGGER.debug("解压完成，耗时：" + (end - start) + " ms");
        } catch (Exception e) {
            throw new RuntimeException("unzip error from ZipUtils", e);
        } finally {
            if (zipFile != null) {
                try {
                    zipFile.close();
                } catch (IOException e) {

                }
            }
        }
    }

    /**
     * zip解压 获取文件列表
     *
     * @param srcFile zip源文件
     * @throws RuntimeException 解压失败会抛出运行时异常
     */
    public static List<ZipEntry> unZip(File srcFile) throws RuntimeException {
        LinkedList<ZipEntry> zipEntries = new LinkedList<>();
        long start = System.currentTimeMillis();
        // 判断源文件是否存在
        if (!srcFile.exists()) {
            throw new RuntimeException(srcFile.getPath() + "this file not exists");
        }
        // 开始解压
        try (ZipFile zipFile = new ZipFile(srcFile)) {
            Enumeration<?> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                LOGGER.debug("解压 unzip" + entry.getName());
                // 如果不是文件夹，就记录此文件
                if (!entry.isDirectory()) {
                    zipEntries.add(entry);
                }
            }
            long end = System.currentTimeMillis();
            LOGGER.debug("解压完成，耗时：" + (end - start) + " ms");
        } catch (Exception e) {
            throw new RuntimeException("unzip error from ZipUtils", e);
        }
        return zipEntries;
    }

    /**
     * MultipartFile 转 File
     *
     * @param file
     * @throws Exception
     */
    public static File multipartFileToFile(MultipartFile file) throws Exception {

        File toFile = null;
        if (file == null || file.getSize() <= 0) {
            return null;
        } else {
            toFile = new File(file.getOriginalFilename() + "_" + UUID.randomUUID().toString().replaceAll("-", ""));
//此处去生成新文件在服务器磁盘内
            try (InputStream inputStream = file.getInputStream(); FileOutputStream fileOutputStream = new FileOutputStream(toFile)) {
                IOUtils.copy(inputStream, fileOutputStream);
                fileOutputStream.flush();
            }
            return toFile;

        }

    }

    /**
     * 追加zip文件
     * 文件会损坏 无法解压
     *
     * @param files      多文件 此方法只支持根目录的
     * @param targetFile 压缩后的包
     * @param oriFile    压缩前的包
     * @return
     */
    @Deprecated
    public static void compressFiles2(List<File> files, File targetFile, File oriFile) throws IOException {
        if (CollectionUtils.isEmpty(files)) {
            return;
        }
////        ZipOutputStream是不支持直接向原.zip文件以追加方式添加文件的。如果需要实现，要将原zip文件都出，然后重新写入新zip文件中，最后写入要追加的文件。

        try (FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
             FileInputStream fileInputStream1 = new FileInputStream(oriFile);) {
            FileChannel channel = fileOutputStream.getChannel();
            FileChannel channel1 = fileInputStream1.getChannel();
            channel1.transferTo(0, channel.size(), channel);
//追加文件
            for (File file : files) {
                //  2019/5/17 多层结构文件是否可以压缩成功
//                ZipEntry entry = new ZipEntry(file.getName());
                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                    FileChannel channel2 = fileInputStream.getChannel();
                    channel.transferFrom(channel2, 0, channel2.size());
                    channel2.close();
                }
            }
            channel.close();
            channel1.close();
        }

    }

    /**
     * 压缩多文件
     *
     * @param files
     * @param targetFile
     * @param oriFile
     * @param time
     * @throws IOException
     */
    public static void compressFiles(List<Map<String, Object>> files, File targetFile, File oriFile, long time) throws IOException {
        if (CollectionUtils.isEmpty(files)) {
            return;
        }
////        ZipOutputStream是不支持直接向原.zip文件以追加方式添加文件的。如果需要实现，要将原zip文件都出，然后重新写入新zip文件中，最后写入要追加的文件。
        try (FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
             ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {
//            如果oriFile是个空zip 此处会报异常java.util.zip.ZipException: zip file is empty 直接catch并忽略
            try (ZipFile zipFile = new ZipFile(oriFile, Charset.defaultCharset())) {
////回写原zip中的文件
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry e = entries.nextElement();
                    e.setTime(time);
                    zipOutputStream.putNextEntry(e);
                    if (!e.isDirectory()) {
                        try (InputStream inputStream = zipFile.getInputStream(e)) {
                            IOUtils.copy(inputStream, zipOutputStream);
                        } finally {
                            zipOutputStream.closeEntry();
                        }

                    } else {
                        zipOutputStream.closeEntry();
                    }
                }
            } catch (Exception e) {
                LOGGER.info("有可能是空zip,忽略 ；e={}", e.getMessage());
            }
//追加文件
            for (Map<String, Object> file : files) {
                //  2019/5/17 多层结构文件是否可以压缩成功
                ZipEntry entry = new ZipEntry(file.get("fileName").toString());
                entry.setTime(time);
                zipOutputStream.putNextEntry(entry);
                try (FileInputStream fileInputStream = new FileInputStream((File) file.get("file"))) {
                    IOUtils.copy(fileInputStream, zipOutputStream);
                } finally {
                    zipOutputStream.closeEntry();
                }
            }
            zipOutputStream.flush();

        }
    }


    /**
     * 压缩多文件
     *
     * @param zipFile    包
     * @param files      包里的多文件 ZipEntry格式
     * @param targetFile 压缩后的包
     * @return
     */
    public static void compressFiles(List<ZipEntry> files, ZipFile zipFile, File targetFile) throws IOException {
        if (CollectionUtils.isEmpty(files)) {
            return;
        }
        try (FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
             ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream, Charset.defaultCharset())) {
//        先追加目录 1 目录去重
            Set<String> collect = files.stream().map(s -> {
                if (s == null) {
                    return null;
                }
                String name = s.getName();
//           判断是否为根目录
                if (!name.contains("/")) {
                    return "/";
                }
                String path = name.substring(0, name.lastIndexOf("/") + 1);
                return path;
            }).collect(Collectors.toSet());
            Set<ZipEntry> pathZipEntryList = collect.stream().map(s -> {
                ZipEntry zipEntry = new ZipEntry(s);
                return zipEntry;
            }).collect(Collectors.toSet());
            //        先追加目录 2添加目录
            for (ZipEntry zipEntry : pathZipEntryList) {
                if (zipEntry != null) {
                    zipOutputStream.putNextEntry(zipEntry);
                    zipOutputStream.closeEntry();
                }
            }
            for (ZipEntry file : files) {
                //  2019/5/17 多层结构文件是否可以压缩成功
                if (file == null) {
                    continue;
                }
//            在写文件
                ZipEntry zipEntry = new ZipEntry(file.getName());
                zipOutputStream.putNextEntry(zipEntry);
                InputStream inputStream = zipFile.getInputStream(file);
                try {
                    IOUtils.copy(inputStream, zipOutputStream);
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    zipOutputStream.closeEntry();
                }

            }

        }
    }

    /**
     * 流转文件
     *
     * @param inputStream
     * @param targetFile
     */
    public static void writeFile(InputStream inputStream, File targetFile) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(targetFile)) {
            IOUtils.copy(inputStream, fileOutputStream);
        }
    }

    /**
     * 字节转文件
     *
     * @param bytes
     * @param targetFile
     */
    public static void writeFileForByte(byte[] bytes, File targetFile) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(targetFile))) {
            bufferedOutputStream.write(bytes, 0, bytes.length);
            bufferedOutputStream.flush();
        }
    }

    /**
     * 字节转文件
     * 适用于大文件
     *
     * @param bytes
     * @param targetFile
     * @throws IOException
     */
    public static void writeFile4BigByte(byte[] bytes, File targetFile) throws IOException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(targetFile))) {
            byte[] buff = new byte[4096];

            int start = 0, end = bytes.length;
            while (start < end) {
                for (int i = 0; i < buff.length; i++) {
                    buff[i] = bytes[start++];
                    if (start == end) {
                        break;
                    }
                }
                bufferedOutputStream.write(buff, 0, buff.length);
                bufferedOutputStream.flush();
            }
        }
    }

}