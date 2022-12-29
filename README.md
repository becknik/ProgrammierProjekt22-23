# ProPro

This is the repository for our (Jannik Becker, Tim Exeler, Silas Klein) programming project ProPro in the WS22/23.

This lecture/project is held by Prof. Stefan Funke & Felix Weitbrecht.

## Copyright statement

We therefore state that we are not the creators of the `Benchmark.java` file in the `./src/main/java/execution` package.
It was initially provided to us by our tutor as template and was finally used for testing of our Dijkstra algorithm.
Therefore, the copyright of this work is restricted.

*Please consider this if you might want to use our code.*

## Build & Run

```
Dependencies:
- gradle >= 7.3
- jdk >= 17
```

- Clone repo & navigate to this repos root
- Execute `gradle build`
- Run `gradle run --args="..."` with ... being replaced by the following formatting:

### Examples for `args` String for `Benchmark.java`

- On a Linux machine: `"0 '<path/to/file>/germany.fmi' 0 0.0 0 0.0 0 '</path/to/que-files>/germany.que' 0 42"`
- *Windows*: Should be the same, just with Windows `\` path separators

Explanation of formatting:

- The `0.0` values represent coordinates (first value representing the longitude, second latitude) to which the closest node should be found
- The `.que` file contains source and target nodes ID the oneToOne Dijkstra will be executed on
- The value 42 ist the oneToAll Dijkstra algorithm source node
- The `0` values are probably place-holders for grading

## Run Test

- TODO: Because we faced some troubles setting up gradle properly, there is no way to run test from from the command line right now...
