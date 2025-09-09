package com.beyond.HanSoom.user.repository;

import com.beyond.HanSoom.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Page<User> findAll(Specification<User> specification, Pageable pageable);
    Optional<User> findBySocialId(String socialId);

    Optional<User> findByName(String name);
}
