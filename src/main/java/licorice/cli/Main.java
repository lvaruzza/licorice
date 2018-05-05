package licorice.cli;

import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import licorice.analysis.Analysis;
import utils.reference.SimpleGenomeRef;

public class Main {
	private static Logger logger = LoggerFactory.getLogger(Main.class);
	
	public static class Parameters {
		@Parameter(names = {"-i","--input"}, description = "Directory or ZIP  with vcf files", arity = 1,required=true)
		private String inputDir;

		@Parameter(names = {"-r","--reference"}, description = "Reference Genome", arity = 1,required=true)
		private String reference;

		@Parameter(names = {"-o","--output"}, description = "Output file name", arity = 1,required=true)
		private String output;

		@Parameter(names = {"-q","--minQual"}, description = "Minimum variant Quality (default=30)", arity = 1,required=false)
		private int minQual = 30;

		public String getInputDir() {
			return inputDir;
		}

		public void setInputDir(String inputDir) {
			this.inputDir = inputDir;
		}

		public String getOutput() {
			return output;
		}

		public void setOutput(String output) {
			this.output = output;
		}

		public String getReference() {
			return reference;
		}

		public void setReference(String reference) {
			this.reference = reference;
		}


        public int getMinQual() {
            return minQual;
        }
    }
	
	public void run(String[] argv) {
		Parameters pars = new Parameters();
		JCommander jc = new JCommander(pars);
		jc.setProgramName("licorice");
		
		try {
			jc.parse(argv);
		} catch(ParameterException e) {
			System.err.println(e.getMessage());
			jc.usage();
			System.exit(-1);
		}
		
		try {
			Analysis analysis = new Analysis(
					new SimpleGenomeRef(Paths.get(pars.getReference())),
					pars.getMinQual(),
					Paths.get(pars.getOutput()),
					Paths.get(pars.getInputDir())
				);
			
			analysis.start();
			while(analysis.isCompleted()) {
				Thread.sleep(1000);				
			}
			logger.info("===> Program finished.");
		} catch (Exception e) {
			System.err.println("Analysis Failed");
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] argv) {
		Main main = new Main();
		main.run(argv);
	}

}
