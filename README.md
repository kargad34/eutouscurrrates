# eutouscurrrates
REQ: 
Develop a microservice, that constantly checks the currency exchange rate from Euro to US-Dollar (1 Euro = x Dollar).

 * The check period has to be configurable and the results are stored in a database.

 * The service has an HTTP-Resource with the following endpoints (The protocol design is up to you) :

   1) Get latest rate

   2) Get historical rates from startDate to endDate

 * Provide an example unit-test for a business-logic class.

* The exchange rate can be taken from a public service or be mocked.

* The database access does not need to be fully implemented, an interface is sufficient.

* The project should be managed with maven and the tests have to be executable using 'mvn test'.

* The service itself does not need to be executable.

The Project's Classes: Utility, DbActions, EurToUsd, ApiImpl, ApiResult, DbConnectionManager, Schedular, DeamonThreadFactory, UsdRateGetter, BaseCurr, CorrecpondingCurr, EuroToUsdMicroApp, TpsViaDelayedQueue, DelayedObject


The project's launcher class is EuroToUsdMicroApp.java. It mainly manages the life cycle of the app. In the project apache derby and sparkjava tools are used. Derby is an embedded DB work together with the app in the same jvm but can easily be separated if needed. Derby can also be reached via jdbc client apps please see sample starter commands' options. sparkjava is a micro web framework to host the REST API that the service provide. During startup the DB and the WEB interface is initialized.

The rate for the EUR to USD is retrieved from a REST service I have found on the internet (http://api.fixer.io/latest?symbols=USD,EUR). The rates are daily updated on that URL, thus to provide more dynamic data I prepare a mock service which gives a random rate if the last update retrieved from the REST service is done in the last 24 hours. There is a configuration(MIN_REFRESH_PERIOD) for that, if it is set to a smaller value the rates are directly updated from REST API.

DB runs on port 1527 (to enable network listener for DERBY use jvm option -Dderby.drda.startNetworkServer=true ) and the HTTP Lister port is given in properties file in src/main/resources. To enable to run tests (junit) successfully the HTTP Port should be empty on host.

For the implementation detail it can told that; The requests are received via sparkjava REST API handlers and the urls are like;

 http://localhost:[HTTP_PORT]/eurtousd/latest
 http://localhost:[HTTP_PORT]/eurtousd/between?startDate={}&endDate={}
 http://localhost:[HTTP_PORT]/eurtousd/stopService  (for graceful shut down of the service)

Results are formed in JSON style using GSON library.

ApiImpl.java is where the rest API hosted is implemented. The API is working asynchronously not to block the caller using  'CompletableFuture' and a related thread pool.

Schedular.java is for periodically retrieving latest rate and update it on DB. UsdRateGetter.java is used to handle http connection to the REST interface on the net. Additionally it has a mock rate service generating random rates if some problem occurs on getting the rate from the REST API.

DnConnectionManager.java is for managing DB server. Apache dbcp2 pooling is used for DB connections. DbActions.java  is for the query management that is needed for the table used.

Table used is created as follows: CREATE TABLE EURTOUSD (DONEDATE int NOT NULL, RATE varchar(10), PRIMARY KEY (DONEDATE))

Table can be reached via squirrel sql jdbc client. Jdbc url is : jdbc:derby://localhost/1527/derbyDB user=user1 passwd=user1

Utility.java is where utility and configuration resides. 

TpsViaDelayedQueue.java is a traffic controller that uses jdk Delayed interface to monitor the traffic with a period of time.

There are three properties file, one is for the configuration of the app itself the others are log4j configurations (App is using log4j2 but the sparkjava is using log4j)

 After the maven dependency libraries (please see pom.xml) are configured to be in the CLASSPATH of the host, the App can be run by the java command with options:

Launcher class: org.gokhanka.euuscurrrates.EuroToUsdMicroAppTest
Commandline argument:  start
Jvm argument: -Dderby.drda.startNetworkServer=true -Dderby.drda.portNumber=1527 





