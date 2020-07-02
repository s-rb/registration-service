package ru.list.surkovr.skblab.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.list.surkovr.skblab.model.entities.User;

import java.util.List;
import java.util.stream.Stream;

public interface UserRepository extends JpaRepository<User, Long> {

    User findUserByLogin(String login);

    User findUserByEmail(String email);

    List<User> findUsersByFirstnameAndLastname(String firstName, String lastName);

    List<User> findUsersByFirstnameAndLastnameAndMiddlename(Stream firstName, Stream lastName, String middleName);
}
