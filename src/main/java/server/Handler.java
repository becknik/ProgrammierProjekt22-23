package server;

import java.io.IOException;

/**
 * Handlers must be thread safe.
 */
public interface Handler  {
    public void handle(Request request, Response response) throws IOException;
}
