# Cynergi Middleware coding standards

## General Guidelines
* Whitespace is your friend.  Don't be afraid to put new lines to separate sub sections of functions that while related are not as tightly related as other sections of the code.

## POJO's
Plain Old Java Objects as they have traditionally be defined as simple classes that only hold data.  In Kotlin this refers to a special type of class called a `data class`.


### Entity
An **Entity** is defined in this project a `data class` that represents a single relationship in the database.  An **Entity** relates to a **Repository** in that a **Repository** only deals with an instance of an **Entity**.

#### Basic Entity Requirements
1. Implement the __com.cynergisuite.domain.Identifiable__ interface
2. Provide an `id: Long? = null` property that will hold the id property of the row the **Entity** is intended to represent.

### ValueObject

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