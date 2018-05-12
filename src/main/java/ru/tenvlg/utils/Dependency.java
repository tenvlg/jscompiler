package ru.tenvlg.utils;

import java.util.Collection;

public class Dependency {
    private String filename;
    private String[] requires;
    private String[] provides;

    public Dependency(String filename, Collection<String> requires, Collection<String> provides) {
        this.filename = filename;
        this.requires = requires.toArray(new String[requires.size()]);
        this.provides = provides.toArray(new String[provides.size()]);
    }

    public String getFilename() {
        return this.filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String[] getRequires() {
        return this.requires;
    }

    public void setRequires(String[] requires) {
        this.requires = requires;
    }

    public String[] getProvides() {
        return this.provides;
    }

    public void setProvides(String[] provides) {
        this.provides = provides;
    }
}
