# Building a MongoDB and Spring Reactive App With Many-Many-Relationships

In this app, I wanted to toy with the complications of managing many-to-many relationships with a NoSQL database such as Mongo and a Reactive environment such as Reactor.  
Create a `Contract` for one or more `Property` s  which are owned by one or more `Client` s  who need money from one or `MadBanker` s. But then figure it this way:

* a Property (like a shopping mall) may need a couple of Contracts to handle the building and the parking lot
* But then, one might need a report on a particular `Client` who has a number `Contract` s with their attendant `Property` s and `MadBanker` obligations.

`BankingAgg` is an aggregation in the Entity or Model layer which helps to create associations between the Entities.

Most of the data work is handled in the Service layer. That was easy to handle and view with tools like MongoDB Compass and Atlas. However the simple attachment of a `Property` to a `Contract` didn't show up when viewing the transaction with a WebClient or a tool like Postman. That only worked when 'destucturing' the zipped set of Tuples (through Mono.zip() or Flux.zip()) with the TupleUtils.function() method. That method, based on a `@FunctionalInterface` provided a learning experience in itself. 

In the Controller layer, I utilized the regular `@RestController` as well as `RouterFunction<ServerResponse>` implementations with associated Handlers. Not all the simple CRUD operations are set up on the Controller layer. The CRUD ops are on the Service layer. I'll fix 'em later.

In the `application.properties` file, you'll find the following among other items:
```text
spring.profiles.active=classic
# spring.profiles.active=data
```
When first starting the app comment out the "...active = classic" and uncomment the "...active=data" like so:
```text
# spring.profiles.active=classic
spring.profiles.active=data
``` 
That will load up your MongoDB with some data. (Don't forget to 'reset' spring.profiles.active)
```text
spring.profiles.active=classic
``` 


The basics of the project are:

* Project type: Maven
* Language: Java
* Spring Boot version :2.7.3
* Project Metadata/Java: 17
* Dependencies: Reactive Web
* And make sure you have Lombok installed


### References

* [Project Reactor docs](https://projectreactor.io/docs/core/release/reference/)
* [Destructuring a tuple](http://www.java-allandsundry.com/2019/12/project-reactor-de-structuring-tuple.html)
* [ObjectInputFilter by Heinz Kabutz at JavaSpecialists](https://www.javaspecialists.eu/archive/Issue304-ObjectInputFilter.html)


