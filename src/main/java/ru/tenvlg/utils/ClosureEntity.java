package ru.tenvlg.utils;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

public class ClosureEntity {
    private String inputFile;
    private List<String> depFiles;

    public ClosureEntity(String inputFile) {
        this.inputFile = inputFile;
        this.depFiles = new ArrayList();
    }

    public String getInputFile() {
        return this.inputFile;
    }

    public void setInputFile(String inputFile) {
        this.inputFile = inputFile;
    }

    public List<String> getDepFiles() {
        return ImmutableList.copyOf(this.depFiles);
    }

    public boolean addDepFile(String depFile) {
        return this.depFiles.add(depFile);
    }
}