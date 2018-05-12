package ru.tenvlg.utils;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.SourceFile;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class JSCompiler {
    private static Pattern reqReg = Pattern.compile("Ext\\.require\\([\\'\\\"]([^\\)]+)[\\'\\\"]\\)");
    private static Pattern provReg = Pattern.compile("Ext\\.provide\\([\\'\\\"]([^\\)]+)[\\'\\\"]\\)");

    public JSCompiler() {
    }

    private static void ResolveDependencies(String require, List<ParsedFile> parsedFiles, ClosureEntity entity, ArrayList<String> seenList) throws Exception {
        ParsedFile parsedFile = null;

        for (ParsedFile dep : parsedFiles) {
            if (dep.getProvides().contains(require)) {
                parsedFile = dep;
                break;
            }
        }

        if (parsedFile == null) {
            throw new Exception("Missing provider for " + require);
        } else {
            if (!seenList.contains(parsedFile.getFilename())) {
                seenList.add(parsedFile.getFilename());

                for (String subRequire : parsedFile.getRequires()) {
                    ResolveDependencies(subRequire, parsedFiles, entity, seenList);
                }

                entity.addDepFile(parsedFile.getFilename());
            }

        }
    }

    private static List<ParsedFile> ParseFiles(List<String> sources) throws IOException {
        List<ParsedFile> result = new ArrayList<>();

        for (String source : sources) {
            List<String> requires = new ArrayList<>();
            List<String> provides = new ArrayList<>();

            for (String line : Files.readAllLines(Paths.get(source), StandardCharsets.UTF_8)) {
                Matcher reqMatcher = reqReg.matcher(line);

                while (reqMatcher.find()) {
                    requires.add(reqMatcher.group(1));
                }

                Matcher provMatcher = provReg.matcher(line);

                while (provMatcher.find()) {
                    provides.add(provMatcher.group(1));
                }
            }

            result.add(new ParsedFile(source, requires, provides));
        }

        return ImmutableList.copyOf(result);
    }

    private static List<ClosureEntity> CalculateClosureEntities(List<String> sources, List<String> inputs) throws Exception {
        List<ClosureEntity> result = new ArrayList<>();
        List<ParsedFile> parsedFiles = ParseFiles(sources);

        for (String input : inputs) {
            ClosureEntity entity = new ClosureEntity(input);

            for (String line : Files.readAllLines(Paths.get(input), StandardCharsets.UTF_8)) {
                Matcher reqMatcher = reqReg.matcher(line);

                while (reqMatcher.find()) {
                    String require = reqMatcher.group(1);
                    ArrayList<String> seenList = new ArrayList<>();
                    seenList.add(input);
                    ResolveDependencies(require, parsedFiles, entity, seenList);
                }
            }

            entity.addDepFile(input);
            result.add(entity);
        }

        return ImmutableList.copyOf(result);
    }

    private static List<String> ExpandSources(List<String> refs) {
        List<File> result = new ArrayList<>();

        for (String ref : refs) {
            File dir = new File(ref);
            result.addAll(FileUtils.listFiles(dir, new String[]{"js"}, true));
        }

        return result.stream().map(File::getAbsolutePath).collect(Collectors.toList());
    }

    static void compile(final String workFolder, String rootJs, List<String> paths, List<String> inputs, List<String> templates, List<String> outputJsps, List<String> outputJs, boolean emitUseStrict) throws Exception {
        java.util.function.Function<List<String>, List<String>> appendWorkFolder = dirs -> dirs.stream().map(dir -> Paths.get(workFolder, dir).toString()).collect(Collectors.toList());

        paths = appendWorkFolder.apply(paths);
        inputs = appendWorkFolder.apply(inputs);
        templates = appendWorkFolder.apply(templates);
        outputJsps = appendWorkFolder.apply(outputJsps);
        outputJs = appendWorkFolder.apply(outputJs);

        if (inputs.size() == templates.size() && inputs.size() == outputJsps.size() && inputs.size() == outputJs.size()) {
            List<String> sources = ExpandSources(paths);
            List<ClosureEntity> entities = CalculateClosureEntities(sources, inputs);

            int i;
            for (i = 0; i < entities.size(); ++i) {
                Compiler compiler = new Compiler();
                CompilerOptions compilerOptions = new CompilerOptions().setEmitUseStrict(emitUseStrict);
                CompilationLevel.SIMPLE_OPTIMIZATIONS.setOptionsForCompilationLevel(compilerOptions);
                List<SourceFile> sourceInputs = entities.get(i).getDepFiles().stream().map(new Function<String, SourceFile>() {
                    @Nullable
                    public SourceFile apply(@Nullable String dep) {
                        return SourceFile.fromFile(dep);
                    }
                }).collect(Collectors.toList());
                compiler.compile(new ArrayList<>(), sourceInputs, compilerOptions);
                Files.write(Paths.get(outputJs.get(i)), compiler.toSource().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }

            for (i = 0; i < entities.size(); ++i) {
                List<String> scripts = new ArrayList<>();
                Iterator i$ = entities.get(i).getDepFiles().iterator();

                String p;
                String templateText;
                while (i$.hasNext()) {
                    templateText = (String) i$.next();
                    p = templateText;

                    String path;
                    for (Iterator j$ = paths.iterator(); j$.hasNext(); p = p.replace(path, "")) {
                        path = (String) j$.next();
                    }

                    p = rootJs + "/" + p.replace("\\", "/");
                    scripts.add(String.format("<script type=\"text/javascript\" src=\"%s\"></script>", p.replace("//", "/")));
                }

                String scriptsLines = Joiner.on("\n\t\t").join(scripts);
                templateText = new String(Files.readAllBytes(Paths.get(templates.get(i))), StandardCharsets.UTF_8);
                p = Pattern.compile("<!-- generated -->.*<!-- /generated -->", 32).matcher(templateText).replaceFirst(scriptsLines);
                Files.write(Paths.get(outputJsps.get(i)), p.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }

        } else {
            throw new Exception("Mismatch input size params");
        }
    }

    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption(new Option("f", "work-folder", true, "Work folder."));
        options.addOption(new Option("r", "root-js", true, "root dependencies."));
        options.addOption(new Option("p", "path", true, "The path that should be traversed to build the dependencies."));
        options.addOption(new Option("i", "input", true, "The inputs to calculate dependencies for."));
        options.addOption(new Option("t", "template", true, "The template jsp file for javascript dependencies inserting."));
        options.addOption(new Option("op", "output-jsp", true, "The outputs to write jsp file."));
        options.addOption(new Option("os", "output-js", true, "The outputs to write js file."));
        options.addOption(new Option("eus", "emit-use-strict", true, "Emit use strict"));
        CommandLineParser cmdLinePosixParser = new PosixParser();
        CommandLine cmd = cmdLinePosixParser.parse(options, args);
        String workFolder = cmd.getOptionValue("work-folder");
        String rootJs = cmd.getOptionValue("root-js");
        List<String> paths = Arrays.asList(cmd.getOptionValues("path"));
        List<String> inputs = Arrays.asList(cmd.getOptionValues("input"));
        List<String> templates = Arrays.asList(cmd.getOptionValues("template"));
        List<String> outputJsps = Arrays.asList(cmd.getOptionValues("output-jsp"));
        List<String> outputJs = Arrays.asList(cmd.getOptionValues("output-js"));

        compile(workFolder, rootJs, paths, inputs, templates, outputJsps, outputJs, Boolean.parseBoolean(cmd.getOptionValue("emit-use-strict")));
    }
}
