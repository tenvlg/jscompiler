package ru.tenvlg.utils;

import java.util.ArrayList;
import java.util.List;

public class ClojureEntity {
    private String inputFile;
    private List<String> depFiles;

    public ClojureEntity(String inputFile) {
        this.inputFile = inputFile;
        this.depFiles = new ArrayList();
    }

    public String getInputFile() {
        return this.inputFile;
    }

    public void setInputFile(String inputFile) {
        this.inputFile = inputFile;
    }

    public String[] getDepFiles() {
        return this.depFiles.toArray(new String[this.depFiles.size()]);
    }

    public boolean addDepFile(String depFile) {
        return this.depFiles.add(depFile);
    }
}