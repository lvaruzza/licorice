package licorice.runner;

import com.google.common.collect.ObjectArrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class RunBinary {
    private static Logger log = LoggerFactory.getLogger(RunBinary.class);

    public String getBinary(String name) {
        switch(System.getProperty("os.name")) {
            case "Linux":
                return "native/linux/" + name;
            default:
                return "native\\windows\\" + name;
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
        return run(ObjectArrays.concat(getBinary(name),args));
    }
}
