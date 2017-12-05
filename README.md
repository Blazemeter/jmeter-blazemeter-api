# BlazeMeter Java SDK

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
|Get Sessions|GET|/api/v4/masters/{masterId}/sessions|
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
|Send Data|POST|/submit.php|
|Post Properties |POST|/api/v4/sessions/{sessionId}/properties?target=all|
|Get JTL Report |GET|/api/v4/sessions/{sessionId}/reports/logs|
|Terminate Session External|POST|/api/v4/sessions/{sessionId}/terminate-external|
|**User**||||
|Get User|GET|/api/v4/user|
|Get Accounts|GET|/api/v4/accounts|
|**Workspace**||||
|Create Project|POST|/api/v4/projects|
|Get Projects|GET|/api/v4/projects?workspaceId={workspaceId}&limit=99999|
|Get Single Tests|GET|/api/v4/tests?workspaceId={workspaceId}|
|Get Multi Tests|GET|/api/v4/multi-tests?workspaceId={workspaceId}|
|**Project**||||
|Create Single Test|POST|/api/v4/tests|
|Get Single Tests|GET|/api/v4/tests?projectId={projectId}|
|Get Multi Tests|GET|/api/v4/multi-tests?projectId={projectId}|
|**Account**||||
|Create Workspace|POST|/api/v4/workspaces|
|Get Workspaces |GET|/api/v4/workspaces?accountId={accountId}&enabled=true&limit=100|
|**Single Test**||||
|Get Test|GET|/api/v4/tests/{testId}|
|Start |POST|/api/v4/tests{testId}/start|
|Start External|POST|/api/v4/tests{testId}/start-external|
|**Multi Test**||||
|Get Multi Test|GET|/api/v4/multi-tests/{testId}|
|Start|POST|/api/v4/multi-tests/{testId}/start|
|**Anonymous Test**||||
|Start External|POST|/api/v4/sessions|


	
