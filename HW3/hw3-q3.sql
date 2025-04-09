SELECT
    origin_city,
    COALESCE(CAST(SUM(CASE WHEN actual_time < 90 THEN 1 ELSE 0 END) AS FLOAT) / SUM(CASE WHEN canceled = 0 THEN 1 ELSE 0 END) * 100, 0) AS percentage
FROM
    FLIGHTS
GROUP BY
    origin_city
ORDER BY
    percentage,
    origin_city;
