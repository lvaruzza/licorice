package utils.reference;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public interface GenomeRef {
	public Path fastaFile();


	public ValidationResult validate();

	public static class ValidationResult {
	    private boolean valid = true;
        private List<String> errorMessages = new ArrayList<String>();

        public void addError(String message) {
            valid=false;
            errorMessages.add(message);
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getErrors() {
            return errorMessages;
        }
    }
}
