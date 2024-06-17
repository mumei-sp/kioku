package commands;

import java.lang.reflect.Method;

public class CommandEvaluator {

    private static final String COMMANDS_CLASS_NAME = "commands.Commands";

    /**
     * Evaluates a command by name with the given arguments.
     *
     * @param commandName The name of the command to evaluate.
     * @param args The arguments to pass to the command's respond method.
     * @return The result from the command's respond method.
     * @throws Exception If the command class cannot be found or instantiated, or if there are issues invoking the respond method.
     */
    public static <T> T evaluate(String commandName, String[] args) throws Exception {
        try {
            // Load the command class dynamically
            Class<?> commandClass = Class.forName(COMMANDS_CLASS_NAME + "$" + commandName.toUpperCase());
            // Ensure the class implements Command interface
            if (!Command.class.isAssignableFrom(commandClass)) {
                throw new IllegalArgumentException("Class " + commandClass.getName() + " does not implement Command interface.");
            }
            // Create an instance of the command class
            Command<T> commandInstance = (Command<T>) commandClass.getDeclaredConstructor().newInstance();
            // Call the respond method with the provided arguments
            Method respondMethod = commandClass.getMethod("respond", String[].class);
            return (T) respondMethod.invoke(commandInstance, (Object) args);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Command class " + commandName + " not found.", e);
        } catch (Exception e) {
            throw new Exception("Error evaluating command " + commandName, e);
        }
    }
}

