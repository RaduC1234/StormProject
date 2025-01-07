package me.radu.data;

public record User(
        Long id,
        String username,
        String password,
        String firstName,
        String lastName,
        UserType type
) implements Comparable<User> {

    @Override
    public int compareTo(User o) {
        return Long.compare(this.id, o.id);
    }

    public enum UserType {
        NORMAL,
        ADMIN,
        UNKNOWN
    }
}
