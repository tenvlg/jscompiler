package ru.tenvlg.utils;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class ParsedFile {
    private String filename;
    private ImmutableList<String> requires;
    private ImmutableList<String> provides;

    public ParsedFile(String filename, List<String> requires, List<String> provides) {
        this.filename = filename;
        this.requires = ImmutableList.copyOf(requires);
        this.provides = ImmutableList.copyOf(provides);
    }

    public String getFilename() {
        return this.filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public ImmutableList<String> getRequires() {
        return this.requires;
    }

    public void setRequires(ImmutableList<String> requires) {
        this.requires = requires;
    }

    public ImmutableList<String> getProvides() {
        return this.provides;
    }

    public void setProvides(ImmutableList<String> provides) {
        this.provides = provides;
    }
}
