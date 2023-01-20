package server;

import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.Map;
import java.net.Socket;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;

class SocketHandler implements Runnable {
    private Socket socket;
    private Handler defaultHandler;
    private Map<String, Map<String, Handler>> handlers;

    public SocketHandler(Socket socket,
                         Map<String, Map<String, Handler>> handlers)  {
        this.socket = socket;
        this.handlers = handlers;
    }

    /**
     * Simple responses like errors.  Normal reponses come from handlers.
     */
    private void respond(int statusCode, String msg, OutputStream out) throws IOException  {
        String responseLine = "HTTP/1.1 " + statusCode + " " + msg + "\r\n\r\n";
        log(responseLine);
        out.write(responseLine.getBytes());
    }

    public void run()  {
        BufferedReader in = null;
        OutputStream out = null;

        try  {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = socket.getOutputStream();

            Request request = new Request(in);
            if (!request.parse())  {
                respond(500, "Unable to parse request", out);
                return;
            }

            // TODO most specific handler
            boolean foundHandler = false;
            Response response = new Response(out);
            Map<String, Handler> methodHandlers = handlers.get(request.getMethod());
            if (methodHandlers == null)  {
                respond(405, "Method not supported", out);
                return;
            }

            for (String handlerPath : methodHandlers.keySet())  {
                if (handlerPath.equals(request.getPath()))  {
                    methodHandlers.get(request.getPath()).handle(request, response);
                    response.send();
                    foundHandler = true;
                    break;
                }
            }

            if (!foundHandler)  {
                if (methodHandlers.get("/*") != null)  {
                    methodHandlers.get("/*").handle(request, response);
                    response.send();
                } else  {
                    respond(404, "Not Found", out);
                }
            }
        } catch (IOException e)  {
            try  {
                e.printStackTrace();
                if (out != null)  {
                    respond(500, e.toString(), out);
                }
            } catch (IOException e2)  {
                e2.printStackTrace();
                // We tried
            }
        } finally  {
            try  {
                if (out != null)  {
                    out.close();
                }
                if (in != null)  {
                    in.close();
                }
                socket.close();
            } catch (IOException e)  {
                e.printStackTrace();
            }
        }
    }

    private void log(String msg)  {
        System.out.println(msg);
    }
}

