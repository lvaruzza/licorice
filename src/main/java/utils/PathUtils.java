package utils;

import com.google.common.io.Files;
import org.apache.commons.io.FilenameUtils;

import java.nio.file.Path;

public class PathUtils {

	public static Path changeExtension(Path v, String ext) {
		String base = FilenameUtils.getBaseName(v.getFileName().toString());
		return v.resolveSibling(base+"."+ext);
	}

	public static boolean checkExtension(Path file, String ext) {
		return Files.getFileExtension(file.toString()).equals(ext);
	}
}
