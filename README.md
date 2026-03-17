# Ballerina Web3 Tool

[![Build](https://github.com/thil4n/web3-tools/actions/workflows/build-timestamped-master.yml/badge.svg)](https://github.com/thil4n/web3-tools/actions/workflows/build-timestamped-master.yml)
[![codecov](https://codecov.io/gh/thil4n/web3-tools/branch/master/graph/badge.svg)](https://codecov.io/gh/thil4n/web3-tools)
[![GitHub Last Commit](https://img.shields.io/github/last-commit/thil4n/web3-tools.svg)](https://github.com/thil4n/web3-tools/commits/master)
[![GitHub issues](https://img.shields.io/github/issues/ballerina-platform/ballerina-standard-library/module/web3-tools.svg?label=Open%20Issues)](https://github.com/ballerina-platform/ballerina-library/labels/module%2Fweb3-tools)

The Ballerina Web3 Tool automatically generates type-safe [Ballerina](https://ballerina.io/) client connectors from Ethereum smart contract ABI (Application Binary Interface) JSON files. Instead of manually writing boilerplate code to interact with smart contracts, point this tool at an ABI file and get a ready-to-use Ballerina client with methods for every contract function, plus built-in Ethereum RPC helpers.

### What you get

- **Smart contract client** — each function in your Solidity contract becomes a Ballerina resource method.
- **Ethereum RPC helpers** — `getAccounts()`, `getBalance()`, `getBlockNumber()`, `getTransactionCount()`, plus Wei ⟷ Ether conversions out of the box.
- **Utility functions** — parameter encoding, hex conversion, and more, generated alongside the client.

## Installation

Pull the tool from [Ballerina Central](https://central.ballerina.io/ballerina/web3/latest):

```bash
bal tool pull web3
```

## Quick Start

Given a `SimpleStorage.json` ABI file:

```bash
bal web3 -a SimpleStorage.json
```

This generates two files in your current directory:

| File | Description |
|------|-------------|
| `main.bal` | A `Web3` client class with contract methods and Ethereum RPC helpers |
| `utils.bal` | Helper functions for parameter encoding and hex conversion |

You can then use the generated client:

```ballerina
import ballerina/io;

public function main() returns error? {
    Web3 client = check new ("http://localhost:8545", "0xYourContractAddress");
    
    // Call a contract method
    decimal value = check client->/retrieve.post();
    io:println("Stored value: ", value);
    
    // Use built-in helpers
    decimal balance = check client.getBalance("0xYourAddress");
    io:println("Balance in ETH: ", client.weiToEther(balance));
}
```

## Usage

```bash
bal web3 -a <abi-file-path> [-o <output-directory>]
```

### Command Options

| Option | Description | Required |
|--------|-------------|----------|
| `-a`, `--abi` | Path to the Solidity ABI JSON file | Yes |
| `-o`, `--output` | Output directory (defaults to current directory) | No |
| `-h`, `--help` | Display help information | No |

### Examples

```bash
# Generate client from a token contract ABI
bal web3 -a Token.json

# Specify an output directory
bal web3 -a Token.json -o ./generated
```

## Generated Client API

### Static Methods (always included)

| Method | Signature | Description |
|--------|-----------|-------------|
| `init` | `init(string api, string address) returns error?` | Initialize with RPC URL and contract address |
| `setContractAddress` | `setContractAddress(string address)` | Change the target contract address |
| `getAccounts` | `getAccounts() returns string[]\|error` | List available accounts |
| `getBalance` | `getBalance(string address) returns decimal\|error` | Get account balance in Wei |
| `getBlockNumber` | `getBlockNumber() returns decimal\|error` | Get the latest block number |
| `getTransactionCount` | `getTransactionCount(string address) returns decimal\|error` | Get transaction count for an address |
| `weiToEther` | `weiToEther(decimal weiAmount) returns decimal` | Convert Wei to Ether |
| `ethToWei` | `ethToWei(decimal etherValue) returns decimal` | Convert Ether to Wei |

### Dynamic Methods (generated from ABI)

Each function defined in the smart contract ABI becomes a Ballerina resource method. For example, a Solidity `store(uint256)` function generates:

```ballerina
resource isolated function post store(decimal _value) returns error? { ... }
```

## Solidity → Ballerina Type Mapping

| Solidity Type | Ballerina Type |
|---------------|----------------|
| `uint8`, `uint256`, `int8`, `int256`, etc. | `decimal` |
| `bool` | `boolean` |
| `address` | `string` |
| `string` | `string` |
| `bytes` | `string` |
| `T[]` (arrays) | `T[]` |
| Multiple return values | `record { ... }` |

## How It Works

```
ABI JSON File
     │
     ▼
┌──────────┐
│ AbiReader │  ── Parses JSON, extracts function signatures
└────┬─────┘
     │
     ▼
┌────────────────┐
│ ClientGenerator │
├────────┬───────┘
│        │
│   ┌────▼──────────────────┐
│   │ StaticFunctionGenerator │  ── Adds standard Ethereum RPC methods
│   └───────────────────────┘
│
│   ┌────▼───────────────────┐
│   │ DynamicFunctionGenerator │  ── Generates contract-specific methods
│   │  • Keccak256 function selectors
│   │  • Parameter encoding
│   │  • Response decoding
│   └────────────────────────┘
│
     ▼
┌───────────┐
│ Formatter  │  ── Formats via Ballerina compiler APIs
└─────┬─────┘
      │
      ▼
  main.bal + utils.bal
```

## Building from Source

### Prerequisites

1. **OpenJDK 21** — [Adoptium](https://adoptium.net/) or [Oracle JDK](https://www.oracle.com/java/technologies/javase-downloads.html)

   Set `JAVA_HOME` to point to your JDK installation.

2. **GitHub credentials** with read package permissions:
   ```bash
   export packageUser=<Username>
   export packagePAT=<Personal access token>
   ```

### Build Commands

```bash
# Build the project
./gradlew clean build

# Run tests
./gradlew clean test

# Build without tests
./gradlew clean build -x test

# Publish to Maven local
./gradlew clean build publishToMavenLocal

# Publish to local Ballerina Central
./gradlew clean build -PpublishToLocalCentral=true

# Publish to Ballerina Central
./gradlew clean build -PpublishToCentral=true
```

### Key Dependencies

| Dependency | Purpose |
|-----------|---------|
| Jackson (`jackson-databind`) | ABI JSON parsing |
| Ballerina Compiler APIs | Syntax tree generation & formatting |
| Picocli | CLI argument parsing |
| BouncyCastle | Keccak256 hashing for function selectors |
| Commons IO | File utilities |

## Known Limitations

- `bytes` types are currently mapped to `string` rather than byte arrays.
- `tuple` types are not yet supported.
- Multiple output decoding is basic — record types are generated but decoding logic is minimal.

## Contributing

As an open-source project, Ballerina welcomes contributions from the community.

Check for [open issues](https://github.com/thil4n/web3-tools/issues) that interest you, or submit a pull request. See the [contribution guidelines](https://github.com/ballerina-platform/ballerina-lang/blob/master/CONTRIBUTING.md) for details.

## Code of Conduct

All contributors are encouraged to read the [Ballerina Code of Conduct](https://ballerina.io/code-of-conduct).

## Useful Links

- [Discord](https://discord.gg/ballerinalang) — Chat with the community
- [Stack Overflow](https://stackoverflow.com/questions/tagged/ballerina) — Ask technical questions (`#ballerina`)
- [Ballerina Performance Benchmarks](https://github.com/ballerina-platform/ballerina-lang/blob/master/performance/benchmarks/summary.md)

## License

This project is licensed under the [Apache License 2.0](LICENSE).
