package borMi.classifier;

import java.util.ArrayList;

public class Partition {
    public enum State {
        nonseparable, mixed, singular, unkown
    }

    State state;
    String predicate;

    public Partition(String pr) {
        setUnkown();
        predicate = pr;
    }

    public String getPredicate() {
        return predicate;
    }

    public void setNonseparable() {
        state = State.nonseparable;
    }

    public void setMixed() {
        state = State.mixed;
    }

    public void setSingular() {
        state = State.singular;
    }

    public void setUnkown() {
        state = State.unkown;
    }

    public boolean isNonseparable() {
        return state == State.nonseparable;
    }

    public boolean isMixed() {
        return state == State.mixed;
    }

    public boolean isSingular() {
        return state == State.singular;
    }

    public boolean isUnkown() {
        return state == State.unkown;
    }

}
