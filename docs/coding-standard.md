# Cynergi Middleware coding standards

---
## POJO's
Plain Old Java Objects as they have traditionally be defined as simple classes that only hold data.  In Kotlin this refers to a special type of class called a `data class`

### Entity
An **Entity** is defined in this project a `data class` that represents a single relationship in the database.  An **Entity** relates to a **Repository** in that a **Repository** only deals with an instance of an **Entity**.

#### Basic Entity Requirements
1. Implement the __com.cynergisuite.domain.Identifiable__ interface
2. 

### ValueObject

### DataTransferObject

---
## Infrastructure

### Repository

### Controller

---
## Business Logic

### Service

### Validator

## Utilities
There should be no Java static "Utility" classes in this project.  Instead use Kotlin Extension methods if you need to add functionality to classes that are provided by 3rd party dependencies.

TODO: Determine what to do with similar shared functionality