package org.gradle.api.internal.file.collections;

import org.gradle.api.file.DirectoryTree;
import org.gradle.api.file.RelativePath;
import org.gradle.api.internal.file.DefaultFileTreeElement;
import org.gradle.internal.nativeintegration.filesystem.FileSystem;

import java.io.File;

public abstract class DirectoryTrees {
    private DirectoryTrees() {
    }

    public static boolean contains(FileSystem fileSystem, DirectoryTree tree, File file) {
        String prefix = tree.getDir().getAbsolutePath() + File.separator;
        if (!file.getAbsolutePath().startsWith(prefix)) {
            return false;
        }

        RelativePath path = RelativePath.parse(true, file.getAbsolutePath().substring(prefix.length()));
        return tree.getPatterns().getAsSpec().test(new DefaultFileTreeElement(file, path, fileSystem, fileSystem));
    }

}