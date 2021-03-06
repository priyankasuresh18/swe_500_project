package com.pancakes;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * Created by krithikabaskaran on 10/21/18.
 */


@Path("/user")
public class LoginRestAPI {
    private Session session=new Session();

    private static DatabaseAccess databaseAccess= new DatabaseAccess();

    private static PasswordUtils passwordUtils= new PasswordUtils();
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String sayPlainTextHello() {
        return "Hello user";
    }

    @PUT
    @Path("/signup")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUserRestApi(@FormParam("user_name") String userName,
                                      @FormParam("user_id") String userId,
                                      @FormParam("password") final String password) {
        User checkUser = null;
        try {
            checkUser = readUser(userId);
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e)
                    .build();
        }
        if (checkUser == null) {
            createUser(userId, userName, password);
            try {
                User newUser = readUser(userId);
                NewCookie newCookie= new NewCookie ("session_cookie",session.createCookie(newUser.getUserId()));
                return Response.status(Response.Status.OK)
                        .entity(newUser)
                        .cookie(newCookie)

                        .build();
            } catch (SQLException e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(e)
                        .build();
            }
        } else {
           return  Response.status(Response.Status.UNAUTHORIZED)
                    .entity("User already exists")
                    .build();
        }
    }
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response loginUserRestApi(@FormParam("user_id") String userId,
                                      @FormParam("password") final String password) {
        User checkUser = null;
        try {
            checkUser = readUser(userId);
        } catch (SQLException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(e)
                    .build();
        }
        if (checkUser == null) {

               return  Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Wrong credentials")
                        .build();

        } else {
            boolean verified = passwordUtils.verifyUserPassword(password, checkUser.password);
            if(!verified){
               return  Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Wrong credentials")
                        .build();

            }
            else {


                NewCookie newCookie= new NewCookie ("session_cookie",session.createCookie(checkUser.getUserId()));
    return  Response.status(Response.Status.OK)
                        .cookie(newCookie)
                        .entity("User logged in")
                        .build();
            }
        }

    }

    public void createUser(String userId, String userName, String password) {
        String securep=passwordUtils.generateSecurePassword(password);
        String sql="Insert into user_details (user_id,user_name,user_password) VALUES ("
                +"\""+userId +"\""+","+"\""+userName+"\""+","+"\""+securep+"\""+")";
        System.out.println(sql);
        databaseAccess.runDatabaseQuery(sql);

    }
    public User readUser(String userId) throws SQLException {
        String sql="Select * from user_details where user_id ="
                +"\""+userId+"\""+";";
        User user = null;
        SqlReturnClass returnClass = databaseAccess.readDatabaseQuery(sql);
        ResultSet rs=returnClass.getResultSet();
        while(rs.next()){
            //Retrieve by column name
            String id  = rs.getString("user_id");
            String password = rs.getString("user_password");
            String name = rs.getString("user_name");
            user = new User(id,name,password);
        }
        //STEP 6: Clean-up environment
       returnClass.close();
        return user;
    }

}
