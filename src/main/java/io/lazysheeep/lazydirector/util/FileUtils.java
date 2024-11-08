package io.lazysheeep.lazydirector.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileUtils
{
    public static List<String> getAllFileNames(String directoryPath)
    {
        try (Stream<Path> paths = Files.walk(Paths.get(directoryPath)))
        {
            return paths.filter(Files::isRegularFile)
                        .map(Path::getFileName)
                        .map(Path::toString)
                        .collect(Collectors.toList());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    public static List<String> getAllFileNames(String directoryPath, String extension, boolean includeExtension)
    {
        try (Stream<Path> paths = Files.walk(Paths.get(directoryPath)))
        {
            return paths.filter(Files::isRegularFile)
                        .map(Path::getFileName)
                        .map(Path::toString)
                        .filter(fileName -> fileName.endsWith(extension))
                        .map(fileName -> includeExtension ? fileName : fileName.substring(0, fileName.length() - extension.length()))
                        .collect(Collectors.toList());
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}