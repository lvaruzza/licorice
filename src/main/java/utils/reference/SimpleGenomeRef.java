package utils.reference;

import java.nio.file.Files;
import java.nio.file.Path;

public class SimpleGenomeRef implements GenomeRef {
	private Path file;
	
	public SimpleGenomeRef(Path refFile) {
		this.file=refFile.toAbsolutePath();
	}
	@Override
	public Path fastaFile() {
		return file;
	}

	@Override
	public ValidationResult validate() {
        ValidationResult r = new ValidationResult();

        if (!Files.exists(file)) {
            r.addError(String.format("Reference genome file '%s' not found.",file.toString()));
        }
        Path faiFile = file.resolveSibling(file.getFileName() + ".fai");
        if (!Files.exists(faiFile)) {
            r.addError(String.format("Reference index '%s' not found.",faiFile.toString()));
        }
		return r;
	}

}
