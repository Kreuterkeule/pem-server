package com.kreuterkeule.PEM.repo;

import com.kreuterkeule.PEM.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepo extends JpaRepository<UserEntity, Long> {

    @Query("SELECT u FROM UserEntity u where u.username = :username")
    public Optional<UserEntity> getUserByUsername(@Param("username") String username);

}
