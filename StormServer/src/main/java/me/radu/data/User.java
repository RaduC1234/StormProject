package me.radu.data;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@NamedQueries({
        @NamedQuery(name = "User.findByUsername", query = "select u from User u where u.username = :username"),
})
public class User implements Comparable<User> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserType type;

    @Column(nullable = true)
    private String savedLocationString;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(username, user.username) && Objects.equals(password, user.password) && Objects.equals(firstName, user.firstName) && Objects.equals(lastName, user.lastName) && type == user.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, password, firstName, lastName, type);
    }

    @Override
    public int compareTo(User o) {
        return (int) (this.id - o.id);
    }

    public enum UserType {
        NORMAL,
        ADMIN,

        UNKNOWN
    }
}
