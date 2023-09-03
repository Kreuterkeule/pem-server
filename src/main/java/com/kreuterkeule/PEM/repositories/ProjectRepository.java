package com.kreuterkeule.PEM.repositories;

import com.kreuterkeule.PEM.models.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {

    List<ProjectEntity> getByName(String name);

}
