# Contributing to Intersmash

Thank you for contributing to [Intersmash](https://github.com/Intersmash/intersmash)!

Try to follow used patterns and submit a clean PR that fix ideally one issue. 
A new unit test has to be part of the PR in case the change is related to a shared code base.

## Adding code

To include new code into this project please open a pull request against the `main` branch by referencing the 
issue that tracks the work.

### Adding tooling code

- The changes must be tracked by a [GitHub issue](https://github.com/Intersmash/intersmash/issues) 
so open one to manage Intersmash feature requests, requests for enhancements and bugs.
- Open a PR referencing the GitHub issue, e.g.: _[issue 9] - Reorgaanize project docs_

#### Creating an issue

To report an issue with this project, please open a new [GitHub issue](https://github.com/Intersmash/intersmash/issues).
Choose the proper template and fill it with the required information.

### Code conventions

Automatic code formatting and imports sorting plugins are applied on the project. Run
```
mvn process-sources
```

to format your code before sending it for revision.
CI jobs will run the checks (`mvn formatter:validate impsort:check`) and fail in case of wrong formatting.

To set up your IDE to comply with the formatting, please get the 
[eclipse-format.xml](./ide-config/eclipse-format.xml) configuration file and follow
[Eclipse Code Formatter instructions](https://github.com/krasa/EclipseCodeFormatter#instructions).
