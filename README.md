# temp

[![CI/CD](https://github.com/Emut/temp/actions/workflows/maven.yml/badge.svg?branch=feature%2Fesg-engine-dependency-removal)](https://github.com/Emut/temp/actions/workflows/maven.yml)
![Coverage](.github/badges/jacoco.svg)
![Coverage](.github/badges/branches.svg)

## Usage

Compile and package with `mvn package`

Run the tool with:

```bash
java -jar esg-coverage-measurer-1.0-SNAPSHOT-jar-with-dependencies.jar
```

```
Usage: CoverageMeasurer [-hV] [--ts-filter=<testSequenceFilter>]
                        [--ts-matcher=<testSequenceMatcher>]
                        [--test-sequence=<testSequenceFiles>]...
                        [--tuple-length=<tupleLengths>]... <esgInputPath>
Measure coverage of test sequences over ESGs.
      <esgInputPath>   Path to esg inputs.
  -h, --help           Show this help message and exit.
  -V, --version        Print version information and exit.
      --test-sequence=<testSequenceFiles>
                       Path to test sequence file. Can add multiple options.
      --ts-filter=<testSequenceFilter>
                       Filter to be used on test sequences.
      --ts-matcher=<testSequenceMatcher>
                       Matcher to be used on test sequences.
      --tuple-length=<tupleLengths>
                       Tuple length to measure against. Can add multiple
                         options.
```

Example:
```bash
java -jar target/esg-coverage-measurer-1.0-SNAPSHOT-jar-with-dependencies.jar src/test/resources/ESGs --tuple-length=1 --tuple-length=2 --tuple-length=3 --tuple-length=4 --test-sequence=src/test/resources/TestSequences/503_all_runs.csv --ts-filter=esg-alphabet --ts-matcher=instance-to-class

java -jar target/esg-coverage-measurer-1.0-SNAPSHOT-jar-with-dependencies.jar src/test/resources/ESGs --tuple-length=1 --tuple-length=2 --tuple-length=3 --tuple-length=4 --test-sequence=src/test/resources/proposed_TestSequences/proposed_all_runs.csv --ts-filter=esg-alphabet --ts-matcher=function-call

java -jar target/esg-coverage-measurer-1.0-SNAPSHOT-jar-with-dependencies.jar src/test/resources/ESGs --tuple-length=1 --tuple-length=2 --tuple-length=3 --tuple-length=4 --test-sequence=src/test/resources/proposed_TestSequences/proposed_all_runs.csv --ts-filter=esg-alphabet --ts-matcher=instance-to-class

```
