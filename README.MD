# CRUD operation on Glacier

### As of right now only retrieval API is available I will add rest soon.

## You will need **Java 1.8** and **Maven** to run this service as this makes use of spring boot.

After building code with maven and running the Jar, you will need to make your request in the below pattern :

`http://localhost:8080/getArchive`

the body will have the below structure :

```
{
	"archiveId" : "<archiveId>",
	"vaultName" : "<vaultName>",
	"accesskey" : "<AWSAccessKey>",
	"secretKey" : "<AWSSecretKey>",
	"retrieval" : "<Retrieval-Method>"
}
```

* `<archiveId>` - replace this with the archive ID you want to extract from glacier.
* `<vaultName>` - replace this with the name of your vault.
* `<AWSAccessKey>` - replace with your AWS access key which has the right to make the retrieval request to Glacier.
* `<AWSSecretKey>` -  replace with your AWS secret key which has the right to make the retrieval request to Glacier.
* `<Retrieval-Method>` - Replace with the retrieval method that you want to use, just be vary when you use Bulk, or Expedited mode instead of Standard you should have enough provisions in place otherwise the request will fail.

Note : 
* if a successfull request is made you will get a JSON response containing the JobId provided by Glacier. A describe job will be initiated on this jobID on another thread to download the file when it ready to be downloaded from Glacier. And you will get a JSON reponse of form - 
	```
	{
    "status": "SUCCESS",
    "message": "The retrieval job has been initiated, once the job completes the Archive will be available for download",
    "error": false,
    "jobId": "<JobID>",
    "errorMessage": ""
	}
	```
	
* Once the file has been downloaded you can download it using the downloadfile API - This might have some issues.... will solve when I get time.

* You can keep an eye on the Console for getting if the job failed the thread will stop if the job failed and print result on to the console with the jobID
