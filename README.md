# money-transfer

<b>Prerequisites</b>
  * Java 8+
  * Maven 3
  <p>
<b>Installation </b>

 * Build from source
 
` mvn clean package `
 * Run
 
` java -jar money-transfer-1.0-SNAPSHOT-jar-with-dependencies.jar `
* Check localhost:8080

<b> API Quick Guide </b>

Http Method | Path | Example body
------------ | -------------|-------------
GET | /accounts/list |
GET | /accounts/{id} |
PUT | /accounts/create | ``` { "amount" : "500" } ```
DELETE | /accounts/{id} |
POST | /accounts/deposit | ```{	"id" : "1",	"amount" : "123" }```
POST | /accounts/withdraw | ```{	"id" : "1",	"amount" : "123" }```
POST | /accounts/transfer | ```{ "fromId" : "1", "toId" : "2", "amount" : "100" } ```
