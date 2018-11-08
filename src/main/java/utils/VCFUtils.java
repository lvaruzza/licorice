package utils;

import htsjdk.tribble.index.Index;
import htsjdk.tribble.index.IndexFactory;
import htsjdk.tribble.util.LittleEndianOutputStream;
import htsjdk.variant.vcf.VCFCodec;
import htsjdk.variant.vcf.VCFFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class VCFUtils {
	private static Logger logger = LoggerFactory.getLogger(VCFUtils.class);
	
	public static Stream<Path> listVCFFiles(Path variantsDir) throws IOException {
		PathMatcher vcfMatcher = FileSystems.getDefault().getPathMatcher("glob:*.{vcf,vcf.gz,bcf}");
		logger.info(String.format("Looking for variants in '%s'", variantsDir.toString()));
		final  Stream.Builder<Path> builder = Stream.builder();

		/*Stream<Path> variants=Files.list(variantsDir)
		    	.filter((Path p) -> vcfMatcher.matches(p.getFileName()))
		        .peek( (Path p) -> logger.info(String.format("Detect variant '%s' matches='%b'", p.toString(),vcfMatcher.matches(p.getFileName()))))
		    	;
		*/

		Files.walkFileTree(variantsDir,new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException
            {
                if (vcfMatcher.matches(file.getFileName())) builder.accept(file);
                return FileVisitResult.CONTINUE;
            }
		});

		return builder.build();
	}

	public static String getSampleName(Path path) {
        File varFile = path.toFile();
        VCFFileReader reader = new VCFFileReader(varFile,false);
        ArrayList<String> names=reader.getFileHeader().getSampleNamesInOrder();

        if(names.size()>1) throw new RuntimeException(String.format("File '%s' is a multisamples VCF",path.toString()));

        return names.get(0);
    }

	public static Map<String,String> makeSamplesDictionary(Stream<Path> files) {
		Map<String,String> m = new HashMap<String,String>();

        files.forEach( p -> m.put(getSampleName(p),p.getFileName().toString()));

		return m;
    }

	public static Path indexPath(Path v) {
		return PathUtils.changeExtension(v,"idx");
	}

	public static boolean isVCF(Path file) {
		return PathUtils.checkExtension(file,"vcf");
	}

	public static boolean isBCF(Path file) {
		return PathUtils.checkExtension(file,"bcf");
	}

	public static void indexVCF(Path v)  {
		try {
			LittleEndianOutputStream out = new LittleEndianOutputStream(new BufferedOutputStream(new FileOutputStream(indexPath(v).toFile())));
			Index index=IndexFactory.createDynamicIndex(v.toFile(), new VCFCodec());
			index.write(out);
			out.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Stream<Path> assureIndex(Stream<Path> variants) {
		return variants.filter( v -> !indexPath(v).toFile().exists() && !isBCF(v)).map( v -> {
			indexVCF(v);
			return v;
		});
	}
}
