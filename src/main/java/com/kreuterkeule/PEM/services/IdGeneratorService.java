package com.kreuterkeule.PEM.services;

import com.kreuterkeule.PEM.repositories.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class IdGeneratorService {

    private final ProjectRepository projectRepository;

    @Autowired
    public IdGeneratorService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public long generateProjectId() {
        long id = new Random().nextLong();
        if (id < 0) return this.generateProjectId();
        return (projectRepository.findById(id).orElse(null) == null) ? id : this.generateProjectId();
    }


}
