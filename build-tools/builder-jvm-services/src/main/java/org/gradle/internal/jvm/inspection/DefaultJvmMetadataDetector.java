package org.gradle.internal.jvm.inspection;

import com.google.common.io.Files;
import com.tyron.common.TestUtil;

import org.gradle.api.JavaVersion;
import org.gradle.api.internal.file.temp.TemporaryFileProvider;
import org.gradle.internal.jvm.Jvm;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.process.ExecResult;
import org.gradle.process.internal.ExecException;
import org.gradle.process.internal.ExecHandleBuilder;
import org.gradle.process.internal.ExecHandleFactory;
import org.gradle.util.internal.GFileUtils;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.EnumMap;


public class DefaultJvmMetadataDetector implements JvmMetadataDetector {

    private final ExecHandleFactory execHandleFactory;
    private final TemporaryFileProvider temporaryFileProvider;

    @Inject
    public DefaultJvmMetadataDetector(
        final ExecHandleFactory execHandleFactory,
        final TemporaryFileProvider temporaryFileProvider
    ) {
        this.execHandleFactory = execHandleFactory;
        this.temporaryFileProvider = temporaryFileProvider;
    }

    @Override
    public JvmInstallationMetadata getMetadata(File javaHome) {
        if (TestUtil.isDalvik()) {
            return JvmInstallationMetadata.from(
                    javaHome,
                    "1.8",
                    "1.8",
                    "1.8",
                    "Android",
                    "Android",
                    ""
            );
        }
        if (javaHome == null || !javaHome.exists()) {
            return failure(javaHome, "No such directory: " + javaHome);
        }
        EnumMap<ProbedSystemProperty, String> metadata;
        if (Jvm.current().getJavaHome().equals(javaHome)) {
            return getMetadataFromCurrentJvm(javaHome);
        }
        return getMetadataFromInstallation(javaHome);
    }

    private JvmInstallationMetadata getMetadataFromCurrentJvm(File javaHome) {
        EnumMap<ProbedSystemProperty, String> result = new EnumMap<>(ProbedSystemProperty.class);
        for (ProbedSystemProperty type : ProbedSystemProperty.values()) {
            if (type != ProbedSystemProperty.Z_ERROR) {
                result.put(type, System.getProperty(type.getSystemPropertyKey()));
            }
        }
        return asMetadata(javaHome, result);
    }

    private JvmInstallationMetadata asMetadata(File javaHome, EnumMap<ProbedSystemProperty, String> metadata) {
        String implementationVersion = metadata.get(ProbedSystemProperty.VERSION);
        if (implementationVersion == null) {
            return failure(javaHome, metadata.get(ProbedSystemProperty.Z_ERROR));
        }
        try {
            JavaVersion.toVersion(implementationVersion);
        } catch (IllegalArgumentException e) {
            return failure(javaHome, "Cannot parse version number: " + implementationVersion);
        }
        String runtimeVersion = metadata.get(ProbedSystemProperty.RUNTIME_VERSION);
        String jvmVersion = metadata.get(ProbedSystemProperty.VM_VERSION);
        String vendor = metadata.get(ProbedSystemProperty.VENDOR);
        String implementationName = metadata.get(ProbedSystemProperty.VM);
        String architecture = metadata.get(ProbedSystemProperty.ARCH);
        return JvmInstallationMetadata.from(javaHome, implementationVersion, runtimeVersion, jvmVersion, vendor, implementationName, architecture);
    }

    private JvmInstallationMetadata getMetadataFromInstallation(File jdkPath) {
        File tmpDir = temporaryFileProvider.createTemporaryDirectory("jvm", "probe");
        File probe = writeProbeClass(tmpDir);
        ExecHandleBuilder exec = execHandleFactory.newExec();
        exec.setWorkingDir(probe.getParentFile());
        exec.executable(javaExecutable(jdkPath));
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream errorOutput = new ByteArrayOutputStream();
            String mainClassname = Files.getNameWithoutExtension(probe.getName());
            exec.args("-cp", ".", mainClassname);
            exec.setStandardOutput(out);
            exec.setErrorOutput(errorOutput);
            exec.setIgnoreExitValue(true);
            ExecResult result = exec.build().start().waitForFinish();
            int exitValue = result.getExitValue();
            if (exitValue == 0) {
                return parseExecOutput(jdkPath, out.toString());
            }
            return failure(jdkPath, "Command returned unexpected result code: " + exitValue + "\nError output:\n" + errorOutput);
        } catch (ExecException ex) {
            return failure(jdkPath, ex);
        } finally {
            GFileUtils.deleteQuietly(tmpDir);
        }
    }


    private static File javaExecutable(File jdkPath) {
        return new File(new File(jdkPath, "bin"), OperatingSystem.current().getExecutableName("java"));
    }

    private JvmInstallationMetadata parseExecOutput(File jdkPath, String probeResult) {
        String[] split = probeResult.split(System.getProperty("line.separator"));
        if (split.length != ProbedSystemProperty.values().length - 1) { // -1 because of Z_ERROR
            final String errorMessage = "Unexpected command output: \n" + probeResult;
            return failure(jdkPath, errorMessage);
        }
        EnumMap<ProbedSystemProperty, String> result = new EnumMap<>(ProbedSystemProperty.class);
        for (ProbedSystemProperty type : ProbedSystemProperty.values()) {
            if (type != ProbedSystemProperty.Z_ERROR) {
                result.put(type, split[type.ordinal()].trim());
            }
        }
        return asMetadata(jdkPath, result);
    }

    private JvmInstallationMetadata failure(File jdkPath, String errorMessage) {
        return JvmInstallationMetadata.failure(jdkPath, errorMessage);
    }

    private JvmInstallationMetadata failure(File jdkPath, Exception cause) {
        return JvmInstallationMetadata.failure(jdkPath, cause.getMessage());
    }

    private File writeProbeClass(File tmpDir) {
        return new MetadataProbe().writeClass(tmpDir);
    }

}
