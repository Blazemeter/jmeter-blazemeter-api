# JMeter BlazeMeter API

This project is a fork from [Blazemeter/blazemeter-api-client](https://github.com/Blazemeter/blazemeter-api-client)  project.

This project is consumed by [Blazemeter/bzm-repositories-plugin](https://github.com/Blazemeter/bzm-repositories-plugin) JMeter Plugin. Where the last mentioned is not only a JMeter Plugin but an Extension for the [Auto Correlation Recorder](https://github.com/Blazemeter/CorrelationRecorder).

**Why aren't we using the original project**

This forked version supports the same API calls done in the original project but also gives support to the new Correlation Rule Templates schemas.

Aside from that difference which could have been implemented in the original repo by contributing.
The original project used a lot of third party dependencies such as HttpOK among others. Resulting (while compiling the uber-jar) in a large and heavy jar file, that increased sustantialy the bzm-repositories project when including it.

Since this project required to be shipped with bzm-repositories the team decided to re-implement the HTTP communication using JMeter dependencies (Apache HTTP),therefore, drastically reducing the size of the uber-jar.

With that being said, we can define this project as a *Lightweight JMeter Dependent BlazeMeter API for Java*

---

We, at BlazeMeter, aim to bring you tools for quicker, and easier development.

As part of this ambition, we are proud to present our Java SDK!

Read more about this library on our [wiki page](https://github.com/Blazemeter/blazemeter-api-client/wiki).

[![Build Status](https://travis-ci.org/Blazemeter/blazemeter-api-client.svg?branch=master)](https://travis-ci.org/Blazemeter/blazemeter-api-client)
[![codecov](https://codecov.io/gh/Blazemeter/blazemeter-api-client/branch/master/graph/badge.svg)](https://codecov.io/gh/Blazemeter/blazemeter-api-client)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/e86b726f20e046a2b89fc13c86ca6f87)](https://www.codacy.com/app/dzmitrykashlach/blazemeter-api-client?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=Blazemeter/blazemeter-api-client&amp;utm_campaign=Badge_Grade)

Supported calls:  

|Description   |Method   |Path   |
|:---|:---|:---|
|**Master**|||
|Get Sessions|GET|/api/v4/sessions?masterId={masterId}|
|Stop Master|POST|/api/v4/masters/{masterId}/stop|
|Terminate Master|POST|/api/v4/masters/{masterId}/terminate|
|Get Master Status Code|GET   |/api/v4/masters/{masterId}/status?events=false   |
|Get CI Status |GET   |/api/v4/masters/{masterId}/ci-status   |
|Get Public Report|GET|/api/v4/masters/{masterId}/public-token|
|Get Summary|GET   |/api/v4/masters/{masterId}/reports/main/summary   |
|Get JUnit Report|GET|/api/v4/masters/{masterId}/reports/thresholds?format=junit|
|Get Functional Report|GET|/api/v4/masters/{masterId}|
|Post Notes |PATCH|/api/v4/masters/{masterId}|
|**Session**||||
|Post Properties |POST|/api/v4/sessions/{sessionId}/properties?target=all|
|Get JTL Report |GET|/api/v4/sessions/{sessionId}/reports/logs|
|**User**||||
|Get User|GET|/api/v4/user|
|Get Accounts|GET|/api/v4/accounts?limit={limit}&sort[]={sort}|
|**Workspace**||||
|Get Workspace|GET|/api/v4/workspaces/{workspaceId}|
|Create Project|POST|/api/v4/projects|
|Get Projects|GET|/api/v4/projects?workspaceId={workspaceId}&limit={limit}&sort[]={sort}|
|Get Single Tests|GET|/api/v4/tests?workspaceId={workspaceId}&limit={limit}&sort[]={sort}|
|Get Multi Tests|GET|/api/v4/multi-tests?workspaceId={workspaceId}&limit={limit}&sort[]={sort}|
|**Project**||||
|Create Single Test|POST|/api/v4/tests|
|Get Single Tests|GET|/api/v4/tests?projectId={projectId}&limit={limit}&sort[]={sort}|
|Get Multi Tests|GET|/api/v4/multi-tests?projectId={projectId}&limit={limit}&sort[]={sort}|
|**Account**||||
|Create Workspace|POST|/api/v4/workspaces|
|Get Workspaces |GET|/api/v4/workspaces?accountId={accountId}&enabled={enabled}&limit={limit}|
|**Single Test**||||
|Get Test|GET|/api/v4/tests/{testId}|
|Start |POST|/api/v4/tests/{testId}/start|
|Update Test |PATCH|/api/v4/tests/{testId}|
|Upload files |POST|/api/v4/tests/{testId}/files|
|Validate Test |POST|/api/v4/tests/{testId}/validate|
|Get Test Validations |GET|/api/v4/tests/{testId}/validations|
|**Multi Test**||||
|Get Multi Test|GET|/api/v4/multi-tests/{testId}|
|Start|POST|/api/v4/multi-tests/{testId}/start|


	
