package org.gradle.api.tasks.bundling;

import org.gradle.api.internal.file.archive.TarCopyAction;
import org.gradle.api.internal.file.archive.compression.ArchiveOutputStreamFactory;
import org.gradle.api.internal.file.archive.compression.Bzip2Archiver;
import org.gradle.api.internal.file.archive.compression.GzipArchiver;
import org.gradle.api.internal.file.archive.compression.SimpleCompressor;
import org.gradle.api.internal.file.copy.CopyAction;
import org.gradle.api.tasks.Input;
import org.gradle.work.DisableCachingByDefault;

/**
 * Assembles a TAR archive.
 */
@DisableCachingByDefault(because = "Not worth caching")
public class Tar extends AbstractArchiveTask {
    private Compression compression = Compression.NONE;

    public Tar() {
        getArchiveExtension().set(getProject().provider(() -> getCompression().getDefaultExtension()));
    }

    @Override
    protected CopyAction createCopyAction() {
        return new TarCopyAction(getArchiveFile().get().getAsFile(), getCompressor(), isPreserveFileTimestamps());
    }

    private ArchiveOutputStreamFactory getCompressor() {
        switch(compression) {
            case BZIP2: return Bzip2Archiver.getCompressor();
            case GZIP:  return GzipArchiver.getCompressor();
            default:    return new SimpleCompressor();
        }
    }

    /**
     * Returns the compression that is used for this archive.
     *
     * @return The compression. Never returns null.
     */
    @Input
    public Compression getCompression() {
        return compression;
    }

    /**
     * Configures the compressor based on passed in compression.
     *
     * @param compression The compression. Should not be null.
     */
    public void setCompression(Compression compression) {
        this.compression = compression;
    }

}
