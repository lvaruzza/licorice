package licorice.cli;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import licorice.analysis.OutputFormat;
import licorice.runner.RunBinary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import licorice.analysis.Analysis;
import utils.FilesSource;
import utils.reference.SimpleGenomeRef;

public class Main {
	private static Logger logger = LoggerFactory.getLogger(Main.class);
	
	public static class Parameters {
		@Parameter(names = {"-i","--input"}, description = "Directory or ZIP  with vcf files", variableArity = true,required=true)
		private List<String> inputDir;

		@Parameter(names = {"-r","--reference"}, description = "Reference Genome", arity = 1,required=true)
		private String reference;

		@Parameter(names = {"-o","--output"}, description = "Output file name", arity = 1,required=true)
		private String output;

		@Parameter(names = {"-q","--minQual"}, description = "Minimum variant Quality (default=15)", arity = 1,required=false)
		private int minQual = 15;

		@Parameter(names = {"-c","--maxNonCall"}, description = "Maximum non call rate(default=0.95)", arity = 1,required=false)
		private double maxNC = 0.95;

		@Parameter(names = {"-t","--no-transpose"}, description = "Do NOT Transpose Matrix",required=false)
		private boolean notTranspose = false;

		@Parameter(names = {"-f","--output-format"}, description = "Output Format (valid options: 1,2,S,X,N)",required=false)
		private String outputFormatName = "default";

		public List<String> getInputDir() {
			return inputDir;
		}

		public void setInputDir(List<String> inputDir) {
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

		public boolean isNotTranspose() {
			return notTranspose;
		}

		public int getMinQual() {
            return minQual;
        }

		public double getMaxNC() {

			return maxNC;
		}

		public String getOutputFormatName() {
			return outputFormatName;
		}
	}
	
	public void run(String[] argv) throws URISyntaxException {
		Parameters pars = new Parameters();
		JCommander jc = new JCommander(pars);
		jc.setProgramName("licorice");

		URL classesRootDirURL = getClass().getProtectionDomain().getCodeSource().getLocation();
		Path  appRootDir = Paths.get(classesRootDirURL.toURI()).getParent().getParent();

		logger.info(String.format("Licorice Path '%s'",appRootDir));

		RunBinary.setBaseDir(appRootDir);

		try {
			jc.parse(argv);
		} catch(ParameterException e) {
			System.err.println(e.getMessage());
			jc.usage();
			System.exit(-1);
		}



		try {

			Stream<Path> source = new FilesSource(pars.getInputDir()).stream();

			Analysis analysis = new Analysis(
					OutputFormat.getFormat(pars.getOutputFormatName(),!pars.isNotTranspose()),
					new SimpleGenomeRef(Paths.get(pars.getReference())),
					pars.getMinQual(),
					pars.getMaxNC(),
					Paths.get(pars.getOutput()),
					source
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
		try {
			main.run(argv);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
