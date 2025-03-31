package org.gradle.api.internal.file;

import org.gradle.internal.file.PathToFileResolver;

import java.io.File;

public class DefaultFileLookup implements FileLookup {
    private final IdentityFileResolver fileResolver = new IdentityFileResolver();

    @Override
    public FileResolver getFileResolver() {
        return fileResolver;
    }

    @Override
    public PathToFileResolver getPathToFileResolver() {
        return getFileResolver();
    }

    @Override
    public FileResolver getFileResolver(File baseDirectory) {
        return fileResolver.withBaseDir(baseDirectory);
    }

    @Override
    public PathToFileResolver getPathToFileResolver(File baseDirectory) {
        return getFileResolver(baseDirectory);
    }
}