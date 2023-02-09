# ProPro

This is the repository for our (Jannik Becker, Tim Exeler, Silas Klein) programming project ProPro in the WS22/23.

This lecture/project is held by Prof. Stefan Funke & Felix Weitbrecht.

---

We want to make sure that neither of us is a frontend dev or want's to become one.
Therefore, as you can see, your website design is kind of functional.
The use of JavaScript nearly drove us to insanity & I'd be glad if I can leave my fingers off this piece of garbage language from now on. :^)

![A preview of our websites](./preview-pictures/priview.png)
![What happens when Dijkstrulating & Server not being set up:](./preview-pictures/preview-server-status-message.png)
![Unlimited POWER!!! mode preview](./preview-pictures/preview-star-wars-mode.png)

## Copyright statement

We therefore state that we are not the creators of the `Benchmark.java` file in the `src/main/java/execution` package.
It was initially provided to us by our tutor as template and was finally used for testing of our Dijkstra algorithm.
Therefore, the copyright of this work is restricted.

*Please consider this if you might want to use our code.*

## Getting Started

### Build

```
Dependencies:
- gradle >= 7.3
- jdk >= 17
```
- Navigate to the projects root & run `gradle build`

### Run

> It seems like the execution of this project requires a decent amount of RAM due to the massive array structures being created.
> 
> If this occurs on your machine, please create a `gradle.properties` file in the projects root and add the argument `org.gradle.jvmargs=-Xmx4096m`.

#### Server

- Run `gradle runDijkstraServer --args="<...>"` from a terminal of your liking. The `<...>` should be replaced by something similar to the following (including the `'`s):

```shell
'<path/to/file>/germany.fmi'
```

Explanation:

- `germany.fmi`: A plain text file holding coordinated nodes, relations between them & some further (by this implementation mostly ignored) data
  - For more intel see [Institut für formale Methoden der Informatik](https://fmi.uni-stuttgart.de/alg/research/stuff/)

#### Benchmark

- Run `gradle runTestBenchmark --args="<...>"`, whereas `<...>` should to be replaced with arguments as the following:
```shell
-1 '<path/to/file>/germany.fmi' -1 0.0 -1 0.0 -1 '</path/to/que-files>/germany.que' -1 42
```

Explanation:

- `-1`s: *Can be ignored* - values are probably place-holders for grading
- `germany.fmi`: See Explanation Section above
- `0.0`: Represents a coordinates (long, lat) to which the closest node should be found of
- `germany.que`: A file containing source and target nodes ID the oneToOne Dijkstra will be executed on ([source](https://fmi.uni-stuttgart.de/files/alg/data/graphs/Benchs.tar.bz2))
- `42`:  A start node ID the oneToAll Dijkstra algorithm will be executed on

#### Unit Tests

Because we faced some troubles setting up gradle properly, there is no way to run test from the command line right now. This is due me being tired of doing stuff for this project & my teammates being to lazy/ less motivated to do so...
