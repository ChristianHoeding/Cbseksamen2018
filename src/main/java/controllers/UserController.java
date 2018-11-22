package controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import com.sun.org.apache.xml.internal.security.algorithms.JCEMapper;
import model.User;
import utils.Hashing;
import utils.Log;

public class UserController {

  private static DatabaseController dbCon;

  public UserController() {
    dbCon = new DatabaseController();
  }

  public static User getUser(int id) {

    // Check for connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build the query for DB
    String sql = "SELECT * FROM user where id=" + id;

    // Actually do the query
    ResultSet rs = dbCon.query(sql);
    User user = null;

    try {
      // Get first object, since we only have one
      if (rs.next()) {
        user =
                new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("password"),
                        rs.getString("email"));

        // return the create object
        return user;
      } else {
        System.out.println("No user found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return null
    return user;
  }

  /**
   * Get all users in database
   *
   * @return
   */
  public static ArrayList<User> getUsers() {

    // Check for DB connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build SQL
    String sql = "SELECT * FROM user";

    // Do the query and initialyze an empty list for use if we don't get results
    ResultSet rs = dbCon.query(sql);
    ArrayList<User> users = new ArrayList<User>();

    try {
      // Loop through DB Data
      while (rs.next()) {
        User user =
                new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("password"),
                        rs.getString("email"));

        // Add element to list
        users.add(user);
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return the list of users
    return users;
  }

  public static User createUser(User user) {

    // Write in log that we've reach this step
    Log.writeLog(UserController.class.getName(), user, "Actually creating a user in DB", 0);
    // We have created
    Hashing hashing = new Hashing();

    // Set creation time for user.
    user.setCreatedTime(System.currentTimeMillis() / 1000L);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Insert the user in the DB
    // TODO: Hash the user password before saving it fixed - we are calling the class Hashing by creating an object. Furthermore we are calling the method "hashWithMd5"  .
    int userID = dbCon.insert(
            "INSERT INTO user(first_name, last_name, password, email, created_at) VALUES('"
                    + user.getFirstname()
                    + "', '"
                    + user.getLastname()
                    + "', '"
                    + hashing.saltWithMd5(user.getPassword()) // Her hashes password - Vi har oprettet et objekt af Hashing klassen, hvori vi har en metode der hedder "saltWithMd5"
                    + "', '"
                    + user.getEmail()
                    + "', "
                    + user.getCreatedTime()
                    + ")");

    if (userID != 0) {
      //Update the userid of the user before returning
      user.setId(userID);
    } else {
      // Return null if user has not been inserted into database
      return null;
    }

    // Return user
    return user;
  }


  public static boolean deleteUser(String token) {
    // Her skabes der forbindelse til DB
    DecodedJWT jwt = null;
    try {
      jwt = JWT.decode(token);
    } catch (JWTDecodeException exception){
      //Invalid token
    }
    int id=jwt.getClaim("Userid").asInt();
    Log.writeLog(UserController.class.getName(), id, "deleting a user in the Database", 0);

    // Her skabes der forbindelse til DB
    if (dbCon == null) {
      dbCon = new DatabaseController();

    }
    User user = UserController.getUser(id);
    String sql = "DELETE FROM user WHERE id=" + id;

    return user != null && dbCon.deleteUser(sql);

  }



  public static User updateUser (User updates){
    DecodedJWT jwt = null;
    try {
      jwt = JWT.decode(updates.getToken());
    } catch (JWTDecodeException exception){
      //Invalid token
    }
    int id =jwt.getClaim("Userid").asInt();
    Hashing hashing = new Hashing();

    // Her skabes der forbindelse til DB
    if (dbCon==null){
      dbCon= new DatabaseController();
    }
    User userToChange = UserController.getUser(id);

    if(updates.getFirstname() == null){
      updates.setFirstname(userToChange.getFirstname());
    }

    if(updates.getLastname() == null){
      updates.setLastname(userToChange.getLastname());
    }

    if(updates.getEmail() == null){
      updates.setEmail(userToChange.getEmail());
    }
    if (updates.getPassword()== null) {
      updates.setPassword(userToChange.getPassword());
    }else{
      updates.setPassword(hashing.saltWithMd5(updates.getPassword()));

    }
    String sql = "Update user set first_name = ' "+ updates.getFirstname() + "', last_name='" + updates.getLastname() +
            "', email='" + updates.getEmail() + "', password = '" + updates.getPassword() + "' Where id = " + id;

    if(dbCon.updateUser(sql)){
      return updates;
    }
    return null;

  }
  public static User getUserByEmail(String emailInfo){
    // Check for connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build the query for DB
    String sql = "SELECT * FROM user where email='" + emailInfo + "'";

    // Actually do the query
    ResultSet rs = dbCon.query(sql);
    User user = null;

    try {
      // Get first object, since we only have one
      if (rs.next()) {
        user =
                new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("password"),
                        rs.getString("email"));

// .withClaim("exp",3600)
        Algorithm algorithm = Algorithm.HMAC256("CBS");
        String token = JWT.create().withClaim("Userid",user.getId()).sign(algorithm);
        user.setToken(token);

        // return the create object
        return user;
      } else {
        System.out.println("No user found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return null
    return user;
  }
}
