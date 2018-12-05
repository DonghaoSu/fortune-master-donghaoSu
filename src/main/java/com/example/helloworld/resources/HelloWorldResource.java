package com.example.helloworld.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.sql.*;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Path("/hello-world") // This is where the HTTP URL path is set
@Produces(MediaType.APPLICATION_JSON)
public class HelloWorldResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(HelloWorldResource.class);



    private final AtomicLong counter;

    private int outputId;
    private String outputContent;
    public HelloWorldResource() {
        this.counter = new AtomicLong();
    }

    /**
     * HTTP GET
     * invoke by:
     * curl -XGET localhost:8080/hello-world?name=aaa
     * curl -XGET localhost:8080/hello-world
     *
     * @param name Note that the URL parameter `name` you see above gets parsed to the `name` method parameter below.
     * @return
     */
    @GET
    public Saying sayHello(@QueryParam("name") Optional<String> name) {

        try{
            // Get database connection
            Connection conn = Database.getConnection();

            // Execute a query
            System.out.println("Creating statement...");

            Statement stmt = conn.createStatement();
            String sql;
            sql = "SELECT * from fortune";
            ResultSet rs = stmt.executeQuery(sql);

            // Extract data from result set

            while(rs.next()){
                //Retrieve by column name
                int outputId  = rs.getInt("id");
                String outputContent = rs.getString("content");


                //Display values
                System.out.print("ID: " + outputId);
                System.out.print(", Content: " + outputContent);
                return new Saying(outputId, outputContent);
            }

            // Clean-up environment
            rs.close();
            stmt.close();
            conn.close();
        }catch(SQLException se){
            //Handle errors for JDBC
            se.printStackTrace();
        }catch(Exception e){
            //Handle errors for Class.forName
            e.printStackTrace();
        }

        System.out.println("Goodbye!");
        return new Saying(counter.incrementAndGet(), name.isPresent() ? "Hello: " + name.get() : "Hello: jason");
    }

    /**
     * HTTP POST
     * invoke by:
     * curl -XPOST localhost:8080/hello-world -H "Content-Type: application/json" -d '{"id":333}'
     * Note above that `-d` parameter for `curl` specifies the JSON data sent as payload to the HTTP server
     *
     * @param saying Note for HTTP POST, the payload data above are parsed into the `saying` method parameter below.
     */
    @POST
    public void receiveHello(@Valid Saying saying) throws SQLException{
        LOGGER.info("Received a saying: {}\n\n", saying);

        // Get database connection
        Connection conn = Database.getConnection();

    }

    /**
     * HTTP DELETE
     * curl -XDELETE 'localhost:8080/hello-world?id=1234'
     * Note that the URL parameter `id` you see above gets parsed to the `id` method parameter below.
     * @param id
     */
    @DELETE
    public void deleteIt(@QueryParam("id") Optional<String> id) {
        if (id.isPresent()) {
            LOGGER.info("delete object with id:=" + id.get());
            System.out.println("delete object with id:=" + id.get());
        } else {
            LOGGER.info("delete. id not supplied");
        }
    }
}
