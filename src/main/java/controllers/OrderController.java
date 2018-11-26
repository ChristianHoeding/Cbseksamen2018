package controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import model.Address;
import model.LineItem;
import model.Order;
import model.User;
import utils.Log;

public class OrderController {

  private static DatabaseController dbCon;

  public OrderController() {
    dbCon = new DatabaseController();
  }

  public static Order getOrder(int id) {

    // check for connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build SQL string to query
    String sql = "SELECT *, \n" +
            "billing.street_address as billing, \n" +
            "shipping.street_address as shipping\n" +
            "FROM orders\n" +
            "JOIN user on orders.user_id = user.id \n" +
            "            LEFT JOIN address as billing\n" +
            "            ON orders.billing_address_id = billing.id \n" +
            "            LEFT JOIN address as shipping \n" +
            "            ON orders.shipping_address_id = shipping.id where orders.id =" +id; // Selv tilføjet

    // Do the query in the database and create an empty object for the results
    ResultSet rs = dbCon.query(sql);
    Order order = null;

    try {
      if (rs.next()) {
        // Perhaps we could optimize things a bit here and get rid of nested queries.
        ArrayList<LineItem> lineItems = LineItemController.getLineItemsForOrder(rs.getInt("id"));

        User user =
                new User(
                        rs.getInt("user_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("password"),
                        rs.getString("email"));
        Address billingAddress =
                new Address(
                        rs.getInt("billing_address_id"),
                        rs.getString("name"),
                        rs.getString("billing"),
                        rs.getString("city"),
                        rs.getString("zipcode"));
        Address shippingAddress =
                new Address(
                        rs.getInt("shipping_address_id"),
                        rs.getString("name"),
                        rs.getString("shipping"),
                        rs.getString("city"),
                        rs.getString("zipcode"));


        // Create an object instance of order from the database dataa
        order =
            new Order(
                rs.getInt("id"),
                user,
                lineItems,
                billingAddress,
                shippingAddress,
                rs.getFloat("order_total"),
                rs.getLong("created_at"),
                rs.getLong("updated_at"));

        // Returns the build order
        return order;
      } else {
        System.out.println("No order found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Returns null
    return order;
  }

  /**
   * Get all orders in database
   *
   * @return
   */
  public static ArrayList<Order> getOrders() {

    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    String sql = "SELECT *, billing.street_address as billing, shipping.street_address as shipping FROM orders \n" +
                 "join user on orders.user_id=user.id \n" +
                 "LEFT join address as billing on orders.billing_address_id=billing.id \n" +
                 "LEFT join address as shipping on orders.shipping_address_id=shipping.id";

    ResultSet rs = dbCon.query(sql);
    ArrayList<Order> orders = new ArrayList<Order>();

    try {
      while(rs.next()) {

        ArrayList<LineItem> lineItems = LineItemController.getLineItemsForOrder(rs.getInt("id"));
        //TODO: Perhaps we could optimize things a bit here and get rid of nested queries.
        User user =
                new User(
                        rs.getInt("user_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("password"),
                        rs.getString("email"));
        Address billingAddress =
                new Address(
                        rs.getInt("billing_address_id"),
                        rs.getString("name"),
                        rs.getString("billing"),
                        rs.getString("city"),
                        rs.getString("zipcode"));
        Address shippingAddress =
                new Address(
                        rs.getInt("shipping_address_id"),
                        rs.getString("name"),
                        rs.getString("shipping"),
                        rs.getString("city"),
                        rs.getString("zipcode"));



        // Create an order from the database data
        Order order =
            new Order(
                rs.getInt("id"),
                user,
                lineItems,
                billingAddress,
                shippingAddress,
                rs.getFloat("order_total"),
                rs.getLong("created_at"),
                rs.getLong("updated_at"));

        // Add order to our list
        orders.add(order);

      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // return the orders
    return orders;
  }

  public static Order createOrder(Order order) {

    // Write in log that we've reach this step
    Log.writeLog(OrderController.class.getName(), order, "Actually creating a order in DB", 0);

    // Set creation and updated time for order.
    order.setCreatedAt(System.currentTimeMillis() / 1000L);
    order.setUpdatedAt(System.currentTimeMillis() / 1000L);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Save addresses to database and save them back to initial order instance
    try {
      // Databasecontrolleren er sat til at skulle autocommitte hver gang - i dette tilfælde sætter vi autocommit til at være false, så vi kun committer når udfaldet ikke er false.
      DatabaseController.getConnection().setAutoCommit(false);


      order.setBillingAddress(AddressController.createAddress(order.getBillingAddress()));
      order.setShippingAddress(AddressController.createAddress(order.getShippingAddress()));

      // Save the user to the database and save them back to initial order instance
      order.setCustomer(UserController.createUser(order.getCustomer()));

      // TODO: Enable transactions in order for us to not save the order if somethings fails for some of the other inserts.

      // Insert the product in the DB
      int orderID = dbCon.insert(
              "INSERT INTO orders(user_id, billing_address_id, shipping_address_id, order_total, created_at, updated_at) VALUES("
                      + order.getCustomer().getId()
                      + ", "
                      + order.getBillingAddress().getId()
                      + ", "
                      + order.getShippingAddress().getId()
                      + ", "
                      + order.calculateOrderTotal()
                      + ", "
                      + order.getCreatedAt()
                      + ", "
                      + order.getUpdatedAt()
                      + ")");

      if (orderID != 0) {
        //Update the productid of the product before returning
        order.setId(orderID);
      }

      // Create an empty list in order to go trough items and then save them back with ID
      ArrayList<LineItem> items = new ArrayList<LineItem>();

      // Save line items to database
      for (LineItem item : order.getLineItems()) {
        item = LineItemController.createLineItem(item, order.getId());
        items.add(item);
        DatabaseController.getConnection().commit();
      }

      order.setLineItems(items);

    } catch (SQLException e) {
      try {
        DatabaseController.getConnection().rollback();
      }catch (SQLException e1){
        e1.printStackTrace();
      }
      finally {
        try {
          DatabaseController.getConnection().setAutoCommit(true);
        }catch (SQLException e2){
          e2.printStackTrace();
        }
      }
    }

    // Return order
    return order;
  }
}