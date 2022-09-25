### To use the project

#### Deploy manually by creating Lambda function from AWS console
1. Create DynamoDB table for Book table
2. Build the app using 
```
mvn clean package
```
3. Upload the jar file to the lambda function
4. Set handler method in Lambda function

#### Deploy automatically using serverless
Run 

```
sls deploy
```