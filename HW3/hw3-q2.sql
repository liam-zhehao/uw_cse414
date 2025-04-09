SELECT DISTINCT
    origin_city AS city
FROM
    FLIGHTS
WHERE
    canceled = 0
GROUP BY
    origin_city
HAVING
    MAX(actual_time) < 240
ORDER BY
    origin_city;
