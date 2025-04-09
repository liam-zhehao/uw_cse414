WITH FlightDurations AS (
    SELECT
        origin_city,
        dest_city,
        actual_time,
        RANK() OVER (PARTITION BY origin_city ORDER BY actual_time DESC) AS rn
    FROM
        FLIGHTS
)
SELECT DISTINCT
    origin_city,
    dest_city,
    actual_time AS time
FROM
    FlightDurations
WHERE
    rn = 1
ORDER BY
    origin_city, 
    dest_city;
