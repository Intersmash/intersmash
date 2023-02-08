# Contributing to Intersmash

Thank you for contributing to [Intersmash](https://github.com/Intersmash/intersmash)!

Try to follow used patterns and submit a clean PR that fix ideally one issue. 
A new unit test has to be part of the PR in case the change is related to a shared code base.

## Console logging level
Use `console-log-level` environment variable to control the console log level threshold (defaults to `INFO`).

## Adding code

To include new code into this project please open a pull request against the `main` branch with the following template:

```text 
[Description of the PR]

Please make sure your MR meets the following requirements:

* [ ] Pull Request contains a description of the changes
* [ ] Pull Request does not include fixes for multiple issues/topics
* [ ] Code is self-descriptive and/or documented
```
### Adding tooling code

- The changes must be tracked by a [GitHub issue](https://github.com/Intersmash/intersmash/issues) 
so open one to manage Intersmash feature requests, requests for enhancements and bugs.
- Open a PR referencing the GitHub issue

#### Creating an issue

To report an issue with this project, please open a new [GitHub issue](https://github.com/Intersmash/intersmash/issues) 
using the following template:

```text
[Description of the issue]
```
If the issue represents a requirement for a feature or enhancement request, please add text to provide a rationale for it.

If the issue is about a bug, please to use the follow template:
```text 
* Environment: [description of environment where this issue occurs]
* Steps to reproduce: [command(s) which can be run to reproduce the failure (including -Dtest option if applicable)]
```

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
