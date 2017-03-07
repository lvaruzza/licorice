package licorice.analysis;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gatkrunner.gatk.GATKFacade;
import gatkrunner.gatk.Utils;
import utils.ZipUtil;
import utils.reference.GenomeRef;

public class Analysis {
	private static Logger logger = LoggerFactory.getLogger(Analysis.class);
	
	private GATKFacade gatk;
	private Path outputDir;
	private String base;
	private Path combined;
	private Matricifier mat = new Matricifier();

	private Thread thread;
	
	public Analysis(final GenomeRef reference,final Path output,final Path variants) throws IOException {
		this(reference,output,Utils.listVCFFiles(ZipUtil.directoryfy(variants)));
	}
	
	public Analysis(final GenomeRef reference,final Path output,final Stream<Path> variants) {
		logger.info("Output file " + output);
		outputDir = output.toAbsolutePath().getParent();
		base = FilenameUtils.removeExtension(output.toString());
		logger.info("Output dir " + outputDir);
		logger.info("Output basename " + base);
		combined = outputDir.resolve(base + ".vcf");
		gatk = new GATKFacade();
		Runnable gatkTask = () -> {
			logger.info("Generating " + combined.toString() + " file");
			gatk.combineVariants(reference, variants, combined);
			logger.info("Matricifying " + combined.toString());
			try {
				mat.matricify(combined, output);
			} catch (IOException e) {
				e.printStackTrace();
			}
			logger.info("====> Analysis finished.");
		};		
		thread = new Thread(gatkTask);
	}
	
	public void start() {
		thread.start();
	}
	
	public boolean isCompleted() {
		return !thread.isAlive();
	}
}
