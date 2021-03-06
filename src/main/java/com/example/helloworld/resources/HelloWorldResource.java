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

@Path("/") // This is where the HTTP URL path is set
@Produces(MediaType.APPLICATION_JSON)
public class HelloWorldResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(HelloWorldResource.class);

    private final AtomicLong counter;

    private int outputId;
    private String outputContent;
    public HelloWorldResource() {
        this.counter = new AtomicLong();
    }

    private long idOffset;
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
    @Path("fortune")
    public String sayHello(@QueryParam("name") Optional<String> name) {

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
                long outputId  = rs.getLong("id");
                String outputContent = rs.getString("content");


                //Display values
                System.out.print("ID: " + outputId);
                System.out.print(", Content: " + outputContent);
                return outputContent;
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
        return null;
        //return new Saying(counter.incrementAndGet(), name.isPresent() ? "Hello: " + name.get() : "Hello: jason");
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
    @Path("{msg}")
    public Response receiveHello(@PathParam("msg") String msg) throws SQLException{

        // Get database connection
        Connection conn = Database.getConnection();

        // Find the largest id in database
        Statement stmt = conn.createStatement();
        String sql;
        sql = "SELECT * from fortune WHERE id=(\n" +
                "    SELECT max(id) FROM fortune\n" +
                "    )";
        ResultSet rs = stmt.executeQuery(sql);

        while(rs.next()) {
            //Retrieve by column name
            idOffset = rs.getLong("id") % Long.MAX_VALUE;
        }

        String postQuery = "INSERT INTO fortune(id, content) VALUES (" + "?,?)";

        PreparedStatement preparedStatement = conn.prepareStatement(postQuery);


        long tempId = counter.incrementAndGet() + idOffset;

        if (get(tempId) == null) {
            preparedStatement.setInt(1, (int) tempId);
            preparedStatement.setString(2, msg);

            preparedStatement.execute();

            return Response
                    .status(201)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(new Saying(tempId, msg))
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
    @Path("fortunes/{id}")
    public Response deleteIt(@PathParam("id") long id) throws SQLException {
        // Get database connection
        Connection conn = Database.getConnection();

        Saying item = get(id);
        if (item != null) {
            String deleteQuery = "delete from fortune where id=?";

            PreparedStatement preparedStatement = conn.prepareStatement(deleteQuery);

            preparedStatement.setLong(1, id);

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

            return new Saying(outputId, outputContent);
        }
        return null;
    }
}
