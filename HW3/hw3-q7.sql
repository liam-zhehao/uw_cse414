SELECT DISTINCT
    c.name AS carrier
FROM
    CARRIERS c
JOIN
    FLIGHTS f ON c.cid = f.carrier_id
WHERE
    f.origin_city = 'Seattle WA'
    AND f.dest_city = 'New York NY'
ORDER BY
    carrier;
