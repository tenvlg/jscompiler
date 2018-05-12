package ru.tenvlg.utils;


import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.List;

/**
 * Goal which touches a timestamp file.
 */
@Mojo(name = "compile", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class MyMojo
        extends AbstractMojo {

    @Parameter(property = "workFolder", required = true)
    private String workFolder;

    @Parameter(property = "rootJs", required = true)
    private String rootJs;

    @Parameter(property = "paths", required = true)
    private List<String> paths;

    @Parameter(property = "inputs", required = true)
    private List<String> inputs;

    @Parameter(property = "templates", required = true)
    private List<String> templates;

    @Parameter(property = "outputJsps", required = true)
    private List<String> outputJsps;

    @Parameter(property = "outputJs", required = true)
    private List<String> outputJs;

    @Parameter(property = "emitUseStrict", required = true)
    private boolean emitUseStrict;

    public void execute() throws MojoFailureException {
        try {
            JSCompiler.compile(this.workFolder, this.rootJs, this.paths, this.inputs, this.templates, this.outputJsps, this.outputJs, this.emitUseStrict);
        } catch (Exception e) {
            throw new MojoFailureException("js compile error", e);
        }
    }
}
