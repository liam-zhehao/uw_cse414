CREATE TABLE Person (
    ssn INT PRIMARY KEY,
    name TEXT
);

CREATE TABLE Driver (
    driverID INT PRIMARY KEY,
    ssn INT UNIQUE,
    FOREIGN KEY (ssn) REFERENCES Person(ssn)
);

CREATE TABLE NonProfessionalDriver (
    driverID INT PRIMARY KEY,
    FOREIGN KEY (driverID) REFERENCES Driver(driverID)
);

CREATE TABLE ProfessionalDriver (
    driverID INT PRIMARY KEY,
    medicalHistory TEXT,
    FOREIGN KEY (driverID) REFERENCES Driver(driverID)
);

CREATE TABLE InsuranceCo (
    name TEXT PRIMARY KEY,
    phone INT
);

CREATE TABLE Vehicle (
    licensePlate TEXT PRIMARY KEY,
    year INT
);

CREATE TABLE Car (
    licensePlate TEXT PRIMARY KEY REFERENCES Vehicle(licensePlate),
    make TEXT
);

CREATE TABLE Truck (
    licensePlate TEXT PRIMARY KEY REFERENCES Vehicle(licensePlate),
    capacity INT
);

CREATE TABLE Owns (
    licensePlate TEXT PRIMARY KEY REFERENCES Vehicle,
    ssn INT REFERENCES Person
);


CREATE TABLE Insures (
    licensePlate TEXT PRIMARY KEY REFERENCES Vehicle,
    name TEXT REFERENCES InsuranceCo,
    maxLiability REAL
);

CREATE TABLE Drives (
    licensePlate TEXT REFERENCES Vehicle,
    ssn INT REFERENCES Person
);

CREATE TABLE Operates (
    licensePlate TEXT PRIMARY KEY REFERENCES Vehicle,
    ssn INT REFERENCES Person 
);

-- Part b: Which relation in your relational schema represents the relationship "insures" in the E/R diagram?
-- The relation that represents "insures" is the table named Insures. It captures the many-to-one relationship between InsuranceCo and Vehicle.
-- And the regular arrow means each Vehicle at most has one InsuranceCo.

-- Part c: Compare the representation of the relationships "drives" and "operates" in your schema, and explain why they are different.
-- The "Drives" relationship connects NonProfessionalDriver to Car, and is many-to-many relationship, meaning that drivers who are nonprofessional drive vehicles which are cars.
-- The "Operates" relationship connects ProfessionalDriver to Truck, and is many-to-one relationship, meaning that vehicles which are trucks at most have one driver who is professional.
