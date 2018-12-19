package com.cbsexam;

import cache.UserCache;
import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.sun.xml.internal.fastinfoset.DecoderStateTables;
import controllers.UserController;

import java.sql.SQLException;
import java.util.ArrayList;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import model.User;
import utils.Encryption;
import utils.Hashing;
import utils.Log;

@Path("user")
public class UserEndpoints {

  /**
   * @param idUser
   * @return Responses
   */
  @GET
  @Path("/{idUser}")
  public Response getUser(@PathParam("idUser") int idUser) {

    // Use the ID to get the user from the controller.
    User user = UserController.getUser(idUser);

    // TODO: Add Encryption to JSON - fixed
    // Convert the user object to json in order to return the object
    String json = new Gson().toJson(user);
    // Here we added encryption to the "user" by calling the method "encryptDecryptXOR" method in the Encryption class.
    json = Encryption.encryptDecryptXOR(json);

    // Return the user with the status code 200
    // TODO: What should happen if something breaks down? - fixed
//  In terms of figuring out what should happen if something breaks down, the user has to be different from null
    if(user!=null){
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    }else
      return Response.status(400).type("Something went wrong ").build();

  }

  public static UserCache UserCache = new UserCache();

  /** @return Responses */
  @GET
  @Path("/")
  public Response getUsers() {

    // Write to log that we are here
    Log.writeLog(this.getClass().getName(), this, "Get all users", 0);

    // Get a list of users
    ArrayList<User> users = UserCache.getUsers(false);

    // TODO: Add Encryption to JSON - fixed
    // Transfer users to json in order to return it to the user
    String json = new Gson().toJson(users);
    // Here we added encryption to the "users" by calling the method "encryptDecryptXOR" method in the Encryption class.
    json = Encryption.encryptDecryptXOR(json);

    UserCache.getUsers(true);

    // Return the users with the status code 200 if users is different from null
    if(users!=null){
      return Response.status(200).type(MediaType.APPLICATION_JSON).entity(json).build();
    }else{
      return Response.status(400).type("Something went wrong").build();
    }


  }

  @POST
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createUser(String body) {

    // Read the json from body and transfer it to a user class
    User newUser = new Gson().fromJson(body, User.class);

    // Use the controller to add the user
    User createUser = UserController.createUser(newUser);

    // Get the user back with the added ID and return it to the user
    String json = new Gson().toJson(createUser);

    // Return the data to the user
    if (createUser != null) {
      // Return a response with status 200 and JSON as type
      // Down below we force the cache to update if creating a new user
      UserCache.getUsers(true);
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity( "The User with '" +json + " Has now been created'").build();
    } else {
      return Response.status(400).entity("Could not create user").build();
    }
  }

  // TODO: Make the system able to login users and assign them a token to use throughout the system (fixed).
  @POST
  @Path("/login")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response loginUser(String UserBody) {
// Down below the user object is transfered from Json to Gson which allows java to handle it as user object.
    User loginUser = new Gson().fromJson(UserBody, User.class);
    Hashing hashing = new Hashing();
     // Down below we see the user object
    User dbUser = UserController.getUserByEmail(loginUser.getEmail());
    String json = new Gson().toJson(dbUser);


    // Return a response with status 200 and JSON as type
    // checks whether or not the user is different from null
    if(dbUser != null && loginUser.getEmail().equals(dbUser.getEmail()) && hashing.saltWithMd5(loginUser.getPassword()).equals(dbUser.getPassword())){
      return Response.status(200).entity("The user with '" + json + "has now been succesfully logged in'").build();

    }
    else
    return Response.status(400).entity("Password or username has been inserted wrong").build();
    }

  // TODO: Make the system able to delete users - Fixed
  @DELETE
  @Path("/delete/")
  public Response deleteUser(String token) {

// Down below we determine whether or not the user has been deleted
    boolean success = UserController.deleteUser(token);
    UserCache.getUsers(true);

if (success){
  return Response.status(200).entity(" The User has been deleted").build();
} else{
  return Response.status(400).entity("The user could not be deleted").build();}
  }

  // TODO: Make the system able to update users - fixed
  @POST
  @Path("/update/")
  public Response updateUser( String infoToUpdate) {

    User updatedInformation = new Gson().fromJson(infoToUpdate, User.class);


    // Down below we call the updatemethod, and afterwards we update the cache
    User updatedUser = UserController.updateUser(updatedInformation);
    UserCache.getUsers(true);

    if(updatedUser !=null){

      return Response.status(200).entity("User has now been updated").build();
    } else{
      return Response.status(400).entity("User could not be updated").build();
    }
  }
}
