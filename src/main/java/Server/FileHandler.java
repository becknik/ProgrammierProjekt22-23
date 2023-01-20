package Server;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class FileHandler implements Handler {
        public void handle(Request request, Response response) {

            try (FileInputStream file = new FileInputStream(request.getPath().substring(1)))
            {
                response.setResponseCode(200, "OK");
                response.addHeader("Content-Type", "text/html");

                int estimatedBytesToRead;
                int offset = 0;
                StringBuilder responseBuilder = new StringBuilder();
                /*
                do {
                    estimatedBytesToRead = file.available();
                    offset += estimatedBytesToRead;

                    byte[] responseFragment = new byte[estimatedBytesToRead];
                    file.read(responseFragment, offset, estimatedBytesToRead);
                    responseBuilder.append(responseFragment);
                } while (estimatedBytesToRead != 0);
                */
                // TODO this is slow
                StringBuffer buf = new StringBuffer();
                int c;
                while ((c = file.read()) != -1) {
                    buf.append((char) c);
                }

                response.addBody(buf.toString());
            } catch (FileNotFoundException e) {
                System.err.println("Failed to resolve file location of request " + request.getPath().substring(1));
                response.setResponseCode(404, "Not Found");
            } catch (IOException e) {
                System.err.println("Failed to creat new InputStream from File" + request.getPath().substring(1));
                e.printStackTrace();
            }
        }
    }

