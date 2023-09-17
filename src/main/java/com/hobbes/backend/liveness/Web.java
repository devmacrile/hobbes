package com.hobbes.backend.liveness;

import com.hobbes.ir.IRVariable;
import com.hobbes.symbol.TypeSymbol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Web {

    private IRVariable variable;
    private List<LiveRange> liveRanges;

    public Web(IRVariable variable) {
        this.variable = variable;
        this.liveRanges = new ArrayList<>();
    }
    public Web(IRVariable variable, LiveRange liveRange) {
        this.variable = variable;
        this.liveRanges = Arrays.asList(liveRange);
    }

    public IRVariable getVariable() {
        return this.variable;
    }

    public void addLiveRange(LiveRange liveRange) {
        this.liveRanges.add(liveRange);
    }

    public List<LiveRange> getLiveRanges() {
        return liveRanges;
    }

    public TypeSymbol getType() {
        return variable.getType().getBaseType();
    }

    public boolean interferes(Web other) {
        if (!getType().equals(other.getType()))
            return false;
        for (LiveRange mine : liveRanges) {
            for (LiveRange theirs : other.getLiveRanges()) {
                if (mine.interferes(theirs))
                    return true;
            }
        }
        return false;
    }

    public int getSpillCost() {
        // note: we really need loop depth here for this to be accurate!
        // use total live range as a proxy for now
        return liveRanges.stream().map(LiveRange::length).reduce(0, Integer::sum);
    }
}
