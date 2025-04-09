SELECT
    name AS carrier
FROM
    CARRIERS
WHERE
    cid IN (
        SELECT DISTINCT
            carrier_id
        FROM
            FLIGHTS
        WHERE
            origin_city = 'Seattle WA'
            AND dest_city = 'New York NY'
    )
ORDER BY
    carrier;
