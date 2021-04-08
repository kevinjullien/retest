package be.vinci.pae.api;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.jersey.server.ContainerRequest;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;

import be.vinci.pae.api.filters.Authorize;
import be.vinci.pae.domain.address.AddressFactory;
import be.vinci.pae.domain.user.User;
import be.vinci.pae.domain.user.UserDTO;
import be.vinci.pae.domain.user.UserFactory;
import be.vinci.pae.domain.user.UserUCC;
import be.vinci.pae.exceptions.UnauthorizedException;
import be.vinci.pae.utils.Config;
import be.vinci.pae.views.Views;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Singleton
@Path("/auths")
public class Authentication {

  @Inject
  private Logger logger;
  private final Algorithm jwtAlgorithm = Algorithm.HMAC256(Config.getStringProperty("JWTSecret"));
  private final ObjectMapper jsonMapper = new ObjectMapper();

  public Authentication() {
    jsonMapper.findAndRegisterModules();
    jsonMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  }

  @Inject
  private UserUCC userUCC;

  @Inject
  private UserFactory userFactory;

  @Inject
  private AddressFactory addressFactory;


  /**
   * Quick way to construct HTTP Response with text only.
   * 
   * @param status the status
   * @param message the message
   * @return a Response containing the status and the message
   */
  private Response constructResponse(Status status, String message) {
    return Response.status(status).entity(message).type(MediaType.TEXT_PLAIN).build();
  }

  /**
   * This method is used to attempt to log a client in. Valid email and password are required to be
   * able to send a token and a response 200.
   * 
   * @param json post received from the client
   * @return Response 401, 412 if KO; 200 and credentials + token if OK
   */
  @POST
  @Path("login")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response login(JsonNode json) {
    if (!checkLoginFields(json)) {
      return constructResponse(Status.PRECONDITION_FAILED, "The input data is invalid");
    }

    String email = json.get("email").asText();
    String password = json.get("password").asText();

    User user = (User) userUCC.connection(email, password);

    return createToken(user);
  }

  private boolean checkLoginFields(JsonNode json) {
    if (!json.hasNonNull("email") || !json.hasNonNull("password")
        || json.get("email").asText().isEmpty() || json.get("password").asText().isEmpty()) {
      return false;
    }
    return true;
  }


  /**
   * This method is used for registering a user. It also adds the address into the database
   * 
   * @param json post received from the client
   * @return Response 401 Or 409 if KO; token if OK
   */
  @POST
  @Path("register")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response register(UserDTO user) {
    if (!checkFieldsRegister(user)) {
      return Response.status(Status.UNAUTHORIZED).entity("Missing fields")
          .type(MediaType.TEXT_PLAIN).build();
    }

    if (!user.getPassword().equals(user.getPasswordVerification())) {
      throw new UnauthorizedException("The passwords don't match");
    }

    return Response.status(Status.CREATED).entity(userUCC.registration(user)).build();

  }

  private boolean checkFieldsRegister(UserDTO user) {
    if (user.getEmail() == null || user.getPassword() == null
        || user.getPasswordVerification() == null || user.getFirstName() == null
        || user.getLastName() == null || user.getAddress().getStreet() == null
        || user.getAddress().getBuildingNumber() == null || user.getAddress().getCity() == null
        || user.getAddress().getPostCode() == null || user.getAddress().getCountry() == null
        || user.getEmail().isEmpty() || user.getPassword().isEmpty()
        || user.getPasswordVerification().isEmpty() || user.getFirstName().isEmpty()
        || user.getLastName().isEmpty() || user.getAddress().getStreet().isEmpty()
        || user.getAddress().getBuildingNumber().isEmpty() || user.getAddress().getCity().isEmpty()
        || user.getAddress().getPostCode().isEmpty() || user.getAddress().getCountry().isEmpty()) {
      return false;
    }
    return true;
  }

  /**
   * Create a valid token to be sent to a user.
   * 
   * @param user a non null user
   * @return a valid token
   */
  private Response createToken(User user) {
    String token;
    try {
      token = JWT.create().withExpiresAt(Date.from(Instant.now().plus(1, ChronoUnit.DAYS)))
          .withIssuer("auth0").withClaim("user", user.getId()).sign(this.jwtAlgorithm);
    } catch (Exception e) {
      throw new WebApplicationException("Impossible to create a token", e,
          Status.INTERNAL_SERVER_ERROR);
    }

    ObjectNode node =
        jsonMapper.createObjectNode().put("token", token).putPOJO("user", user.getId());
    logger.log(Level.INFO, "Connection of user:" + user.getUsername() + " :: " + user.getId());
    return Response.ok(node, MediaType.APPLICATION_JSON).build();
  }

  /**
   * Get the current user.
   * 
   * @param request the request
   * @return a user with public filtered fields as a String
   */
  @GET
  @Path("user")
  @Authorize
  @Produces(MediaType.APPLICATION_JSON)
  public Response getUser(@Context ContainerRequest request) throws JsonProcessingException {
    return Response.ok(jsonMapper.writerWithView(Views.Public.class)
        .writeValueAsString((UserDTO) request.getProperty("user"))).build();
  }
}
