## How to run repository locally.

Step 1: clone the repository/download the zip file

Step 2 : Make sure you have java 17 installed on your local machine.

Step 3 : Run the main class from here :url_shortner/src/main/java/com/example/url_shortner/UrlShortnerApplication.java

step 4 : Apis will be exposed at "/" path.

## Testing /shorten api in postman

Step 1: The url to test this endpoint locally will be http://localhost:8080/shorten

Step 2: The method type is post.

Step 3 : body signature is {"url": "yourdomain"};


## Testing /redirect api in postman

Step 1 :  The url to test this endpoint locally will be http://localhost:8080/redirect?code="yourshortcode"

Step2 : replace the yourshort code with actual short code.

## To run the tests:

  Run the class present in file. url_shortner/src/test/java/com/example/url_shortner/UrlShortnerApplicationTests.java

  If you see error that no such database exist please delete application.properties file present in test folder and try again.

  


