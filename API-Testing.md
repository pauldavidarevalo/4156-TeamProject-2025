# API Testing

This document describes the API endpoints and their expected behavior for testing purposes.

---

## 1. `POST logs/upload`

| Test Description              | Request Body                              | Expected Response Body    | Expected Status Code |
|-------------------------------|-------------------------------------------|---------------------------|----------------------|
| Valid .log file and client id | JSON object with .log file and client id  | JSON object with message "Log file processed successfully." | 200 OK               |
| No .log file                  | JSON object with only a client id         |  |  |
| No client id                  | JSON object with only a .log file         |  |  |
| Invalid .log file             | JSON object with a different type of file |  |  |
---

## 2. `GET logs/statusCodeCounts`

| Test Description          | Request Body                                  | Expected Response Body                              | Expected Status Code |
|---------------------------|-----------------------------------------------|-----------------------------------------------------|----------------------|
| Valid client id           | JSON object with client id as query parameter |                                                     | 200 OK               |
| Client id not in database | JSON object with invalid client id            | JSON object with message "Error clientId not found." | 404 Not Found        |
| No parameter              | JSON object without a parameter               | Empty JSON object                                   | 200 OK               |

---

## 3. `GET timeseries/requests/{clientId}`

| Test Description          | Request Body                                 | Expected Response Body | Expected Status Code |
|---------------------------|----------------------------------------------|----------------------|--------------------|
| Valid client id           | JSON object with client id as path parameter |                        | 200 OK               |
| Client id not in database | JSON object with invalid client id           | Empty JSON object      | 200 OK               |
| No path parameter         | JSON object without a path parameter         | Empty JSON object      | 200 OK               |


---

## 4. `GET timeseries/error-counts/{clientId}`

| Test Description          | Request Body | Expected Response Body | Expected Status Code |
|---------------------------|--------------|----------------------|--------------------|
| Valid client id           | JSON object with client id as query parameter |                        | 200 OK               |
| Client id not in database | JSON object with invalid client id            | Empty JSON object      | 200 OK               |
| No path parameter         | JSON object without a parameter               | Empty JSON object      | 200 OK               |


---

## 5. `GET suspicious-ips/{clientId}`

| Test Description          | Request Body                                 | Expected Response Body                               | Expected Status Code |
|---------------------------|----------------------------------------------|------------------------------------------------------|----------------------|
| Valid client id           | JSON object with client id as path parameter |                                                      | 200 OK               |
| Client id not in database | JSON object with invalid client id           | Empty JSON object                                    | 200 OK               |
| No path parameter         | JSON object without a path parameter         | JSON object with timestamp, status, error, and path. | 404 Not Found        |


---

