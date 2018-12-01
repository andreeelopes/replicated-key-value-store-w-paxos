# Replicated Key-Value Store with Paxos

### Directory strucutre of the project (TBD)
###
```
server
├───logs
├───src
│   ├───main
│   │   ├───java/pt/unl/fct/ecma
│   │   │   ├───api						(api based on the Swagger specification)
│   │   │   ├───brokers					(allows the decoupling of services)
│   │   │   ├───conf					(general configurations)
│   │   │   ├───controllers				(request handlers)
│   │   │   ├───errors					(custom exceptions for HTTP errors)
│   │   │   ├───models					(models of the data)
│   │   │   ├───repositories			(allows queries to the db)
│   │   │   ├───security				(authorization and authentication services)
│   │   │   │   └───annotations			(Spring Security annotations)
│   │   │   └───services				(services that the logic of the application)
│   │   └───resources					(contains the db configuration)
│   └───test/java/pt/unl/fct/ecma		(Spring Boot Tests)
│       └───utils						(some utilities used on the tests
```


###Developers

* André Lopes nº 45617
* Nelson Coquenim nº 45694