package be.vinci.pae.domain.user;

import java.util.List;

public interface UserUCC {

  UserDTO connection(String email, String password);

  void acceptUser(int id, String role);

  void deleteUser(int id);

  List<UserDTO> getUnvalidatedUsers();

  UserDTO getUserFromId(int id);
}
