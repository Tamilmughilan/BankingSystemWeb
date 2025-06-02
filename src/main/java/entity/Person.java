package entity;

//Interface person with common methods of all the entities implementing them
public interface Person {
    int getId();
    String getName();
    String getEmail();

    default boolean isValidEmail() {
        String email = getEmail();
        return email != null && email.contains("@") && email.contains(".");
    }

    default boolean isValidName() {
        String name = getName();
        return name != null && !name.trim().isEmpty();
    }
}
