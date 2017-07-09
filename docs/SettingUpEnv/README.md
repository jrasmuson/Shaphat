1. With Java installed on the computer build the jar with ./gradlew buildZip
![](assets/README-71201.png)

2. After setting up AWS account, create a role
![](assets/README-f3cdb.png)

3. Give permissions for kinesis, rekognition, dynamo, and cloudwatch(for logs)
![](assets/README-1e85f.png)

4. Review
![](assets/README-0d0d0.png)

5. Startup kinesis with name shaphat_test_stream
![](assets/README-13e14.png)
![](assets/README-e6395.png)

6. Setup dynamo with name shaphat_test(partition key hostname sort_key imageUrl) can change capacity to 1
![](assets/README-8ad8e.png)

7. Configure lambda use blank function
![](assets/README-ae014.png)

8. Configure trigger as kinesis shaphat_test_stream
![](assets/README-8ff91.png)

9. Upload Shaphat-1.0.zip jar file and setup env variable(table_name: shaphat_test_table)
![](assets/README-17ba1.png)

10. Setup configuration to use java8, the created role and setup handler to be LambdaHandler::recordHandler
![](assets/README-6f6e0.png)

11. Configure test event to kinesis and set kinesis data to "ew0KICAgICAgICAiaG9zdG5hbWUiOiJmbG93ZXJtZWFuaW5nLmNvbSIsDQogICAgICAgICJpbWFnZVVybCI6Imh0dHA6Ly93d3cuZmxvd2VybWVhbmluZy5jb20vZmxvd2VyLXBpY3MvT3JjaGlkLU1lYW5pbmcuanBnIg0KfQ" which base64decodes to { "hostname":"flowermeaning.com", "imageUrl":"http://www.flowermeaning.com/flower-pics/Orchid-Meaning.jpg" }
![](assets/README-70e1c.png)

12. Save and test and should get back the log messages that the url and service were called for this one record
![](assets/README-91a28.png)

13. Should then be able to see the row in DynamoDb
![](assets/README-67d42.png)

14. Can then use any method to put records in kinesis such as the testScripts/putRecord.sh
![](assets/README-ba8dc.png)
