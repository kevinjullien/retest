package be.vinci.pae.services.DAO;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import be.vinci.pae.domain.user.UserDTO;
import be.vinci.pae.domain.user.UserFactory;
import be.vinci.pae.services.DAL.DalServices;
import jakarta.inject.Inject;

public class UserDAOImpl implements UserDAO {

  @Inject
  private DalServices dalService;
  @Inject
  private UserFactory userFactory;


  /**
   * Searching through the database for the user, using his email.
   * 
   * @param email the email
   * @return the user if he exists, otherwise null
   */
  @Override
  public UserDTO getUser(String email) {
    // TODO PS -> attribut?
    // TODO fetch de l'adresse aussi
    UserDTO user = null;

    try {
      PreparedStatement ps = dalService.getPreparedStatement(
          "SELECT u.id_user, u.username, u.last_name, u.first_name, u.email, u.role, "
              + "u.registration_date, u.is_validated, u.password, u.address "
              + "FROM pae.users u WHERE u.email = ?;");


      ps.setString(1, email);

      ResultSet rs = ps.executeQuery();

      while (rs.next()) {
        user = userFactory.getUserDTO(); // TODO bon endroit?
        user.setId(rs.getInt(1));
        user.setUsername(rs.getString(2));
        user.setLastName(rs.getString(3));
        user.setFirstName(rs.getString(4));
        user.setEmail(rs.getString(5));
        user.setRole(rs.getString(6));
        user.setRegistrationDate(rs.getTimestamp(7).toLocalDateTime());
        user.setValidated(rs.getBoolean(8));
        user.setPassword(rs.getString(9));
        user.setAddress(rs.getInt(10));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return user;
  }

}