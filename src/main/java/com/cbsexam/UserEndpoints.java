package com.cbsexam;

import cache.UserCache;
import com.google.gson.Gson;
import controllers.UserController;
import java.util.ArrayList;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import model.User;
import utils.Encryption;
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
    // Here we added encryption to the "order" by calling the method "encryptDecryptXOR" method in the Encryption class. Furthermore it takes json in it's parameter.
    json = Encryption.encryptDecryptXOR(json);

    // Return the user with the status code 200
    // TODO: What should happen if something breaks down? - fixed
    UserController.getUser(idUser);
// Nedenstående if-statement angiver hvilken betingelse der skal være opfyldt for at systemet kører den rigtige status.
    if(user!=null){
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    }else
      return Response.status(400).type("Something went wrong ").build();

  }

  public static UserCache UserCache = new UserCache(); // selv tilføjet

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
    // Here we added encryption to the "order" by calling the method "encryptDecryptXOR" method in the Encryption class. Furthermore it takes json in it's parameter.
    json = Encryption.encryptDecryptXOR(json);

    UserController.getUsers();

    // Return the users with the status code 200
    // Nedenstående if-statement angiver hvilken betingelse der skal være opfyldt for at systemet kører den rigtige status.
    if(users!=null){
      return Response.status(200).type(MediaType.APPLICATION_JSON).entity(json).build();
    }else
      return Response.status(400).type("Something went wrong").build();

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
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
      return Response.status(400).entity("Could not create user").build();
    }
  }

  // TODO: Make the system able to login users and assign them a token to use throughout the system.
  @POST
  @Path("/login")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response loginUser(String x) {

    // Return a response with status 200 and JSON as type
    return Response.status(400).entity("Endpoint not implemented yet").build();
  }

  // TODO: Make the system able to delete users - Fixed
  @POST
  @Path("/delete/{delete}")
  public Response deleteUser(@PathParam("delete") int idToDelete) {


    boolean success = UserController.deleteUser(idToDelete);
    UserCache.getUsers(true);

if (success){
  return Response.status(200).entity(" The User with "+ idToDelete + " has now been deleted").build();
} else{
  return Response.status(400).entity("The user could not be deleted").build();}
  }

  // TODO: Make the system able to update users - fixed
  @POST
  @Path("/update/{update}")
  public Response updateUser(@PathParam("update") int idToUpdate, String body) {

    User updates = new Gson().fromJson(body, User.class);

    User currentuser = UserController.getUser(idToUpdate);

    if(updates.getFirstname() == null){
      updates.setFirstname(currentuser.getFirstname());
    }

    if(updates.getLastname() == null){
      updates.setLastname(currentuser.getLastname());
    }

    if(updates.getEmail() == null){
      updates.setEmail(currentuser.getEmail());
    }
    // LAv en metode til at ændre password

    boolean success = UserController.updateUser(idToUpdate, updates);
    UserCache.getUsers(true);

    if(success){
      // Dette betyder at vi henter data fra Databasen og ikke fra cachen.
      return Response.status(200).entity("User with " + idToUpdate + " has now been updated").build();
    } else{
      return Response.status(400).entity("User could not be updated").build();
    }
  }
}
