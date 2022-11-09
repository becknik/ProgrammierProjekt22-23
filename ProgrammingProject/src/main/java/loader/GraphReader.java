package ProgrammingProject.src.main.java.loader;

import java.io.*;
import java.nio.file.Path;

public class GraphReader {

    public static void main(String[] args) {

        GraphReader.read(new File("stgtregbz.fmi"));
    }

    public static void read(final File file) {

        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(file)) {

            GraphReader.prepareBuffer(bufferedReader);

        } catch (FileNotFoundException e) {
            System.err.println("Reader could not find graph file location of " + file.toString());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IO exception for accessing graph file" + file.toString());
            e.printStackTrace();
        }
    }


    private static void prepareBuffer(final BufferedReader bufferedReader) throws IOException {
        String buffer;

        while (( buffer = bufferedReader.readLine() ) != null) {
            String[] parsedBuffer = buffer.trim().split(" ");

            System.out.println(parsedBuffer);

            if (parsedBuffer[4] == "0") {   // TODO this may cause problems
                GraphReader.parseNodes(parsedBuffer);
            } else if (Integer.parseInt(parsedBuffer[4]) > 0) {
                GraphReader.parseEdges(parsedBuffer);
            }
        }
    }

    private static  void parseNodes(String[] buffer) {

    }

    private static void parseEdges(String[] buffer){

    }

}
