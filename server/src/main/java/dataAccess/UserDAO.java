package dataAccess;
import model.UserData;

public interface UserDAO {

    boolean isUser(UserData userData);

    UserData getUser(String username);

    void createUser(UserData userData);

    void clear();

}
