# Neonomics Assignment

## Intro:
We have an API towards application developers, which returns information about all the banks which are available for the application.

The response from the API looks like this:
```json
[
  {
    "bic":"DOLORENOR9XXX",
    "name":"Bank Dolores",
    "countryCode":"NO",
    "auth":"ssl-certificate",
    "products":[
      "accounts",
      "payments"
    ]   
  },
  {
    ...
  }
]
```
There are two version of the API:

- `/v1/banks/all` - implementation is based on the static file, which is locally available
- `/v2/banks/all` - new version of the API, which will need to read the data from the remote servers

## Challenge:
1. Add unit tests for both API versions.

2. API response:
    - v1 should return: name, id, countryCode and product
    - v2 should return: name, id, countryCode and auth

3. Complete the implementation of the `/v2/banks/all` endpoint, by implementing `BanksRemoteCalls.handle(Request request, Response response)` method.
The respective configuration file is `banks-v2.json`. Implementation needs to use the data from the configuration file,
and for each bank retrieve the data from the remote URL specified. You will need to add HTTP client of your choice to the project. 
You can find the mock implementation for the remote URLs in the MockRemotes class. 

4. Add pagination and filtering to v1 and v2 of the banks APIs.
- Non-Functional requirements
  - The client should be able to define the page size, default being 5.
  - A filter for countrycode, product and other values should be added.

5. Refactor the existing code base.
    - Feel free to add comments to the code to clarify the changes you are making.
## Sending in the assignment
- You may send in the assignment on a git repository or as a zip.


