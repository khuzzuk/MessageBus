package pl.khuzzuk.messaging;

public interface Action {
    Action EMPTY_ACTION = () -> {/*empty action*/};
    void resolve();
}
