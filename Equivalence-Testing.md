# Equivalence Partition Documentation

This document defines the equivalence partitions for each API endpoint in our service, maps those partitions directly to the tests that cover them.

---

## 1. POST `/logs/upload`

### Inputs

* `clientId` (String, required)
* `file` (.log file, required)

### A. `clientId` Input Partitions

| Partition ID | Description                    | Expected Behavior |
| ------------ | ------------------------------ | ----------------- |
| CID-1        | Valid non-empty string         | 200 OK            |
| CID-2        | Blank or whitespace            | 400 Bad Request   |
| CID-3        | Missing or null                | 400 Bad Request   |
| CID-4        | Very long but non-empty string | 200 OK            |
| CID-5        | Contains special characters    | 200 OK            |

### B. `file` Input Partitions

| Partition ID | Description                  | Expected Behavior |
| ------------ | ---------------------------- | ----------------- |
| FILE-1       | Valid `.log` file, non-empty | 200 OK            |
| FILE-2       | Missing file                 | 400 Bad Request   |
| FILE-3       | Empty file                   | 400 Bad Request   |
| FILE-4       | Wrong file extension         | 400 Bad Request   |
| FILE-5       | Missing filename             | 400 Bad Request   |
| FILE-6       | I/O or processing error      | 400/500           |

### Partition → Test Mapping

| Partition ID   | Test Name                                               |
| -------------- | ------------------------------------------------------- |
| CID-1 + FILE-1 | `testUploadLogSuccess`                                  |
| FILE-2         | `testUploadLogMissingFilePart`                          |
| CID-2          | `testUploadLogMissingClientId`                          |
| CID-3          | `testUploadLogReturnsBadRequestWhenNullClientIdIsGiven` |
| FILE-4         | `testUploadLogWrongExtension`                           |
| FILE-5         | `testUploadLogFileReturnsBadRequestFromMissingFilename` |
| FILE-6         | `testUploadLogException`                                |
| CID-4          | `testUploadLogStillAcceptsClientIdWithLongLength`       |

---

## 2. GET `/logs/statusCodeCounts`

### Inputs

* `clientId` (query parameter)

### Equivalence Partitions

| Partition ID | Description                       | Expected Behavior   |
| ------------ | --------------------------------- | ------------------- |
| SCC-1        | ClientId exists + log data exists | 200 OK              |
| SCC-2        | ClientId exists but no log data   | 200 OK (empty JSON) |
| SCC-3        | ClientId does not exist           | 404 Not Found       |
| SCC-4        | Missing clientId                  | 200 OK (empty JSON) |

### Partition → Test Mapping

| Partition ID | Test Name                                              |
| ------------ | ------------------------------------------------------ |
| SCC-1        | `testGetStatusCodeCountsFound`                         |
| SCC-2        | `testGetStatusCodeCountsEmptyDatabaseReturnsEmptyJson` |
| SCC-3        | `testGetStatusCodeCountsNotFound`                      |
| SCC-4        | Covered by endpoint behavior                           |

---

## 3. GET `/timeseries/requests/{clientId}`

### Inputs

* `clientId` (path parameter)

### Equivalence Partitions

| Partition ID | Description                 | Expected Behavior   |
| ------------ | --------------------------- | ------------------- |
| REQ-1        | Valid clientId with data    | 200 OK              |
| REQ-2        | Valid clientId with no data | 200 OK (empty JSON) |
| REQ-3        | Invalid clientId            | 404 Not Found       |
| REQ-4        | Missing path parameter      | 404 Not Found       |

### Partition → Test Mapping

| Partition ID | Test Name                                                                         |
| ------------ | --------------------------------------------------------------------------------- |
| REQ-1        | `testGetRequestCountsByHourReturnsDataSuccessfully`                               |
| REQ-2        | `testGetRequestCountsByHourReturnsEmptyJsonWhenNoDataButClientExists`             |
| REQ-3        | `testGetRequestCountsByHourReturnsNotFoundWithMessageForInvalidOrMissingClientId` |
| REQ-4        | Default Spring behavior                                                           |

---

## 4. GET `/timeseries/error-counts/{clientId}`

### Inputs

* `clientId` (path parameter)

### Equivalence Partitions

| Partition ID | Description                 | Expected Behavior   |
| ------------ | --------------------------- | ------------------- |
| ERR-1        | Valid clientId with data    | 200 OK              |
| ERR-2        | Valid clientId with no data | 200 OK (empty JSON) |
| ERR-3        | Invalid clientId            | 404 Not Found       |
| ERR-4        | Missing path parameter      | 404 Not Found       |

### Partition → Test Mapping

| Partition ID | Test Name                                                           |
| ------------ | ------------------------------------------------------------------- |
| ERR-1        | `testGetErrorCountsByHourReturnsDataSuccessfully`                   |
| ERR-2        | `testGetErrorCountsByHourReturnsEmptyJsonWhenNoDataButClientExists` |
| ERR-3        | `testGetErrorCountsByHourReturnsNotFoundForInvalidClientId`         |
| ERR-4        | Default Spring behavior                                             |

---

## 5. GET `/suspicious-ips/{clientId}`

### Inputs

* `clientId` (path parameter)

### Equivalence Partitions

| Partition ID | Description                            | Expected Behavior |
| ------------ | -------------------------------------- | ----------------- |
| IP-1         | Valid clientId + suspicious IPs exist  | 200 OK            |
| IP-2         | Valid clientId + no suspicious IPs     | 200 OK (message)  |
| IP-3         | Invalid clientId                       | 404 Not Found     |
| IP-4         | Missing path parameter                 | 404 Not Found     |
| IP-5         | Valid clientId with special characters | 200 OK            |

### Partition → Test Mapping

| Partition ID | Test Name                                                                                                   |
| ------------ | ----------------------------------------------------------------------------------------------------------- |
| IP-1         | `testGetSuspiciousIpsReturnsDataSuccessfully`                                                               |
| IP-2         | `testGetSuspiciousIpsReturnsEmptyWhenNoData`, `testGetSuspiciousIpsEmptyDatabaseReturnsNoSuspiciousMessage` |
| IP-3         | `testGetSuspiciousIpsReturnsEmptyWhenClientDoesNotExist`                                                    |
| IP-5         | `testGetSuspiciousIpsWithSpecialCharactersInPath`                                                           |
| IP-4         | Default Spring behavior                                                                                     |

---

## 6. GET `/top-endpoints/{clientId}`

### Inputs

* `clientId` (path parameter)

### Equivalence Partitions

| Partition ID | Description                          | Expected Behavior   |
| ------------ | ------------------------------------ | ------------------- |
| TOP-1        | Valid clientId with endpoint data    | 200 OK              |
| TOP-2        | Valid clientId with no endpoint data | 200 OK (empty JSON) |
| TOP-3        | Invalid clientId                     | 200 OK (empty JSON) |
| TOP-4        | Missing path parameter               | 404 Not Found       |

### Partition → Test Mapping

| Partition ID | Test Name                                                |
| ------------ | -------------------------------------------------------- |
| TOP-1        | `testGetTopEndpointsReturnsDataSuccessfully`             |
| TOP-2        | `testGetTopEndpointsReturnsEmptyJsonWhenNoData`          |
| TOP-3        | `testGetTopEndpointsWithInvalidClientIdReturnsEmptyJson` |
| TOP-4        | Default Spring behavior                                  |

---
