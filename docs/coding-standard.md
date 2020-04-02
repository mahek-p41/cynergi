# Cynergi Middleware coding standards

## General Guidelines
* Whitespace is your friend.  Don't be afraid to put new lines to separate sub sections of functions that while related are not as tightly related as other sections of the code.

## POJO's
Plain Old Java Objects as they have traditionally be defined as simple classes that only hold data.  In Kotlin this refers to a special type of class called a `data class`.


### Entity
An **Entity** is defined in this project as a `data class` that represents a single relationship in the database.  An **Entity** relates to a **Repository** in that a **Repository** only deals with an instance of an **Entity**.

#### Basic Entity Requirements
1. Implement the __com.cynergisuite.domain.Identifiable__ interface
2. Provide an `id: Long? = null` property that will hold the id property of the row the **Entity** is intended to represent.
   * Maps to the id column in the database
   * Auto populated by the database
3. Should be immutable by default
   * All properties should be `val`
   * All properties that hold collections of some sort should be immutable.
4. Create these in the root of the Model hierarchy
   * Related classes describing the model can be nested as deeply as necessary as long as they are closely related.
   * For example inventory audits **AuditEntity** and **AuditExceptionEntity** are closely related with their relationship being described as follows:
     * **AuditEntity** lives in __com.cynergisuite.middleware.audit__
     * **AuditExceptionEntity** lives in __com.cynergisuite.middleware.audit.exception__
5. Use classes from other parts of the domain as much as possible to represent relationships rather than simple ***tableName|entityName***Id
6. Depending on how the relationship is expressed within the Class Hierarchy itself simplifications can be used such as replacing the need for the entire **Entity** graph by using __com.cynergisuite.domain.Identifiable__ to represent a parent if the need to associate the parent directly is required.  Other possibilities exist where the need for a subset of the data an **Entity** needs to be exposed as part of the relationship, which will require a separate Class definition that defines that subset.
   * It is not necessary when referencing other parts of the domain to pull in an entire loosely related **Entity** if only a subset is required.  Of course judgement will have to be used to determine when to pull in the entire **Entity**
     * TODO determine rules for this
7. Constructors should define how **Entity** instances are created.  Do not use setters to fill out an Object
7. The primary constructor should describe all the properties that the **Entity** will be capturing
8. Use secondary constructors to do transformations or creations with different needs


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
3. Because of #2 will contain the __javax.validation__ annotations.
4. Because of #2 will contain Swagger Documentation Annotations for generating the API documentation.
5. 

### DataTransferObject

## Infrastructure

### Repository

### Controller

## Business Logic

### Service

### Validator
Validators should provide any business logic around validating when a request to create or update operation is being requested against part of the model.

## Utilities
There should be no Java static "Utility" classes in this project.  Instead use Kotlin Extension methods if you need to add functionality to classes that are provided by 3rd party dependencies.

TODO: Determine what to do with similar shared functionality between different very loosely related parts of the domain