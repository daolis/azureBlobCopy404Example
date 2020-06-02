# Reproduce azure copy error 404 if ther is a %20 in the blobname

## Prerequisites

Create a BlobStorage account with the following structure 

* `sourcecontainer` 
  * `01-testfile.txt`
  * `02-test%20file.txt` 
* `targetcontainer` - empty

source should look like this
![source][source.png]

Set the environment variables with accountname and password

* `TEST_ACCOUNT_NAME`
* `TEST_ACCOUNT_KEY` 

Change the constant `WITH_WORKAROUND` to true to use the workaround.
