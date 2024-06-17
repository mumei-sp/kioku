package commands;

import connection.RESPParser;
import db.InMemoryStore;

public class Commands {

    public static class PING implements Command<String> {
        private static final String NAME = "PING";
        private static final String SUMMARY = "Returns the server's liveliness response.";

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public String getSummary() {
            return SUMMARY;
        }

        @Override
        public String respond(String[] args) {
            if (args.length == 1) {
                return "PONG";
            }
            return args[1];
        }
    }

    public static class SET implements Command<String> {
        private static final String NAME = "SET";
        private static final String SUMMARY = "Sets the string value of a key, ignoring its type. The key is created if it doesn't exist.";

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public String getSummary() {
            return SUMMARY;
        }

        @Override
        public String respond(String[] args) {
            if (args.length < 3) {
                throw new IllegalArgumentException("Insufficient arguments. Expected at least 3 arguments: command, key, value.");
            }

            String key = args[1];
            Object value = args[2];
            Long expirationTime = Long.MAX_VALUE;

            try {
                for (int i = 3; i < args.length; i++) {
                    switch (args[i].toLowerCase()) {
                        case "ex":
                            if (i + 1 < args.length) {
                                try {
                                    expirationTime = Long.parseLong(args[i + 1]) * 1000; // Convert seconds to milliseconds
                                    i++; // Skip the next argument as it has been processed
                                } catch (NumberFormatException e) {
                                    throw new IllegalArgumentException("Invalid expiration time format. Expected a number.");
                                }
                            } else {
                                throw new IllegalArgumentException("Missing expiration time after 'ex'.");
                            }
                            break;
                        default:
                            throw new IllegalArgumentException("Unknown argument: " + args[i]);
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                throw new IllegalArgumentException("Insufficient arguments for the specified options.", e);
            }

            InMemoryStore.put(key, value, expirationTime);
            return RESPParser.OKAY;
        }
    }

    public static class GET implements Command<Object> {
        private static final String NAME = "GET";
        private static final String SUMMARY = "Returns the string value of a key.";

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public String getSummary() {
            return SUMMARY;
        }

        @Override
        public Object respond(String[] args) {
            if (args.length < 2) {
                throw new IllegalArgumentException("Insufficient arguments. Expected at least 2 arguments: command, key.");
            }

            String key = args[1];
            Object value = InMemoryStore.get(key);

            if (value == null) {
                return RESPParser.NULL_VALUE;
            }

            return value;
        }
    }

    public static class DEL implements Command<Object> {
        private static final String NAME = "DEL";
        private static final String SUMMARY = "Deletes one or more keys.";

        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public String getSummary() {
            return SUMMARY;
        }

        @Override
        public Object respond(String[] args) {
            int deletedKeys = 0;
            boolean isFailed = false;
            for(int i=1; i< args.length; ++i) {
                if(InMemoryStore.remove(args[1])) deletedKeys++;
                else isFailed = true;
            }
            if(isFailed) return deletedKeys;
            return true;

        }
    }


}
