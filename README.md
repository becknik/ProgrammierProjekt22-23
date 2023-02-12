# ProPro

This is the repository for our (Jannik Becker, Tim Exeler, Silas Klein) programming project ProPro in the WS22/23.

This lecture/project is held by Prof. Stefan Funke & Felix Weitbrecht.

---

## Introduction

Our website features executing a point to point & point to all points dijkstra shortest path algorithm & laying the result onto the map using GEOJson.
The one to all website mode covers all extrem cases we could think of.
The website communicates with the backend server also included in this project with plain AJAX requests using the [org.json](https://stleary.github.io/JSON-java/index.html) backend library.

We tried to find the fastest way of implementing the algorithm (in Java!) & we think we're close to the top.
Our one to all implementation executes in about 4 seconds on a relatively new AMD Ryzen Laptop CUP.
To get to these result, we tried to encapsulate multiple primitive datatypes into larger ones to avoid having to many allocated arrays.
For primitiv type optimized datastructures we made use of the [fastutil](https://fastutil.di.unimi.it/docs/index.html) library with some custom extension.

However, this "tuple-ing" sometimes sadly causes the opposite of making the algorithm faster due to the coldness of the CPU cache (as we think) & weird other stuff, so we wen through a really exhaustive testing & trying out phase...

## Website Preview

![A preview of our websites](./preview-pictures/preview.png)
See: [gallery](GALLERY.md)

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
