package org.gradle.internal.resource.local;

import org.gradle.internal.resource.ReadableContent;
import org.gradle.internal.resource.ResourceExceptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class FileReadableContent implements ReadableContent {
    private final File file;

    public FileReadableContent(File file) {
        this.file = file;
    }

    @Override
    public long getContentLength() {
        return file.length();
    }

    @Override
    public InputStream open() {
        try {
            return new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw ResourceExceptions.readMissing(file, e);
        }
    }
}
