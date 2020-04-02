# Cynergi Middleware coding standards

## General Guidelines
* Whitespace is your friend.  Don't be afraid to put new lines to separate sub sections of functions that while related are not as tightly related as other sections of the code.

## POJO's
Plain Old Java Objects as they have traditionally be defined as simple classes that only hold data.  In Kotlin this refers to a special type of class called a `data class`.


### Entity
An **Entity** is defined in this project as a `data class` that represents a single relationship in the database.  An **Entity** relates to a **Repository** in that a **Repository** only deals with an instance of an **Entity**.

#### Basic Entity Requirements
1. Implement the _com.cynergisuite.domain.Identifiable_ interface
2. Provide an `id: Long? = null` property that will hold the id property of the row the **Entity** is intended to represent.
   * Maps to the id column in the database
   * Auto populated by the database
3. Should be immutable by default
   * All properties should be `val`
   * All properties that hold collections of some sort should be immutable.
4. Create these in the root of the Model hierarchy
   * Related classes describing the model can be nested as deeply as necessary as long as they are closely related.
   * For example inventory audits **AuditEntity** and **AuditExceptionEntity** are closely related with their relationship being described as follows:
     * **AuditEntity** lives in _com.cynergisuite.middleware.audit_
     * **AuditExceptionEntity** lives in _com.cynergisuite.middleware.audit.exception_
5. Use classes from other parts of the domain as much as possible to represent relationships rather than simple ***tableName|entityName***Id
6. Depending on how the relationship is expressed within the Class Hierarchy itself simplifications can be used such as replacing the need for the entire **Entity** graph by using _com.cynergisuite.domain.Identifiable_ to represent a parent if the need to associate the parent directly is required.  Other possibilities exist where the need for a subset of the data an **Entity** needs to be exposed as part of the relationship, which will require a separate Class definition that defines that subset.
   * It is not necessary when referencing other parts of the domain to pull in an entire loosely related **Entity** if only a subset is required.  Of course judgement will have to be used to determine when to pull in the entire **Entity**
     * TODO determine rules for this
7. Constructors should define how **Entity** instances are created.  Do not use setters to fill out an object, this will lead to inconsistent requirements in larger **Entity** definitions where it can become unclear what is required to get a usable instance of an **Entity**.
7. The primary constructor should describe all the properties that the **Entity** will be capturing.
8. Use secondary constructors to do transformations.
9. Kotlin's `.copy()` methods can be used when changes need to be applied to an **Entity**
   * Note that this creates a "shallow" copy


#### Optional Entity Components
These are properties that are available from the database, but may not be necessary when providing data to a requesting client.

1. `uuRowId: UUID`
   * Maps to the uu_row_id column in the database of all user tables.
   * Auto populated by the database
2. `timeCreated: OffsetDateTime`
   * Maps to to the time_created column in the database of all user tables
   * Auto populated by the database
3. `timeUpdated: OffsetDateTime`
   * Maps to the time_updated
   * Auto populated by the database.

### ValueObject
A **ValueObject** is defined in this project as a `data class` that represents a detached data holder that can be manipulated directly by business logic without worrying about needing to persist to the database. It is intended to be transient and can be passed between **Service** instances and **Controller** instances.

Rules of thumb for **ValueObject** 
1. Should mirror the **Entity** it is representing.
2. Try to use these as much as possible when passing between different instances of either **Service** or **Controller**.
3. Because of #2 will contain the _javax.validation_ annotations.
4. Because of #2 will contain Swagger Documentation Annotations for generating the API documentation.
7. Constructors should define how **Entity** instances are created.  Do not use setters to fill out an object, this will lead to inconsistent requirements in larger **Entity** definitions where it can become unclear what is required to get a usable instance of an **Entity**.
7. The primary constructor should describe all the properties that the **Entity** will be capturing.
8. Use secondary constructors to do transformations.

### DataTransferObject
**DataTransferObjects** are used when a **ValueObject** does not give a shape to the data required by requesting clients. There will be fewer in number of these "DTO's" than either **Entity** or **ValueObject** definitions.  DTO's are only really useful in situations where the shape of the data as described by **ValueObject** or **Entity** object graphs need to be sent or received from an external source.

### Type's
**Type** classes are essentially enumerations (but not a Kotlin `enum`) that are backed by the database in tables that are postfixed with `_type_domain`. These will be used to describe possible choices presented to the user in most instances.  They are used by the database to enforce integrity of rows in various tables, helping to define the domain model in the database.

Type's may not always have a **Repository** all their own as they may only be loaded by their "parent" association within the domain model.

#### Simple Type's
1. Implement _com.cynergisuite.domain.TypeDomainEntity_ interface
2. At a minimum should have:
   1. `id: Long`
   2. `value: String`
   3. `description: String`
   4. `localizationCode: String`

An example of a simple **Type** is _com.cynergisuite.middleware.audit.permission.AuditPermissionType_.

#### Complex Type's
A Complex **Type** is one where where business rules act upon

## Business Logic

### Service

### Validator
Validators should provide any business logic around validating when a request to create or update operation is being requested against part of the model.

## Infrastructure

### Repository
A **Repository** class is used to define the interaction with the database.  This is where select/insert/update/delete queries will be written and mapped to an instance or instances of an **Entity**.

### Controller
A **Controller** defines the public API.  This is where requests from clients are routed to appropriate business logic.  There won't be a perfect one-to-one between an **Entity** and a **Controller**, but at least a majority (> 50%) of the time  there will be an **Entity** that backs a **Controller**.

#### Requirements
1. Should be annotated with _io.micronaut.http.annotation.Controller_
   * The path needs to be provided that will denote the "root" that the controller will service.  Most of the time this will be very similar to the part of the domain it will be interacting with.  For example: "/api/audit" defines the root of the Audit domain
2. Should never deal directly with an **Entity**
3. Should always interact with **Service** instances by passing either **DataTransferObject** or **ValueObject** instances between the methods
4. Web tier data should stay in the **Controller**
   * For example: If information about a user is required by the business logic
5. Should only provide paged output when dealing with collections of data.  The only exception being where there is a fixed amount of data that is known at development time.  Think data stored in "_type_domain" tables.  These "_type_domain" tables are used to describe and enforce finite values available to various relationships in the database.

Rules of thumb
1. Will almost always provide a `fetcOne` method that takes and ID to be looked up.
   * Will most likely need to limit what ID's can be loaded based on the logged in user's ability to access data.  Think a user can only read Audit's for the company they logged into.
2. Will many times provide a 


## Utilities
There should be no Java static "Utility" classes in this project.  Instead use Kotlin Extension methods if you need to add functionality to classes that are provided by 3rd party dependencies.  It is OK to use "Utility" classes provided by 3rd party libraries such as Apache Commons or Google Guava.

TODO: Determine what to do with similar shared functionality between different very loosely related parts of the domain