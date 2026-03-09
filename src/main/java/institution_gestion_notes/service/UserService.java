package institution_gestion_notes.service;

import institution_gestion_notes.entity.User;

import java.util.Optional;

public interface UserService {

    User createUser(User user);

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long idUser);

    void updatePassword(String email, String newPassword);
}
