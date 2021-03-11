package be.vinci.pae.tests;

import be.vinci.pae.domain.user.UserDTO;
import be.vinci.pae.services.DAO.UserDAO;

public class MockUserDAO implements UserDAO {

  @Override
  public UserDTO getUser(String email) {
    UserDTO user = null;
    if (email.equals("test@test.com")) {

      user = UserDistributor.getGoodValidatedUser();
    }
    return user;
  }

}