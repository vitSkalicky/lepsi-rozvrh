fastlane documentation
================
# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```
xcode-select --install
```

Install _fastlane_ using
```
[sudo] gem install fastlane -NV
```
or alternatively using `brew install fastlane`

# Available Actions
## Android
### android deployPlay
```
fastlane android deployPlay
```
Build and deploy a new version to the Google Play internal track. Promotion to production is done manually from Google Play console
### android buildRelease
```
fastlane android buildRelease
```
Builds a new release for f-droid or official manual distribution. If using yourself, please change flavor below from 'Official' to 'Development'.
### android buildDebug
```
fastlane android buildDebug
```
Builds a debug apk

----

This README.md is auto-generated and will be re-generated every time [fastlane](https://fastlane.tools) is run.
More information about fastlane can be found on [fastlane.tools](https://fastlane.tools).
The documentation of fastlane can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
