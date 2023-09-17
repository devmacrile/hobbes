package com.hobbes.util;

public class StringLineBuilder {

    private static final String NewLine = "\n";
    private StringBuilder sb;

    public StringLineBuilder() {
        this.sb = new StringBuilder();
    }

    public StringLineBuilder append(String s) {
        sb.append(s);
        return this;
    }

    public StringLineBuilder appendLine(String s) {
        sb.append(s).append(NewLine);
        return this;
    }

    public StringLineBuilder appendLine(String s, String prefix) {
        sb.append(prefix).append(s).append(NewLine);
        return this;
    }

    public String toString() {
        return sb.toString();
    }
}
