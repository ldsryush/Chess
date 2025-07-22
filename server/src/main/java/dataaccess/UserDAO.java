package dataaccess;
import model.UserData;


public interface UserDAO {

    boolean isUser(UserData userData) throws dataaccess.DataAccessException;

    UserData getUser(String username) throws dataaccess.DataAccessException;

    void createUser(UserData userData) throws dataaccess.DataAccessException;

    void clear() throws dataaccess.DataAccessException;

}