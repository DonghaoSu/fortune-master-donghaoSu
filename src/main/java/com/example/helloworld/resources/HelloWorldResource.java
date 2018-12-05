package com.example.helloworld.resources;

import io.dropwizard.jersey.errors.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
            sql = "SELECT * from fortune order by RAND() LIMIT 1";
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
    public Response receiveHello(@Valid Saying saying) throws SQLException{
        LOGGER.info("Received a saying: {}\n\n", saying);

        // Get database connection
        Connection conn = Database.getConnection();
        long id = saying.getId();
        String content = saying.getContent();

        Saying item = get(id);
        if (item == null) {
            String postQuery = "INSERT INTO fortune(id, content) VALUES (" + "?,?)";

            PreparedStatement preparedStatement = conn.prepareStatement(postQuery);

            preparedStatement.setInt(1, (int) id);
            preparedStatement.setString(2, content);

            preparedStatement.execute();

            return Response
                    .status(202)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(new Saying(id, content))
                    .build();

        } else {
            return Response
                    .status(404)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(new ErrorMessage(404,
                            "[FAIL] Insert data FAILED! The ID that you trying to insert is already in the database!"))
                    .build();
        }
    }

    /**
     * HTTP DELETE
     * curl -XDELETE 'localhost:8080/hello-world?id=1234'
     * Note that the URL parameter `id` you see above gets parsed to the `id` method parameter below.
     * @param id
     */
    @DELETE
    //@Path("/fortunes/{id}")
    public Response deleteIt(@QueryParam("id") Optional<String> id) throws SQLException {
        if (id.isPresent()) {
            LOGGER.info("delete object with id:=" + id.get());
            System.out.println("delete object with id:=" + id.get());

            // Get database connection
            Connection conn = Database.getConnection();

            Saying item = get(Integer.parseInt(id.get()));
            if (item != null) {
                String deleteQuery = "delete from fortune where id=?";

                PreparedStatement preparedStatement = conn.prepareStatement(deleteQuery);

                preparedStatement.setInt(1, Integer.parseInt(id.get()));

                preparedStatement.execute();

                return Response
                        .status(202)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .entity(item)
                        .build();

            } else {
                return Response
                        .status(404)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .entity(new ErrorMessage(404,
                                "[FAIL] Delete data FAILED! This ID is not in the database!"))
                        .build();
            }
        } else {
            LOGGER.info("delete. id not supplied");
            return Response
                    .status(404)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(new ErrorMessage(404,
                            "[FAIL] Delete data FAILED! This ID is not supplied!"))
                    .build();
        }
    }

    private Saying get(long id) throws SQLException {
        // Get database connection
        Connection conn = Database.getConnection();

        // Execute a query
        System.out.println("Creating statement...");

        Statement stmt;
        stmt = conn.createStatement();
        String sql = "SELECT * from fortune where id=" + id;
        ResultSet rs = stmt.executeQuery(sql);

        while(rs.next()){
            //Retrieve by column name
            int outputId  = rs.getInt("id");
            String outputContent = rs.getString("content");

            System.out.print(", Content: " + outputContent);
            return new Saying(outputId, outputContent);
        }
        return null;
    }
}
