# File Processing Benchmark (Java 21)

![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=psteixeira23_file-processing-benchmark&metric=alert_status)
![Coverage](https://sonarcloud.io/api/project_badges/measure?project=psteixeira23_file-processing-benchmark&metric=coverage)
![Maintainability](https://sonarcloud.io/api/project_badges/measure?project=psteixeira23_file-processing-benchmark&metric=sqale_rating)
![Reliability](https://sonarcloud.io/api/project_badges/measure?project=psteixeira23_file-processing-benchmark&metric=reliability_rating)

## Overview

This project benchmarks multiple strategies for reading and processing large CSV files
in Java 21. It compares execution time, memory usage, scalability, and code clarity
across different I/O approaches while running the same scenarios on each strategy.

## Executive Summary (Key Learnings)

- MemoryMapped leads most single-pass scenarios on the baseline dataset.
- On the scaled dataset, NIO ByteBuffer and MemoryMapped split the lead in single-pass results.
- In isolated mode, BufferedReader and Files.lines generally outperform NIO and MemoryMapped.
- Single-pass timing reflects realistic throughput, while isolated timing highlights per-scenario cost.

## Why this benchmark

This project compares different strategies for processing large files in Java and
measures their impact on execution time, memory usage, and scalability. The goal is
to make trade-offs explicit so teams can choose the right approach for their system.

**What is being compared**

- The same CSV processing scenarios executed with different file-reading strategies.
- Two execution modes (single-pass vs. isolated) to understand realistic vs. analytical timing.

**Why these approaches**

- **BufferedReader**: baseline, widely used, easy to read and maintain.
- **Files.lines**: stream-based API, concise and readable, higher abstraction cost.
- **NIO ByteBuffer**: explicit buffering control, lower-level optimization opportunities.
- **MemoryMapped**: OS-level mapping, useful for very large files and sequential scans.

**Where each fits best**

| Strategy | Best fit |
| --- | --- |
| BufferedReader | Simple pipelines, moderate file sizes, clarity-first systems. |
| Files.lines | Stream-friendly codebases, functional pipelines, quick implementations. |
| NIO ByteBuffer | High-throughput systems where decoding control matters. |
| MemoryMapped | Very large files, repeated sequential scans, memory-mapped batch jobs. |

## When this analysis is useful

- Financial statement processing (large CSV or fixed-width files).
- High-volume CSV imports (ETL and data migration).
- Nightly batch pipelines and reporting jobs.
- Integrations with legacy systems that export large flat files.

## Why this matters in practice

This project demonstrates disciplined performance evaluation: it measures before
making claims, documents trade-offs, and ties results to real operational contexts.
That is the same mindset required when diagnosing production bottlenecks and making
architecture decisions in data-heavy systems.

## Features

- Four file-reading strategies (BufferedReader, Files.lines, NIO ByteBuffer, MemoryMapped).
- Two execution modes (single-pass and isolated) for realistic and analytical comparisons.
- Scenario-based processing with per-scenario timings and counts.
- Console report plus an HTML report for easier inspection.
- Modular architecture with interfaces and isolated strategy implementations.
- Unit tests with high coverage.

## Requirements

- Java 21
- Maven 3.9+

## Project Layout

```
src/main/java/com/patrick/benchmark/
  App.java
  BenchmarkRunner.java
  BenchmarkReport.java
  BenchmarkResult.java
  ProcessingMode.java
  metrics/
  processing/
  processing/scenario/
  readers/
  reporting/
src/main/resources/
  benchmark-input.csv
  benchmark-input-large.csv
src/test/resources/
  benchmark-input-test.csv
reports/
  benchmark-report.html
```

## Input Data

The CSV format expects a header and six columns:

```
uf,tipo_doenca,local_obito,faixa_etaria,sexo,total
```

The header line is skipped during processing.

Included datasets:

- `src/main/resources/benchmark-input.csv`  
  Sample dataset for local runs.
- `src/main/resources/benchmark-input-large.csv`  
  Scaled dataset (20x repeat of the sample data lines) for larger runs.
- `src/test/resources/benchmark-input-test.csv`  
  Small dataset used by unit tests.

You can replace the sample dataset with your own file, as long as it follows
the same format.

## Scenarios

Each strategy is evaluated against the same scenarios:

- Total number of records.
- Records where `local_obito` is not `HOSPITAL` or `DOMICILIO`.
- Counts by `faixa_etaria`.
- Counts by sex (`M`, `F`, and `I` treated as `Other`).
- Counts by `uf`.

## Execution Modes

- **Single-Pass (Realistic)**: One pass computes all scenarios together.  
  Scenario time reflects processing within the same pass.

- **Isolated (Analytical)**: One pass per scenario.  
  Scenario time includes the full pass time for that scenario.

## Running

### 1) Build

```
mvn -q -DskipTests package
```

### 2) Execute the benchmark

```
java -cp target/classes com.patrick.benchmark.App <file> [--mode=single|isolated] [--charset=UTF-8] [--runs=5]
```

Example:

```
java -cp target/classes com.patrick.benchmark.App ./src/main/resources/benchmark-input.csv --mode=isolated --runs=5
```

Runs default to 5 to support mean and standard deviation in the HTML report.
Use `--runs=1` for faster, single-pass execution.

### HTML Report

The HTML report is written to:

```
reports/benchmark-report.html
```

If the file already exists, it is overwritten by the latest run.

## Benchmark Environment

- OS: macOS 26.2 (build 25C56)
- CPU: Apple M3 (8 cores: 4 performance, 4 efficiency)
- Memory: 16 GB
- JVM: OpenJDK Corretto 21.0.6 (64-bit)

All benchmarks were executed on a local machine using the environment above.

## Results (latest runs)

The numbers below come from five runs per dataset using:

- Baseline dataset: `src/main/resources/benchmark-input.csv` (15,781 records)
- Scaled dataset: `src/main/resources/benchmark-input-large.csv` (315,620 records)
- Java 21
- Commands:

```
java -cp target/classes com.patrick.benchmark.App ./src/main/resources/benchmark-input.csv
java -cp target/classes com.patrick.benchmark.App ./src/main/resources/benchmark-input-large.csv
```

Use these results as **relative comparisons**. Absolute times vary with hardware,
storage, and OS scheduling.

All values are `mean +/- standard deviation` in milliseconds (n=5, sample std dev).
Each benchmark was executed five times. The values shown represent the mean and
standard deviation, reducing bias from one-off execution variance.

### Baseline Dataset (15,781 records)

#### Single-Pass (Realistic) — Scenario Time in One Pass (ms)

| Scenario | BufferedReader | Files.lines | NIO ByteBuffer | MemoryMapped |
| --- | --- | --- | --- | --- |
| Total Records | 0.305444 +/- 0.009015 | 0.261809 +/- 0.029374 | 0.243371 +/- 0.013322 | 0.234391 +/- 0.005938 |
| Non Hospital or Home | 1.115208 +/- 0.024838 | 0.909142 +/- 0.094015 | 0.611320 +/- 0.042411 | 0.524947 +/- 0.047431 |
| By Age Range | 2.376719 +/- 0.153061 | 0.799201 +/- 0.088561 | 0.435219 +/- 0.060391 | 0.360834 +/- 0.037579 |
| By Sex | 2.180379 +/- 0.070568 | 1.519630 +/- 0.450260 | 1.160075 +/- 0.302108 | 0.748148 +/- 0.077990 |
| By UF | 2.504944 +/- 0.066400 | 1.073242 +/- 0.116128 | 0.550047 +/- 0.053006 | 0.517665 +/- 0.037711 |

#### Isolated (Analytical) — Full Pass per Scenario (ms)

| Scenario | BufferedReader | Files.lines | NIO ByteBuffer | MemoryMapped |
| --- | --- | --- | --- | --- |
| Total Records | 8.036408 +/- 0.236959 | 4.650333 +/- 0.233147 | 9.558508 +/- 2.188627 | 5.923733 +/- 0.429491 |
| Non Hospital or Home | 8.334733 +/- 7.042704 | 4.559767 +/- 0.451775 | 6.387350 +/- 0.855229 | 5.858225 +/- 0.143454 |
| By Age Range | 5.522833 +/- 0.274619 | 5.058541 +/- 0.849277 | 6.234691 +/- 0.216738 | 6.085242 +/- 0.365182 |
| By Sex | 8.299183 +/- 8.123581 | 5.115050 +/- 0.783804 | 5.999567 +/- 0.153213 | 5.948292 +/- 0.253046 |
| By UF | 4.697058 +/- 0.298177 | 4.346917 +/- 0.193461 | 6.150925 +/- 0.178124 | 6.064425 +/- 0.193368 |

### Scaled Dataset (315,620 records)

#### Single-Pass (Realistic) — Scenario Time in One Pass (ms)

| Scenario | BufferedReader | Files.lines | NIO ByteBuffer | MemoryMapped |
| --- | --- | --- | --- | --- |
| Total Records | 4.418490 +/- 0.039827 | 4.204268 +/- 0.023927 | 4.157467 +/- 0.023158 | 4.173194 +/- 0.034923 |
| Non Hospital or Home | 6.953867 +/- 0.054798 | 4.356784 +/- 0.054713 | 4.291021 +/- 0.059595 | 4.281612 +/- 0.128000 |
| By Age Range | 8.243550 +/- 0.168915 | 5.384511 +/- 0.147678 | 5.237404 +/- 0.236973 | 5.334716 +/- 0.214703 |
| By Sex | 11.640695 +/- 0.423778 | 6.456523 +/- 0.212146 | 6.449301 +/- 0.206654 | 6.736678 +/- 0.552824 |
| By UF | 11.365372 +/- 0.351851 | 7.942197 +/- 0.247066 | 7.914073 +/- 0.274162 | 7.876498 +/- 0.263048 |

#### Isolated (Analytical) — Full Pass per Scenario (ms)

| Scenario | BufferedReader | Files.lines | NIO ByteBuffer | MemoryMapped |
| --- | --- | --- | --- | --- |
| Total Records | 78.424217 +/- 0.835415 | 80.132484 +/- 1.665134 | 125.361625 +/- 2.924273 | 109.530434 +/- 0.582402 |
| Non Hospital or Home | 76.204400 +/- 0.656171 | 76.134150 +/- 1.073030 | 112.499784 +/- 0.447518 | 110.082200 +/- 1.337789 |
| By Age Range | 81.162925 +/- 2.071671 | 79.912283 +/- 1.471946 | 117.199750 +/- 3.717411 | 114.306625 +/- 0.574174 |
| By Sex | 77.361325 +/- 1.560662 | 76.915258 +/- 0.908883 | 167.339317 +/- 119.657928 | 113.054242 +/- 0.544262 |
| By UF | 83.149958 +/- 0.587064 | 83.576642 +/- 1.617972 | 118.296258 +/- 0.753268 | 116.501725 +/- 0.459908 |

### Key takeaways

- On the baseline dataset, **MemoryMapped** led all single-pass scenarios.
- On the scaled dataset, **NIO ByteBuffer** and **MemoryMapped** alternated the lead in single-pass.
- In isolated mode, **BufferedReader** and **Files.lines** generally led, while NIO and MemoryMapped trailed.
- Single-pass results are more representative for end-to-end throughput; isolated results are best for per-scenario profiling.

### Interpreting the numbers

Single-pass results were the most stable across runs, with relatively low standard
deviations. Isolated runs showed higher variance in some scenarios, especially for
NIO ByteBuffer on the scaled dataset and BufferedReader on the baseline dataset.
This suggests that in production, single-pass processing is likely to produce more
predictable throughput, while isolated passes are better suited for targeted analysis.

## Output

The console and HTML reports provide:

- Total execution time per strategy.
- Memory delta in MB (approximate).
- Records processed and invalid lines.
- Scenario timings and counts.
- Error counts and messages when failures occur.

## Testing

```
mvn test
```

The project enforces a minimum coverage threshold via JaCoCo.

## Notes

- Memory usage is approximate and based on a before/after snapshot.
- The CSV header is always ignored.
- Charset defaults to UTF-8; use `--charset=` to override.

## Limitations of the Experiment

- Results are from a local environment and may not generalize to other machines.
- No network I/O is involved; storage and OS cache effects are not isolated.
- Scenarios run sequentially without concurrency or parallel processing.
- Results are based on a single run; repeated runs and statistical aggregation were not applied.

## License

MIT. See `LICENSE`.
