## PageRank in Pregel's Bulk Synchronous Parallel model

Implementation of PageRank under Pregel's Bulk Synchronous Parallel (BSP) model. Each vertex is deployed on a separate thread and the execution is synchronized by using wait and notify. 

## Build

Compile and test
```
./gradlew build
```

Compile only
```
./gradlew assemble
```

Test
```
./gradlew test
```

## Input/Output Format

```
new PageRank(5, {1,2,3}, {0,0,0})
```
means the graph has five vertices, `0, 1, 2, 3, 4`, and three directed edges, `1->0, 2->0, 3->0`.

This project was partly developed as a requirement for the "Systems for Data Science" course(590S) at University of Massachusetts. The interface code was provided by the [PLASMA lab](https://github.com/plasma-umass).

