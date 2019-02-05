# JPaxos-Benchmark
Infrastructure for benchmarking [JPaxos](https://github.com/JPaxos/JPaxos) with the goal of mesuring 
the impact of the new leader election algorithm implemented in [my fork of JPaxos](https://github.com/vincenzobaz/JPaxos).
More details in the [paper].

## Building
The wrappers for JPaxos client and replica as well as the PuppetMaster can be built using [sbt](https://www.scala-sbt.org/) (>= 1.0). 
The scripts to run the experiments require a bash shell and [curl](https://curl.haxx.se/). 
Refer to `simple_experiment.sh` for an example of experiment. Start PuppetMaster with `sbt "PuppetMaster / run"` 
before starting an experiment.
