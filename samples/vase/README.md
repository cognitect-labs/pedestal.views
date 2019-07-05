# vase

## Run the Sample

1. Start a new REPL: `lein repl`
2. Start your service in dev-mode: `(def dev-serv (run-dev))`

## Create a User or Three

    curl -X POST -H "Content-Type: application/json" -d '{"payload": [{"user/userId": 500, "user/userEmail": "mtnygard@example.com"}]}' http://localhost:8080/api/sample.vase/v1/users

    curl -X POST -H "Content-Type: application/json" -d '{"payload": [{"user/userId": 501, "user/userEmail": "ddeaguiar@example.com"}]}' http://localhost:8080/api/sample.vase/v1/users

    curl -X POST -H "Content-Type: application/json" -d '{"payload": [{"user/userId": 502, "user/userEmail": "ohpauleez@example.com"}]}' http://localhost:8080/api/sample.vase/v1/users

## See the users

    curl http://localhost:8080/api/sample.vase/v1/users
