# `var`: The Good, the Bad and the Ugly

This repository is a complementary resource to my LinkedIn article [Why Your Java `var` Code Fails Code Review](https://www.linkedin.com/pulse/why-your-java-var-code-fails-review-stoyan-topchiyski-xwome). It contains runnable Java 21 examples of where `var` helps, where it hurts, and where it infers a surprising type.

Tag [`v1.0.0`](https://github.com/topchiyski/var-good-bad-ugly/tree/v1.0.0) is the version that was current when the article was published.

## Setup

- JDK 21 or later
- Internet access on the first run to download Maven and dependencies
- On Windows, set `JAVA_HOME` to your JDK installation directory

The Maven Wrapper downloads Maven automatically; a separate Maven installation is not required.

## What's Inside

| File | Shows |
|---|---|
| `main/.../var/Var.java` | `var` is not a reserved keyword, so it is still a legal name |
| `main/.../mechanics/BytecodeParityDemo.java` | `var` compiles to the same bytecode as explicit types |
| `test/.../good/BoilerplateEliminationTest.java` | Removing redundant types |
| `test/.../good/PipelineDecompositionTest.java` | Splitting long stream chains into named steps |
| `test/.../bad/ReadabilityAndScopeTest.java` | Unclear names and wide variable scope |
| `test/.../ugly/IntersectionTypeTrapTest.java` | Mixed `List.of()` arguments infer an intersection type |
| `test/.../ugly/DiamondOperatorTrapTest.java` | `var` with `<>` infers `Object` |
| `test/.../ugly/PrimitiveOverloadTrapTest.java` | `int` literals and the `List.remove(int)` overload |
| `test/.../ugly/CompileTimeTrapTest.java` | Runs javac on snippets to prove which traps compile silently and which are rejected |
| `test/.../mechanics/BytecodeParityTest.java` | Runs javap and asserts both `BytecodeParityDemo` methods compile to identical bytecode |

Each test puts the problem (`BAD`, `TRAP`) next to the fix (`GOOD`, `SENIOR PATTERN`). `TestMocks` provides simple stand-ins for Selenium and RestAssured so everything runs without a browser or server; don't treat them as a reference for the real APIs.

## Running

Run all tests:
```bash
./mvnw clean test
```

Run one example:
```bash
./mvnw test -Dtest=PrimitiveOverloadTrapTest
```

`BytecodeParityTest` checks the bytecode automatically. To compare it yourself:
```bash
./mvnw compile
javap -v -c target/classes/com/acme/mechanics/BytecodeParityDemo.class
```

On Windows, use `.\mvnw.cmd` instead of `./mvnw`, for example:

```powershell
.\mvnw.cmd clean test
```

## Contact

For questions or feedback, contact me on [LinkedIn](https://www.linkedin.com/in/stoyan-topchiyski/).

## License

[MIT](LICENSE)
