# Contributing to Intersmash

Thank you for contributing to [Intersmash](https://github.com/Intersmash/intersmash)!

When submitting changes, try to follow used patterns and prepare a clean PR that should ideally fix one issue. 
A new unit test _should_ to be part of the PR in case the changes are related to a common code base.

## Adding code

To contribute changes to Intersmash please open a pull request against the `main` branch by referencing the 
issue that tracks the work. 

As said, the changes must be tracked by a [GitHub issue](https://github.com/Intersmash/intersmash/issues), so feel free to open one to manage Intersmash 
feature requests, requests for enhancements and bugs.

Once you're satisfied with your changes, push them by opening a PR referencing the GitHub issue, e.g.: 
_"\[issue 9\] - Reorgaanize project docs"_. 
Commit messages should include the issue tracker to, e.g.: _"\[issue-218\] - Update supported Kafka"_

Once pushed, automatic CI checks will be run to test the changes, and reported on the GitHub pull request, see e.g.:
https://github.com/Intersmash/intersmash/pull/220/checks.
Once those are green, one project maintainer should start OpenShift tests, which are run on internal infrastructure.
The validation job outcome will be reported as a comment to the PR, see e.g.: https://github.com/Intersmash/intersmash/pull/220#issuecomment-2589828030.
Project maintainers will look into the failures and provide with further details until OpenShift tests are deemed to 
be passing.

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
