package com.hobbes.backend.liveness;

public class LiveRange {

    int start;
    int end;

    public LiveRange(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int length() {
        return end - start + 1;
    }

    public boolean interferes(LiveRange other) {
        return start <= other.getEnd() && end >= other.getStart();
    }
}
