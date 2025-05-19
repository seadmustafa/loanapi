package com.inghub.loanapi.repository;


import com.inghub.loanapi.entity.Role;
import com.inghub.loanapi.enums.RoleEnum;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends CrudRepository<Role, Integer> {
    Optional<Role> findByName(RoleEnum name);
}
