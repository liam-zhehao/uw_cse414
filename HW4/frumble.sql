CREATE TABLE Sales (
    name TEXT,
    discount TEXT,
    month TEXT,
    price INT
);

.mode csv
.import mrFrumbleData.csv as Sales

--To find whether name → discount, name → month or name → price
SELECT name, COUNT(DISTINCT discount), COUNT(DISTINCT month), COUNT(DISTINCT price)
FROM Sales
GROUP BY name;
-- The output shows that name → price

--To find whether discount → name, discount → month or discount → price
SELECT discount, COUNT(DISTINCT name), COUNT(DISTINCT month), COUNT(DISTINCT price)
FROM Sales
GROUP BY discount;
-- The output shows that discount does not have one functional dependency

--To find whether month → name, month → discount or month → price
SELECT month, COUNT(DISTINCT name), COUNT(DISTINCT discount), COUNT(DISTINCT price)
FROM Sales
GROUP BY month;
-- The output shows that month → discount

--To find whether price → name
--Because name → price, if price → discount or price → discount, there will be a conflict
SELECT price, COUNT(DISTINCT name)
FROM Sales
GROUP BY price;
-- The output shows that price does not have one functional dependency

--From above, name → price and month → discount

--To find whether name, discount → month
SELECT name, discount, COUNT(DISTINCT month)
FROM Sales
GROUP BY name, discount;

--To find whether discount,month → name, price
SELECT discount, month, COUNT(DISTINCT name), COUNT(DISTINCT price)
FROM Sales
GROUP BY discount, month;

--To find whether discount,price → name,month
SELECT discount, price, COUNT(DISTINCT name), COUNT(DISTINCT month)
FROM Sales
GROUP BY discount, price;

--To find whether month,price → name
SELECT month, price, COUNT(DISTINCT name)
FROM Sales
GROUP BY month, price;

-- From above, it has the minium times of checking the FDs and find none. And no need to check more.

-- Decompose into BCNF and create the tables
CREATE TABLE S1(
    name TEXT PRIMARY KEY,
    price INT
);

CREATE TABLE S2(
    month TEXT PRIMARY KEY,
    discount TEXT
);

CREATE TABLE S3(
    name TEXT REFERENCES S1,
    month TEXT REFERENCES S2
);

INSERT INTO S1(name, price)
SELECT DISTINCT name, price FROM Sales;
-- S1 size 36
INSERT INTO S2(month, discount)
SELECT DISTINCT month, discount FROM Sales;
SELECT * FROM S2;
SELECT COUNT(*) FROM S2;
-- S2 size 12
INSERT INTO S3(name, month)
SELECT DISTINCT name, month FROM Sales;
SELECT * FROM S3;
SELECT COUNT(*) FROM S3;
-- S3 size 426