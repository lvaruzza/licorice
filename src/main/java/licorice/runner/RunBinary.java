package licorice.runner;

import com.google.common.collect.ObjectArrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class RunBinary {
    private static Logger log = LoggerFactory.getLogger(RunBinary.class);

    private static String baseDir=".";

    public static void setBaseDir(String base) {
        baseDir = base;
    }

    public String getBinary(String name) {
        switch(System.getProperty("os.name")) {
            case "Linux":
                return baseDir + "/native/linux/" + name;
            default:
                return baseDir + "\\native\\windows\\" + name;
        }
    }

    public int run(String... args) {
        Process p;
        log.info("Running: '" + String.join(" ",args) + "' on " + System.getProperty("os.name"));
        try {
            p = Runtime.getRuntime().exec(args);
            p.waitFor();
            BufferedReader errorReader =
                    new BufferedReader(new InputStreamReader(p.getErrorStream()));

            BufferedReader outReader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String lineErr = null ;
            String lineOut = null;

            while ((lineErr = errorReader.readLine())!= null ||
                   (lineOut = outReader.readLine())!= null){
                if (lineOut != null) log.info(lineOut);
                if (lineErr != null) log.error(lineErr);
            }
            return p.exitValue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int runBinary(String name,String... args) {
        String programPath = getBinary(name);
        if (!new File(programPath).exists()) {
            String msg = String.format("Executable '%s' does not exist", programPath);
            log.error(msg);
            throw new RuntimeException(msg);
        }
        return run(ObjectArrays.concat(programPath,args));
    }
}
