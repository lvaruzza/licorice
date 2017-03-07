package utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

public class ZipUtil {
	public static boolean isZip(Path zipFile) {
		return FilenameUtils.getExtension(zipFile.getFileName().toString()).equals("zip");
	}
	
	public static void unzip(Path zipfilePath,Path outputDir) throws ZipException, IOException {
		ZipFile zf = new ZipFile(zipfilePath.toFile());
		Enumeration<? extends ZipEntry> entries = zf.entries();
		while (entries.hasMoreElements()) {
	        ZipEntry entry = entries.nextElement();
			File entryDestination = new File(outputDir.toString(), entry.getName());
	        if (entry.isDirectory()) {
	            entryDestination.mkdirs();
	        } else {
	            InputStream in = zf.getInputStream(entry);
	            OutputStream out = new FileOutputStream(entryDestination);
	            IOUtils.copy(in, out);
	            IOUtils.closeQuietly(in);
	            IOUtils.closeQuietly(out);
	        }
		}
		zf.close();
	}
	
	public static Path directoryfy(Path input) throws IOException {
		if (isZip(input)) {
			Path tempDir =  Files.createTempDirectory(Paths.get("."),"zipfile");
			ZipUtil.unzip(input, tempDir);
			FileUtils.forceDeleteOnExit(tempDir.toFile());
			return tempDir;
		} else {
			return input;
		}
	}
}
