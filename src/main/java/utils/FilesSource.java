package utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FilesSource {

    private static Logger logger = LoggerFactory.getLogger(FilesSource.class);

    private List<Path> paths;
    private String[] exts;
    private List<Path> lst;

    public FilesSource(List<String> inputDir,String... extensions)  {
        paths = inputDir.stream().map( p -> Paths.get(p)).collect(Collectors.toList());
        exts = extensions;

        lst = paths.stream().map( p -> ZipUtil.directoryfy(p)).collect(Collectors.toList());

        lst.forEach( p -> logger.info(String.format("Looking at path '%s'",p)));
    }

    public Stream<Path> stream() {
        return lst.stream().flatMap( p -> VCFUtils.listVCFFiles(p));
    }
}
