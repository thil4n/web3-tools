# Ballerina Web3 Tool

[![Build](https://github.com/thil4n/web3-tools/actions/workflows/build-timestamped-master.yml/badge.svg)](https://github.com/thil4n/web3-tools/actions/workflows/build-timestamped-master.yml)
[![codecov](https://codecov.io/gh/thil4n/web3-tools/branch/master/graph/badge.svg)](https://codecov.io/gh/thil4n/web3-tools)
[![GitHub Last Commit](https://img.shields.io/github/last-commit/thil4n/web3-tools.svg)](https://github.com/thil4n/web3-tools/commits/master)
[![GitHub issues](https://img.shields.io/github/issues/ballerina-platform/ballerina-standard-library/module/web3-tools.svg?label=Open%20Issues)](https://github.com/ballerina-platform/ballerina-library/labels/module%2Fweb3-tools)

The Ballerina-Web3-Tool is a development toolkit designed to simplify the integration of Web3 functionalities into Ballerina applications.
It provides a seamless experience for developers to interact with Ethereum-based blockchains, enabling smart contract interactions, account management, and transaction processing.

## Installation

Execute the command below to pull the Web3 tool from [Ballerina Central](https://central.ballerina.io/ballerina/web3/latest).

```bash
bal tool pull web3
```

## Usage

The Web3 tool provides the following capabilities.

1. Generate Ballerina client for interacting with basic Blockchain features.
2. Generate Ballerina connector for specific Smart contract.

The client generated from a smart contract can be used to call the methods defined in the solidity smart contract.

The following command will generate Ballerina client for the given smart contract.

```bash
bal web3 -a <abi-file-path> -o <output-file-path>
```

### Command options

| Option            | Description                                                                                    | Mandatory/Optional |
| ----------------- | ---------------------------------------------------------------------------------------------- | ------------------ |
| `<abi-file-path>` | The path of the Solidity ABI file.                                                             | Mandatory          |
| `<output-path>`   | The path of the output directory. If not provided, the current working directory will be used. | Optional           |

For example,

```bash
bal web3 -a Token.json
```

Upon successful execution, the following files will be created inside the default module in the Ballerina project.

```bash
client.bal (There can be multiple client files depending on the Web3 file)
utils.bal
```

## Building from the Source

### Setting Up the Prerequisites

1. OpenJDK 21 ([Adopt OpenJDK](https://adoptopenjdk.net/) or any other OpenJDK distribution)

   > **Info:** You can also use [Oracle JDK](https://www.oracle.com/java/technologies/javase-downloads.html). Set the JAVA_HOME environment variable to the pathname of the directory into which you installed JDK.

2. Export GitHub Personal access token with read package permissions as follows,
   ```
   export packageUser=<Username>
   export packagePAT=<Personal access token>
   ```

### Building the Source

Execute the commands below to build from the source.

1.  To build the library:

        ./gradlew clean build

2.  To run the integration tests:

        ./gradlew clean test

3.  To build the module without the tests:

        ./gradlew clean build -x test

4.  To publish to maven local:

        ./gradlew clean build publishToMavenLocal

5.  Publish the generated artifacts to the local Ballerina central repository:

        ./gradlew clean build -PpublishToLocalCentral=true

6.  Publish the generated artifacts to the Ballerina central repository:

        ./gradlew clean build -PpublishToCentral=true

## Contributing to Ballerina

As an open-source project, Ballerina welcomes contributions from the community.

You can also check for [open issues](https://github.com/thil4n/web3-tools/issues) that
interest you. We look forward to receiving your contributions.

For more information, go to the [contribution guidelines](https://github.com/ballerina-platform/ballerina-lang/blob/master/CONTRIBUTING.md).

## Code of Conduct

All contributors are encouraged to read the [Ballerina Code of Conduct](https://ballerina.io/code-of-conduct).

## Useful Links

- Chat live with us via our [Discord server](https://discord.gg/ballerinalang).
- Post all technical questions on Stack Overflow with the [#ballerina](https://stackoverflow.com/questions/tagged/ballerina) tag.
- View the [Ballerina performance test results](https://github.com/ballerina-platform/ballerina-lang/blob/master/performance/benchmarks/summary.md).
