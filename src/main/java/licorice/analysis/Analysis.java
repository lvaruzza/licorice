package licorice.analysis;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gatkrunner.gatk.GATKFacade;
import gatkrunner.gatk.VCFUtils;
import utils.ZipUtil;
import utils.reference.GenomeRef;

public class Analysis {
	private static Logger logger = LoggerFactory.getLogger(Analysis.class);
	
	private GATKFacade gatk;
	private Path outputDir;
	private String base;
	private Path combined;
	private Matricifier mat = new Matricifier();
	private Callable<Void> onfinish = null; 
	private Consumer<Integer> progressConsumer = null; 
	private Thread thread;

	private UncaughtExceptionHandler onexception;
	
	public Analysis(final GenomeRef reference,final Path output,final Path variants) throws IOException {
		this(reference,output,VCFUtils.listVCFFiles(ZipUtil.directoryfy(variants)));
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
			if (progressConsumer!=null) progressConsumer.accept(50);
			try {
				mat.matricify(combined, output);
			} catch (IOException e1) {
				throw new RuntimeException(e1);
			}
			logger.info("====> Analysis finished.");
			if (progressConsumer!=null) progressConsumer.accept(100);
			if (onfinish!=null) {
				try {
					onfinish.call();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};		
		thread = new Thread(gatkTask);
		if (onexception != null)
			thread.setUncaughtExceptionHandler(onexception);
	}
	
	public void start() {
		thread.start();
	}
	
	public boolean isCompleted() {
		return !thread.isAlive();
	}
	
	public void onFinish(Callable<Void> callback) {
		onfinish = callback;
	}
	
	public void onException(UncaughtExceptionHandler h) {
		onexception=h;
	}
	
	public void progressListener(Consumer<Integer> consumer	) {
		progressConsumer = consumer;
	}
}
