package dev.fxe.recaf_forge;

import javafx.stage.DirectoryChooser;
import me.coley.recaf.ui.controls.ExceptionAlert;
import org.apache.commons.io.IOUtils;
import org.jline.utils.Log;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Filip
 */
public class Extractor {
    private static final DirectoryChooser dirChooser = new DirectoryChooser();

    public static Path getResourcePath(String name, String resourcePath) {
        try (InputStream in = Extractor.class.getResourceAsStream(resourcePath)) {
            String[] strings = resourcePath.split("\\.");
            String suffix = strings[strings.length - 1];
            if (in != null) {
                String fileName = name == null ? String.valueOf(in.hashCode()) : name;
                File tempFile = File.createTempFile(fileName, suffix);
                tempFile.deleteOnExit();
                saveFile(tempFile.toPath(), IOUtils.toByteArray(in));
                return tempFile.toPath();
            } else {
                Log.info("InputStream is null");
            }
        } catch (Exception ex) {
            ExceptionAlert.show(ex, "Failed to read resources");
        }
        return null;
    }

    public static Path extractMDK(Path mdkPath) {
        Path exportDir = getExportDir();
        if (exportDir == null) return null;
        String destDirectory = exportDir.toString();
        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(mdkPath.toFile()))) {
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                String filePath = destDirectory + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    extractFile(filePath, zipIn);
                } else {
                    File dir = new File(filePath);
                    dir.mkdirs();
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }
        } catch (Exception ex) {
            ExceptionAlert.show(ex, "Failed to extract mdk zip");
        }
        return exportDir;
    }

    private static void extractFile(String filePath, ZipInputStream zipIn) throws IOException {
        saveFile(Paths.get(filePath), IOUtils.toByteArray(zipIn));
    }

    public static void saveFile(Path path, byte[] data) {

        try {
            Files.createDirectories(path.getParent());

            try (
                final BufferedOutputStream out = new BufferedOutputStream(Files.newOutputStream(
                    path,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND))
            ) {
                out.write(data);
            }
        } catch (Exception ex) {
            ExceptionAlert.show(ex, "Failed to write file " + path);

        }
    }

    private static Path getExportDir() {
        File file = dirChooser.showDialog(null);
        if (file != null) {
            return Paths.get(file.getPath() + "/mdk/");
        }
        return null;
    }
}
