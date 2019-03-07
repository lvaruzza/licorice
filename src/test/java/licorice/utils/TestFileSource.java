package licorice.utils;

import org.testng.annotations.Test;
import utils.FilesSource;

import java.util.List;

import static org.testng.AssertJUnit.assertEquals;

public class TestFileSource {

    //@Test
    public void testList() {
        FilesSource fs = new FilesSource(List.of("/data/customers/longping10k/vcfs/R_2019_01_25_13_05_38_user_S5-TFS-Schlieren-50-Long_Ping_10K_Maize_Plate_1","/data/customers/longping10k/vcfs/R_2019_01_25_15_40_58_user_S5-TFS-Schlieren-51-Long_Ping_10K_Maize_Plate_2"));

        fs.stream().forEach( p -> System.out.println(p) );
        assertEquals(191,fs.stream().count());
    }
}
