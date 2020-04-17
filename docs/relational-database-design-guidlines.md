# Relational Database Design Guidelines

## Table Naming
* Need to indicate the data that is being stored
* Need to be singular
  * Ag insurance example farm_owners_dwelling instead of farm_owners_dwellings when defining data about a dwelling on a Farm Owners policy.  Think a farmhouse.
* Need to use complete words for names
  * Ag insurance example instead of fo_ob you would want to use farm_owners_out_building when defining the table  for holding information about buildings under a Farm Owners policy that are part of a dwelling, think shed or detached garage.
* If the table is going to hold domain values, it needs to be post fixed with _type_domain
  * Basically, any table that will hold a fixed set of values managed by the installation vs the user.  So, for example days of the week would be day_of_the_week_type_domain.
* When defining foreign keys, the name of the table being referenced should be duplicated in the name of the column and postfixed with _id.
  *	Ag insurance example farm_owners_out_building references the farm_owners_dwelling it is associated with so the foreign key column in farm_owners_out_building would be named farm_owners_dwelling_id

## Standard Table Requirements
* Required to have the following four columns
  * id BIGSERIAL NOT NULL PRIMARY KEY
  * uu_row_id UUID DEFAULT uud_generate_v1()  NOT NULL
    * This is intended to be used for synchronization between datastores in the future, think like copying data from an operational database to a data warehouse.
  * time_created TIMESTAMPZ DEFAULT clock_timestamp() NOT NULL
  * time_updated TIMESTAMPZ DEFAULT clock_timestamp() NOT NULL
* Make sure to attach the last_update_column_fn as a trigger to all tables to keep the time_updated accurate as well as ensuring the uu_row_id is immutable.
* When defining columns in general your first instinct should be to make it NOT NULL
* When defining foreign keys your first instinct should be to make it NOT NULL
* When defining foreign keys there should never be a default value provided
* When defining columns that hold a BOOLEAN they should always be NOT NULL and a default pertinent to what is being described provided.
  * The reasoning for this is that defining a BOOLEAN column as nullable introduces a third state and is no longer a BOOLEAN
* Providing defaults for other columns types should be used sparingly
* Required to have the following four columns
  * id BIGSERIAL NOT NULL PRIMARY KEY
  * uu_row_id UUID DEFAULT uud_generate_v1()  NOT NULL
    * This is intended to be used for synchronization between datastores in the future, think like copying data from an operational database to a data warehouse.
  * time_created TIMESTAMPZ DEFAULT clock_timestamp() NOT NULL
  * time_updated TIMESTAMPZ DEFAULT clock_timestamp() NOT NULL
* Make sure to attach the last_update_column_fn as a trigger to all tables to keep the time_updated accurate as well as ensuring the uu_row_id is immutable.
* When defining columns in general your first instinct should be to make it NOT NULL
* When defining foreign keys your first instinct should be to make it NOT NULL
* When defining foreign keys there should never be a default value provided
* When defining columns that hold a BOOLEAN they should always be NOT NULL and a default pertinent to what is being described provided.
  * The reasoning for this is that defining a BOOLEAN column as nullable introduces a third state and is no longer a BOOLEAN
* Providing defaults for other columns types should be used sparingly

## Domain Table Requirements
Required to have the following four columns:

* id INTEGER NOT NULL
  * This is an integer because these tables should hold a fixed number of values defined by the system that can be contained in 4 bytes.
  * This column is not attached to a sequence and is managed by the migration scripts.  The reasoning for this is that values stored here need to be known to the business logic without querying the database and therefore need to have predictable values.
* value VARCHAR(at least 50 characters) NOT NULL
  * Contents should be all caps
  * Contents should be unique
  * Contents should not contain padded white space on the left or right and should have a minimum length of 1 enforced with a check constraint
* description VARCHAR(at least 100 characters) NOT NULL
  * This is the default English description of a Domain value.
* localization_code (at least 50 characters) NOT NULL
  * This lines up with the localizations provided by the application so that depending on the user’s locale a relevant message can be displayed to them
  * Typically values in this column will be the English equivalent of the description with the requirement that all letters are lower case and spaces have been replaced with dots.

There can be additional columns such as color or font maybe or pretty much anything else to help with describing the domain value to the user.

## Cross Reference Table Requirements (aka join tables)
* Should by default have two columns
* Both columns should be BIGINT and be foreign key references to other tables
* Prefer defining a unique constraint with the two foreign key columns
* Both foreign key columns should be NOT NULL
* The foreign key columns should be the table name they are referencing post fixed with _id
* Can hold other values useful for defining information about the relationship, but this is a rarity and should be used sparingly

## Column Naming
* Need to indicate the data that is being stored but not the type of data in the name.
  * No need to prefix or postfix everything with text_ to columns that store character types
  * Dates and Timestamps can kind of ignore this rule as date or time is usually indicative of their function within the language of the system.  date_of_birth for example.
    * Prefer time to timestamp, or consider using at in place of time.
* Prefer spelling out names to using abbreviations like Table Naming
  * Possible exceptions are when the column name exceeds the maximum length allowed by Postgres, or maybe well understood abbreviations such as QTY for quantity.
* Avoid prefixing columns with the table name that the column is defined in
  * farm_owners_dwelling would not have its coverage amount defined as fod_coverage_amount instead it would just be coverage_amount

## Objects other than table naming conventions
* Function names should end in _fn
* Foreign key names should end in _fkey
* Sequence names should end in _seq
* Check constraints should end in _check
* Unique constraints should end in _uq
* Triggers should end in _trg
* Soft Foreign Keys end in _sfk
  * this is going to be anything that has to reference data hosted in the fastinfo_production schema

## Constraints
* If you have a unique constraint attached to a single column there is no need to add an additional index to it, as Postgres uses indexes to manage the uniqueness of the column
* Creating a multi-column unique constraint that has as one of its parts the PRIMARY KEY column will make the constraint useless as every row inserted into the table will always have a unique PRIMARY KEY and therefore the unique constraint will never be enforced
