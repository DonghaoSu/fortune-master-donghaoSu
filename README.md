FORTUNE

# Running The Application

To test the example application run the following commands.

* To create the example, package the application using [Apache Maven](https://maven.apache.org/) from the directory of the project.

        mvn clean package

* To run the server run.

        java -jar target/dropwizard-example-1.4.0-SNAPSHOT.jar server example.yml

* To GET a random fortune:
	curl -XGET localhost:8080/fortune
	
* To POST a fotune:
	curl -XPOST localhost:8080/{YOUR POST CONTENT}
	
* To DELETE a fortune:
	curl -XDELETE localhost:8080/cortunes/{THE ID OF THE FORTUNE THAT YOUR WANT TO DELETE}
