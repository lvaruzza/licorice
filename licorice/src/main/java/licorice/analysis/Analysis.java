package licorice.analysis;

import java.nio.file.Path;
import java.util.stream.Stream;

import gatkrunner.gatk.GATKFacade;
import utils.reference.GenomeRef;

public class Analysis {
	private GATKFacade gatk;
	
	private Thread thread;
	
	public Analysis(final GenomeRef reference,final Path output,final Stream<Path> variants) {
		gatk = new GATKFacade();
		Runnable gatkTask = () -> {
			gatk.combineVariants(reference, variants, output);
		};		
		thread = new Thread(gatkTask);
	}
	
	public void start() {
		thread.start();
	}
	
	
}
