package commands;

public interface Command<R> {
    String getName();
    String getSummary();
    R respond(String[] args);
}
