package ru.krlvm.powertunnel.exceptions;

/**
 * Better handling of UnknownHostException
 * on server startup
 */
public class PTUnknownHostException extends Exception {

    private final String host;
    private final Type type;

    public PTUnknownHostException(String host, Type type, Throwable cause) {
        super(cause);
        this.host = host;
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        SERVER_IP("Server IP"),
        DNS("DNS");

        final String display;

        Type(String display) {
            this.display = display;
        }

        @Override
        public String toString() {
            return display;
        }
    }
}
