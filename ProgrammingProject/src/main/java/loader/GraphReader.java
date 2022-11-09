package ProgrammingProject.src.main.java.loader;

import java.io.*;
import java.util.Optional;

public class GraphReader {


    private enum LineType {
        NODE,
        EDGE
    }

    private record ParsedLine(LineType type, Number[] values) {

    }

    public static void main(String[] args) {
        GraphReader.read(new File("stgtregbz.fmi"));
    }

    public static void read(final File file) {


        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {

            String line;
            while (( line = bufferedReader.readLine() ) != null) {

                Optional<ParsedLine> optionalParsedLine = GraphReader.prepareBuffer(line);
                if (optionalParsedLine.isEmpty()) continue;

                if (optionalParsedLine.get().type == LineType.NODE) {
                    System.out.print("Adding Node to array: ");
                    for (Number number : optionalParsedLine.get().values) {
                        System.out.print(number + ", ");
                    }
                    System.out.println();
                    GraphReader.parseNode(optionalParsedLine.get().values);
                } else if (optionalParsedLine.get().type == LineType.EDGE) {
                    GraphReader.parseEdge(optionalParsedLine.get().values);
                } else continue;
            }

        } catch (FileNotFoundException e) {
            System.err.println("Reader could not find graph file location of " + file);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IO exception for accessing graph file" + file);
            e.printStackTrace();
        }
    }


    private static Optional<ParsedLine> prepareBuffer(final String line) {

            String[] rawValues = line.trim().split(" "); // TODO: Maybe use RegEx here?

            if (rawValues.length < 5) return Optional.empty();
            else if (rawValues[4].equals("0")) {   // TODO this may cause problems
                int nodeID = Integer.parseInt(rawValues[0]);    // Maybe chance this to long?
                long idk = Long.parseLong(rawValues[1]);
                double  longitude = Double.parseDouble(rawValues[2]);
                double  latitude = Double.parseDouble(rawValues[3]);

                Number[] parsedNumbers = {nodeID, idk, longitude, latitude};
                return Optional.of(new ParsedLine(LineType.NODE, parsedNumbers));

            } else if (Integer.parseInt(rawValues[4]) > 0) {
                int startNode = Integer.parseInt(rawValues[0]);
                int targetNode = Integer.parseInt(rawValues[0]);
                int distance = Integer.parseInt(rawValues[0]);

                Number[] parsedNumbers = {startNode, targetNode, distance};
                return Optional.of(new ParsedLine(LineType.NODE, parsedNumbers));
            }

            return Optional.empty();
    }

    private static  void parseNode(Number[] buffer) {

    }

    private static void parseEdge(Number[] buffer){

    }

}
