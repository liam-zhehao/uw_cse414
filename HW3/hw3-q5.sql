WITH DirectCities AS (
    SELECT DISTINCT
        dest_city
    FROM
        FLIGHTS
    WHERE
        origin_city = 'Seattle WA'
),
OneStopCities AS (
    SELECT DISTINCT
        f2.dest_city
    FROM
        FLIGHTS f1
    JOIN
        FLIGHTS f2 ON f1.dest_city = f2.origin_city
    WHERE
        f1.origin_city = 'Seattle WA'
        AND f2.dest_city != 'Seattle WA'
        AND NOT EXISTS (
            SELECT 1
            FROM DirectCities
            WHERE dest_city = f2.dest_city
        )
),
AllCities AS (
    SELECT DISTINCT
        origin_city AS city
    FROM
        FLIGHTS
)
SELECT city
FROM AllCities
WHERE city NOT IN (SELECT dest_city FROM DirectCities)
      AND city NOT IN (SELECT dest_city FROM OneStopCities)
	  AND city != 'Seattle WA'
ORDER BY city;
