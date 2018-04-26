# FIESTA-IoT Experiment Management Console

### To install

Before proceeding with the installation make sure:
- mysql is installed and running
 - to start you can use `sudo <PATH_TO_MYSQL.SERVER>/mysql.server start`. This is sometime `/usr/local/mysql/support-files/`
-  ERM component is installed and running, and `utils.fiesta-iot` library is accessible to maven
- Authentication and Authorization component (OpenAm) is installed and running.
- Wildfly is installed and running. The code has been tested to successfully work on WildFly v10.0.0

Then 
- copy following properties to `fiesta-iot.properties` after changing the `<HOST>`, `<OPEN AM ADMIN USER>`, `<OPEN AM ADMIN USER'S PASSWORD>`. Following assumes that all the components are deployed in one machine and have a same host. In case you have different HOST for ERM, EEE, EMC, Data repository, and Security server, use appropriate HOSTs. `fiesta-iot.properties` could be found in PATH_TO_WILDFLY/wildfly-10.0.0.Final/standalone/configuration. In case it is not available create `fiesta-iot.properties`.

```
eee.console.PATH=<HOST>/experimentConsole
eee.console.ERM_PATH=<HOST>/experiment.erm/rest/experimentservices
eee.console.ERM_API_GETAllUSEREXPERIMENTSDESCRIPTIONS=/getAllUserExperimentsDescreptions
eee.console.ERM_API_GETEXPERIMENTDESCRIPTION=/getExperimentModelObject
eee.console.ERM_API_GETEXPERIMENTSERVICEMODELOBJECT=/getExperimentServiceModelObject
eee.console.EXPERIMENT_EDITOR_UI=
eee.console.ERM_API_DISCOVERABLE=/getDiscoverableExperimentServiceModelObject
eee.console.EEE_PATH=<HOST>/schedulerServices
eee.console.EEE_API_SCHEDULE=/scheduler/scheduleFISMOExecution
eee.console.EEE_API_GETJOBIDFISMOUSERFEMO=/scheduler/getJobIDfromFISMOIDUserIDandFEMOID
eee.console.EEE_API_GETJOBSTATUS=/monitoring/getJobIDStatus
eee.console.EEE_API_GETJOBIDLOG=/monitoring/getJobExecutionLog
eee.console.EEE_API_POLLING=/polling/pollForReport
eee.console.EEE_API_RESTART=/scheduler/resumeJobExecution
eee.console.EEE_API_STOP=/scheduler/stopJobExecution
eee.console.EEE_API_DELETEFISMO=/scheduler/deleteScheduledJobsOfFismo
eee.console.EEE_API_GETEXPERIMENTERSUBSCRIPTIONS=/monitoring/getMySubscriptionsforExperiment
eee.console.EEE_API_UNSUBSCRIBE=/subscription/unsubscribeToFISMOReport
eee.console.EEE_API_SUBSCRIBE=/subscription/subscribeToFISMOReport
eee.console.SECURITY_API_GETUSER=<HOST>/openam/json/users?_action=idFromSession
```

Once the above steps are done use following
``` sh
cd <PATH_TO_EXPERIMENT_CONSOLE>/experimentConsole
mvn clean install
cd <PATH_TO_WILDFLY_DEPLOYMENT_FOLDER>/deployments/
rm -rf scheduler*
cp PATH_TO_EXPERIMENT_CONSOLE>/experimentConsole/target/experimentConsole.war <PATH_TO_WILDFLY_DEPLOYMENT_FOLDER>/deployments/
```

You would need to get authentication token first from the FIESTA-IoT OpenAM server. Once it is available you can access using the following:
`<HOST>/experimentConsole/experimentConsole.jsp?iPlanetDirectoryPro=<TOKEN>`

