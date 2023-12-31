package com.kreuterkeule.PEM.repositories;

import com.kreuterkeule.PEM.models.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {

    Optional<UserEntity> findByUsername(String username);

    Boolean existsByUsername(String username);

    @Query("SELECT u FROM UserEntity u where u.identifierToken = :token ")
    Optional<UserEntity> findByIdentifierToken(String token);

    @Query("SELECT user FROM UserEntity user WHERE user.username IN :usernames")
    List<UserEntity> findUsersByUsernames(List<String> usernames);

}
