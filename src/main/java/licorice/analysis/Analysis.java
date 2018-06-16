package licorice.analysis;

import gatkrunner.gatk.GATKFacade;
import gatkrunner.gatk.VCFUtils;
import licorice.runner.RunBCFtools;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utils.ZipUtil;
import utils.reference.GenomeRef;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Analysis {
	private static Logger logger = LoggerFactory.getLogger(Analysis.class);
	
	private Path outputDir;
	private String base;
	private Path combined;
	private Callable<Void> onfinish = null;
	private Consumer<Integer> progressConsumer = null; 
	private Thread thread;
    private boolean useGATK = false;

	private UncaughtExceptionHandler onexception;
	
	public Analysis(final GenomeRef reference,final int minQual,boolean transpose,final Path output,final Path variants) throws IOException {
		this(reference,minQual,transpose,output,VCFUtils.listVCFFiles(ZipUtil.directoryfy(variants)));
	}

    private void combineVariantsGATK(final GenomeRef reference,final Stream<Path> variants) {
        GATKFacade gatk = new GATKFacade();
        logger.info("Generating " + combined.toString() + " file using GATK");
        gatk.combineVariants(reference, variants, combined);
    }

    private void combineVariantsBCF(final GenomeRef reference,final int minQual,final Stream<Path> variants) {
        logger.info("Generating " + combined.toString() + " file using bcftools");
        RunBCFtools runner = new RunBCFtools();
		try {
			runner.combineVariantsByChunks(reference,minQual,combined,variants);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Analysis(final GenomeRef reference,final int minQual,boolean transpose,final Path output,final Stream<Path> variants) {
		Matricifier mat = new Matricifier(transpose);
		logger.info("Output file " + output);
		outputDir = output.toAbsolutePath().getParent();
		base = FilenameUtils.removeExtension(output.toString());
		logger.info("Output dir " + outputDir);
		logger.info("Output basename " + base);
		combined = outputDir.resolve(base + ".vcf.gz");
		Runnable analysisTask = () -> {
		    if (useGATK) {
                combineVariantsGATK(reference,variants);
            } else {
		        combineVariantsBCF(reference,minQual,variants);
            }
            if(!Files.exists(combined)) {
		        throw new RuntimeException(String.format("File '%s' not generated, fail in merging",combined.toString()));
            }
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
		thread = new Thread(analysisTask);
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
