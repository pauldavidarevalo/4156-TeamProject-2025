# API Testing

This document describes the API endpoints and their expected behavior for testing purposes.

---

## 1. `POST logs/upload`

| Test Description         | Request Body                              | Expected Response Body    | Expected Status Code |
|--------------------------|-------------------------------------------|---------------------------|----------------------|
| Valid .log file and client id | JSON object with .log file and client id  | JSON object with message "Log file processed successfully." | 200 OK               |
| No .log file             | JSON object with only a client id         |  |  |
| No client id             | JSON object with only a .log file         |  |  |
| Invalid file             | JSON object with a different type of file |  |  |

- <img src="/images/Screenshot upload OK.png" alt="alt text" />
---

## 2. `GET logs/statusCodeCounts`

| Test Description          | Request Body                                  | Expected Response Body                               | Expected Status Code |
|---------------------------|-----------------------------------------------|------------------------------------------------------|----------------------|
| Valid client id           | JSON object with client id as query parameter | JSON object with counts per status code              | 200 OK               |
| Client id not in database | JSON object with invalid client id            | JSON object with message "Error clientId not found." | 404 Not Found        |
| No parameter              | JSON object without a parameter               | Empty JSON object                                    | 200 OK               |

- <img src="/images/Screenshot StatusCodeCounts OK.png" alt="alt text" />
- <img src="/images/Screenshot statusCodeCounts Error.png" alt="alt text" />
---

## 3. `GET timeseries/requests/{clientId}`

| Test Description          | Request Body                                 | Expected Response Body                               | Expected Status Code |
|---------------------------|----------------------------------------------|------------------------------------------------------|--------------------|
| Valid client id           | JSON object with client id as path parameter | JSON object with date and how many requests          | 200 OK               |
| Client id not in database | JSON object with invalid client id           | Empty JSON object                                    | 200 OK               |
| No path parameter         | JSON object without a path parameter         | JSON object with timestamp, status, error, and path. | 404 Not Found                |

- <img src="/images/Screenshot requests OK.png" alt="alt text" />

---

## 4. `GET timeseries/error-counts/{clientId}`

| Test Description          | Request Body | Expected Response Body                                                       | Expected Status Code |
|---------------------------|--------------|------------------------------------------------------------------------------|--------------------|
| Valid client id           | JSON object with client id as query parameter | JSON object with a timestamp and the error counts for codes "4xx" and "5xx". | 200 OK               |
| Client id not in database | JSON object with invalid client id            | Empty JSON object                                                            | 200 OK               |
| No path parameter         | JSON object without a parameter               | JSON object with timestamp, status, error, and path.                         | 404 Not Found               |

- <img src="/images/Screenshot error-counts.png" alt="alt text" />
---

## 5. `GET suspicious-ips/{clientId}`

| Test Description          | Request Body                                 | Expected Response Body                                                                                                 | Expected Status Code |
|---------------------------|----------------------------------------------|------------------------------------------------------------------------------------------------------------------------|----------------------|
| Valid client id           | JSON object with client id as path parameter | JSON object with a list of objects containing errorCount, ipAddress, and hourWindow which have the highest errorCount. | 200 OK               |
| Client id not in database | JSON object with invalid client id           | Empty JSON object                                                                                                      | 200 OK               |
| No path parameter         | JSON object without a path parameter         | JSON object with timestamp, status, error, and path.                                                                   | 404 Not Found        |

- <img src="/images/Screenshot suspicious-ips.png" alt="alt text" />
---

