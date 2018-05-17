package licorice.runner;

import com.google.common.collect.ObjectArrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

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
                return baseDir + "\\native\\windows\\" + name + ".exe";
        }
    }

    public int run(String... args) {
        Process p;
        log.info("Running: '" + String.join(" ",args) + "' on " + System.getProperty("os.name"));
        try {
            ProcessBuilder pb = new ProcessBuilder(args);

            p = pb.start();

            BufferedReader errorReader =
                    new BufferedReader(new InputStreamReader(p.getErrorStream()));

            BufferedReader outReader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String lineErr = null ;
            String lineOut = null;

            while ((lineErr = errorReader.readLine())!= null ||
                   (lineOut = outReader.readLine())!= null){
                if (lineOut != null) log.debug(lineOut);
                if (lineErr != null) log.error(lineErr);
            }
            p.waitFor();
            return p.exitValue();
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    public int run(File stdout,String... args) {
        ProcessBuilder pb = new ProcessBuilder(args);
        log.info("Running: '" + String.join(" ",args) + "' on " + System.getProperty("os.name"));
        log.info(String.format("Redirecting output to file '%s'",stdout.getAbsolutePath()));
        try(PrintWriter out = new PrintWriter(stdout)) {
            Process p = pb.start();
            BufferedReader errorReader =
                    new BufferedReader(new InputStreamReader(p.getErrorStream()));

            BufferedReader outReader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String lineErr = null;
            String lineOut = null;

            while ((lineOut = outReader.readLine())!= null){
                if (lineOut != null) {
                    out.println(lineOut);
                }
            }
            p.waitFor();

            while ((lineErr = errorReader.readLine())!= null){
                log.error(lineErr);
            }

            return p.exitValue();
        } catch (Exception e) {
            log.error(e.getMessage());
            e.printStackTrace();
            return -1;
        }
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

    public int runBinary(String name,File stdout,String... args) {
        String programPath = getBinary(name);
        if (!new File(programPath).exists()) {
            String msg = String.format("Executable '%s' does not exist", programPath);
            log.error(msg);
            throw new RuntimeException(msg);
        }
        return run(stdout,ObjectArrays.concat(programPath,args));
    }

}
