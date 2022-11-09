package ProgrammingProject.src.main.java.loader;

import java.io.*;
import java.util.Optional;
import java.util.logging.Logger;

public class GraphReader {
    private static final Logger logger = Logger.getLogger(GraphReader.class.getName());
    public static boolean enableLogging;


    private enum LineType {
        NODE,
        EDGE
    }

    private record ParsedLine(LineType type, Number[] values) {}

    public static void main(String[] args) {
        //enableLogging = true;
        GraphReader.read(new File("stgtregbz.fmi"));
    }

    /**
     * Does the reading of the raw graph file contents into a set of arrays
     * @param file - The file of the raw graph contents
     */
    public static void read(final File file) {


        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {

            String line;
            while (( line = bufferedReader.readLine() ) != null) {

                Optional<ParsedLine> optionalParsedLine = GraphReader.prepareBuffer(line);

                ParsedLine parsedLine;
                if (optionalParsedLine.isEmpty()) continue;
                else parsedLine = optionalParsedLine.get();

                if (parsedLine.type == LineType.NODE) {
                    logParsedLine(parsedLine);
                    GraphReader.parseNode(parsedLine.values);
                } else if (parsedLine.type == LineType.EDGE) {
                    GraphReader.parseEdge(parsedLine.values);
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


    /**
     * Extracts the input Strings of the raw graph files and parses them to data types. Also figures somehow out which
     * kind of line {@code LineType} the corresponding line is and wrapps all information in a {@code ParsedLine} object
     * @param line - The string to be parsed and typed
     * @return - The {@code ParsedLine} record object
     */
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
                return Optional.of(new ParsedLine(LineType.EDGE, parsedNumbers));
            }

            return Optional.empty();
    }

    /**
     * Outputs the formatted contents of the {@code ParsedLine} into the info log, when the {@code enableLogging}
     * variable is set.
     * @param parsedLine - The contents to be formatted & logged
     */
    private static void logParsedLine(final ParsedLine parsedLine) {
        if (GraphReader.enableLogging) {
            StringBuilder logBuffer = new StringBuilder(String.format("Adding %s with following values to array:\t",
                    parsedLine.type.name()));

            for (int i = 0; i < parsedLine.values.length; i++) {
                logBuffer.append(parsedLine.values[i]);
                if (i < parsedLine.values.length - 1) logBuffer.append(",");
            }

            logger.info(logBuffer.toString());
        }
    }
    private static  void parseNode(Number[] buffer) {

    }

    private static void parseEdge(Number[] buffer){

    }

}
