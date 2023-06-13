![Prisma Cloud Code Security](src/main/resources/images/prismaLogo1.2.png)

[![Maintained by Bridgecrew.io](https://img.shields.io/badge/maintained%20by-bridgecrew.io-blueviolet)](https://bridgecrew.io/?utm_source=github&utm_medium=organic_oss&utm_campaign=checkov-vscode)
![Build](https://github.com/bridgecrewio/checkov-jetbrains-idea/workflows/Build/badge.svg)
[![Version](https://plugins.jetbrains.com/plugin/21907-prisma-cloud)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/17721-checkov.svg)](https://plugins.jetbrains.com/plugin/17721-checkov)
[![slack-community](https://img.shields.io/badge/Slack-contact%20us-lightgrey.svg?logo=slack)](https://slack.bridgecrew.io/?utm_source=github&utm_medium=organic_oss&utm_campaign=checkov-intellij)

# Prisma Cloud Plugin for JetBrains IDEA

The Prisma Cloud plugin for JetBrains leverages the capabilities of [Prisma Cloud Code Security](https://docs.paloaltonetworks.com/prisma/prisma-cloud/prisma-cloud-admin-code-security), a static code analysis tool designed specifically to scan your code for Infrastructure-as-Code (IaC) misconfigurations, Software Composition Analysis (SCA) issues and Secrets vulnerabilities.

By integrating the Prisma Cloud plugin into JetBrains, developers receive real-time scan results and inline fix suggestions while developing code.
You can download the plugin directly from the [JetBrains Plugin Marketplace](https://plugins.jetbrains.com/plugin/21907-prisma-cloud).

## Key Features
---
- Scans

  - Scans for IaC misconfigurations, SCA vulnerabilities, and Secrets and License violations

  - Includes [1000+ built-in policies](https://docs.paloaltonetworks.com/prisma/prisma-cloud/prisma-cloud-admin/prisma-cloud-policies) covering security and compliance best practices for AWS, Azure, Google Cloud, Bitbucket and Alibaba cloud providers

  - Comprehensive IaC scans for Terraform, Terraform Plan, CloudFormation, Kubernetes, Helm, Serverless and ARM templates  

  - Supports Terraform and CloudFormation checks that accurately evaluate arguments expressed in [variables](https://github.com/bridgecrewio/checkov/blob/master/docs/2.Basics/Handling%20Variables.md) and remote modules

  - Detects [AWS credentials](https://github.com/bridgecrewio/checkov/blob/master/docs/2.Basics/Scanning%20Credentials%20and%20Secrets.md) in EC2 User Data, Lambda environment variables, and Terraform providers

- Fixes: Provides automated inline fix capabilities for IaC and SCA issues directly within the editor

- Documented guidelines for commonly misconfigured attributes, secrets and licenses

- Supports inline [suppression](https://github.com/bridgecrewio/checkov/blob/master/docs/2.Basics/Suppressing%20and%20Skipping%20Policies.md) via comments to skip specific checks 

## Supported Package Managers and Languages
---

The following package managers and languages, along with their corresponding file formats, are supported:

- **NPM**: Package.json, package-lock.json, yarn.lock, bower.json
- **Python**: Requirements.txt, Pipfile, pipfile.lock
- **Go**: Go.mod, go.sum
- **Maven**: Pom.xml (including parent POMs)
- **Gradle**: Build.gradle, gradle.properties, gradle-wrapper.properties
- **Kotlin**: Build.gradle.kts
- **.NET**: Packages.config, *.csproj, Paket
- **Ruby**: Gemspec, gemfile, gemfile.lock
- **PHP Composer**: Coming soon

## Supported IaC Frameworks
---

The following infrastructure-as-code frameworks are supported:

Terraform   | CloudFormation | Kubernetes    
------------|----------------|---------------
Serverless  | Helm           | TerraformPlan 
ARM         | Dockerfile     | Bicep         
Kustomize   | OpenAPI 



## Getting started
---
### Prerequisites

* [Python](https://www.python.org/downloads/) >= 3.7, [Pipenv](https://docs.pipenv.org/) or a running [Docker](https://www.docker.com/products/docker-desktop) daemon

The Prisma Cloud plugin automatically invokes the latest version of ```Prisma Cloud Code Security```.

### Installation

- Using IDE built-in plugin system:
  
  `Settings/Preferences` > `Plugins` > `Marketplace` > `Search for "Prisma Cloud"` >
`Install Plugin`
  
- Manually:

In your IDE: Select `Settings/Preferences` (for Windows/Mac) > `Plugins` > `⚙️` > `Install plugin from disk...`

### Configuration

1. Subscribe to the Application Security module in Prisma Cloud. 

2. [Generate and save a Prisma Cloud access key](https://docs.paloaltonetworks.com/prisma/prisma-cloud/prisma-cloud-admin-code-security/get-started/generate-access-keys) which consists of an Access Key ID and Secret.

3. Configure plugin settings: In JetBrains IDE navigate to `Settings` > `Tools` > `Prisma Cloud` and fill in the provided fields: 
a. Access ID and Secret (required fields). 
b. Recommended: Provide a custom CA certificate by specifying the path to the certificate file.  
**NOTE**: Ensure that the certificate file is in the PEM format.

## Usage
---
### Scan
 
- Scan a project: Click scan or the Play button to scan a project
- Scan a file: Open a file. A scan will run automatically when opening or saving a file 

### Analyze Results and Fix Issues

- Scan results include details of violating policies and provide a link to step-by-step fixes or guidelines based on the Prisma Cloud Code Security fix dictionaries:

-- **In-line fixes**: Prisma Cloud Code Security highlights errors in the editor as you code, including details of the violating policy. To fix an error, select the line with the issue to display a popup with the description and suggested fix

-- **Problems tool panel** at the bottom of the screen: Lists vulnerabilities in the left pane. Click on a vulnerability to display its details. If a fix is available, select the Fix button that is displayed in the top right of the panel. Otherwise refer to suggested solutions or the documentation to resolve the issue  

-- **Suppression**: You can skip checks by clicking the *Suppression* button in the popup or in the *Error view* of the Problems tool panel

### Troubleshoot 

Troubleshoot errors directly in the JetBrains UI using the **Event Log**.

## Disclaimer
---
The plugin uses Prism Cloud’s ‘fixes’ API to analyze code and produce fixes, enhancing the results displayed in the JetBrains IDE. When a scan detects violations of Prisma Cloud policies in files, those files are passed to the ‘fixes’ API for the sole purpose of generating inline code fixes. For information about data collected and shared with Prisma Cloud when using Prisma Cloud Code Security, please refer to Prisma Cloud’s [Privacy Policy](https://www.paloaltonetworks.com/legal-notices/trust-center/privacy?utm_source=github&utm_medium=organic_oss&utm_campaign=checkov-vscode). 

## Support
---
Prisma Cloud builds and maintains Prisma Cloud Code Security to secure your engineering environment.
Start with our [Documentation](https://docs.paloaltonetworks.com/prisma/prisma-cloud/prisma-cloud-admin-code-security).
If you need direct support you can email us at *noreply@paloaltonetworks.com*.

---
The plugin is based on the [Jetbrains Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template

<!-- Plugin description end -->
