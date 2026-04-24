# 🏫 Smart Campus Sensor & Room Management API

A RESTful API built with JAX-RS (Jersey 2) deployed on Payara Server for managing campus rooms and sensors.

---
🔗 Demo Link : https://drive.google.com/file/d/1dc5IoeoMKmy4QMDQr05b8Li3NuFbuVW2/view?usp=sharing

## API Overview

- **Base URL** → `http://localhost:8080/smartCampusApi/api/v1`
- **Architecture** → REST (JAX-RS / Jersey 2)
- **Data Format** → JSON
- **Storage** → In-memory HashMap (no database)

---

## 🔗 Endpoints 

### 🚪 Room Endpoints

- `GET /api/v1/rooms` — Retrieve a list of all rooms
- `POST /api/v1/rooms` — Create a new room
- `GET /api/v1/rooms/{roomId}` — Retrieve a specific room by ID
- `DELETE /api/v1/rooms/{roomId}` — Delete a room (blocked with 409 if sensors are still assigned)

### 📡 Sensor Endpoints

- `GET /api/v1/sensors` — Retrieve a list of all sensors (supports optional `?type=` filter)
- `POST /api/v1/sensors` — Register a new sensor (validates roomId exists)
- `GET /api/v1/sensors/{sensorId}` — Retrieve a specific sensor by ID
- `DELETE /api/v1/sensors/{sensorId}` — Remove a sensor and unlink it from its room

### 📊 Sensor Reading Endpoints

- `GET /api/v1/sensors/{sensorId}/readings` — Retrieve the full reading history for a sensor
- `POST /api/v1/sensors/{sensorId}/readings` — Add a new reading (blocked with 403 if sensor is in MAINTENANCE or OFFLINE status)

---

## ✨ Features

- RESTful API built with JAX-RS (Jersey 2) and deployed on Payara Server
-  Discovery endpoint at `GET /api/v1` implementing HATEOAS with navigational resource links
- Full room management including creation, retrieval, and safe deletion with sensor conflict detection
- Full sensor management with referential integrity validation against existing rooms
- Automatic update of a sensor's `currentValue` when a new reading is posted
- Custom ExceptionMappers for 409, 422, 403, and 500 error responses returning structured JSON
- Global safety net mapper preventing raw stack traces from being exposed to clients
- All data stored in-memory using HashMaps and ArrayLists — no database required

---

## 📝 Conceptual Report

### Part 1 — Project & Application Configuration

#### Question 1

In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions.

- JAX-RS creates a brand new instance of every Resource class for each incoming HTTP request which is called the per request lifecycle. It means any data stored as an instance field inside a resource class would be completely wiped after every single request which makes it impossible to persist any state between requests.

- To resolve this issue, all data is stored in the DataStore singleton which is a class with one shared instance accessed through getInstance() that lives for the entire server process lifetime. Every resource class calls DataStore.getInstance() and operates on the same shared HashMaps, meaning data persists across all the requests for as long as the server is running.

- For the thread safety in a production environment, ConcurrentHashMap and synchronized write blocks would be used to prevent race conditions when multiple requests attempt to modify the same data at the same time.

#### Question 2

Why is the provision of "Hypermedia" (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

- HATEOAS is used to embed navigational links directly inside API responses so that clients can discover and traverse the API dynamically, without having to memorise URLs from external static documentation.

The benefits of HATEOAS over static documentation can be mentioned as follows:

- If a URL in a server side changes, clients reading links from responses adapt automatically without any code changes on the client's side.
- A new developer can explore the entire API starting from one entry point by simply following the links provided, exactly like browsing a website.
- It separates the client from the server's internal URL structure, which allows the server to evolve independently without breaking existing clients.
- It makes the API self describing. Each response tells the client what actions and resources are available next.

---

### Part 2 — Room Management

#### Question 1

When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.

- Returning the full room objects give the client all the data needed in a single request. It reduces the network round trips and improves the user experience. However, for a large scale dataset this wastes the bandwidth as the client may only need basic information such as IDs.

- Returning only the IDs keep the payload small. However, it forces the client to make additional requests to fetch the details of each room which becomes expensive at scale. The best practice in the current industry is to return a lightweight representation of a summary in list responses containing only key fields such as id and name while the full detailed representation is available via the individual GET/{roomID} endpoint. This helps to balance the bandwidth efficiency with the usability.

#### Question 2

Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.

- Yes, DELETE is idempotent. Calling the same operation on multiple occasions produces the same server state despite of how many times it is called.

- In this project the first DELETE /rooms/HALL-01 finds the room, then removes it and returns HTTP 200 and the second DELETE /rooms/HALL-01 finds nothing and returns HTTP 404.

- The state of the server is similar after both of these calls. The room does not exist. The HTTP response code varies between the two calls but that does not violate idempotency as idempotency is defined in the terms of server state not the response codes.

---

### Part 3 — Sensor Operations & Linking

#### Question 1

We explicitly use the @Consumes(MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?

- The @Consumes(MediaType.APPLICATION_JSON) annotation declares a strict contract that this endpoint will only accept requests with the Content Type header set to application/json.

- If a client sends requests with text/plain or application/xml, JAX-RS automatically rejects the request with HTTP 415 Unsupported Media Type before the Java method code even executes.

#### Question 2

You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/v1/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?

- The query parameter approach GET /api/v1/sensors?type=CO2 is preferred over the path segment approach GET /api/v1/sensors/type/CO2. The reasons for this can be mentioned as follows:

- Path segments are meant to identify a specific resource by its unique identity. Query parameters are designed to express optional criteria or filters applied to a collection.
- Multiple filters compose naturally and readably with query parameters whereas path-based filtering becomes deeply nested, unreadable, and structurally rigid.
- Query parameters are optional. Therefore, the endpoint works with or without them. Path segments make parameters mandatory by structure.
- Query parameter URLs are more cache friendly.

---

### Part 4 — Deep Nesting with Sub-Resources

#### Question 1

Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path in one massive controller class?

- The sub-resource locator pattern is implemented in SensorResource where the getSensorReadings() method returns an instance of SensorReadingResource instead of handling the request itself. JAX-RS then assigns all the processing of the /readings path to that dedicated class.

- The sub resource locator pattern provides several architectural benefits. It enforces separation of concerns. All the logic of reading history is in SensorReadingResource instead of cluttering SensorResource with irrelevant tasks. It makes it also easier to manage. A real campus API might have a lot of sub resources per sensor including readings, alerts and maintenance history. Putting all these endpoints into a single class would make it an unmanageable class that is difficult to navigate. Additionally, each sub resource class can be unit tested directly without a fully fledged HTTP server. It also can be reused under other parent paths with slight modifications which encourages reuse of code across the API.

---

### Part 5 — Advanced Error Handling, Exception Mapping & Logging

#### Question 1

Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

- HTTP 404 means that the URL that was requested does not correspond to any resource in the system. The problem is the request path while HTTP 422 means the URL is valid, the request reached the correct endpoint and the JSON body is syntactically correct but the logical content inside the payload is invalid.

- HTTP 422 is semantically more accurate because the problem is not that the client requested a wrong URL. The problem is that the data inside the payload violates a business rule by referencing to a non existent resource. This helps API clients to write more accurate error handling because their request data can be fixed rather than their URL.

#### Question 2

From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

- Class names and package structures are revealed which allows the attackers to map the internal architecture of the application.
- Exact line numbers are exposed which will help to pinpoint precisely where the errors occur. That will help the attackers create targeted exploits against specific code paths.
- Library names and version numbers appear in stack traces which allows the attackers to cross reference them against known CVE databases to find exploitable vulnerabilities in specific version.
- Internal logic flow is exposed through the call stack which reveals how the application processes data and identifying possible injection points or logic flaws.
- File System paths can sometimes appear in stack traces as well. Therefore, it may reveal server's directory structure to attackers.

#### Question 3

Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?

- Using JAX-RS filters for logging is architecturally superior to placing Logger.info() calls inside every individual resource method. It eliminates code duplication and ensures cleaner design. With filters a single class can handle logging for all endpoints while manual logging requires codes to be repeated inside every method. This approach also helps to keep resource methods focused on their main responsibility which is handling business logic. Filters also guarantee consistency across the application. Logging is automatically applied to every request and response helping to remove the risk of developers forgetting to add logs to the new endpoints. Any changes to the logging format can also be done in one place which helps to make the updates much easier.
