package com.kreuterkeule.PEM.controller;

import com.kreuterkeule.PEM.dto.CreatePDto;
import com.kreuterkeule.PEM.models.ProjectEntity;
import com.kreuterkeule.PEM.models.UserEntity;
import com.kreuterkeule.PEM.repositories.ProjectRepository;
import com.kreuterkeule.PEM.repositories.UserRepository;
import com.kreuterkeule.PEM.security.JwtUtils;
import com.kreuterkeule.PEM.services.IdGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("api/project")
public class ProjectController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private IdGeneratorService idGen;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/create")
    public ResponseEntity<ProjectEntity> create(@RequestBody CreatePDto createPDto) {
        UserEntity creator = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).orElse(null);
        if (creator == null) {
            ProjectEntity $ = new ProjectEntity();
            $.setResponsible(new HashSet<>());
            return new ResponseEntity<>($, HttpStatus.BAD_REQUEST);
        }
        System.out.println(creator.getUsername() + " is creating a project: '" + createPDto.getName() + "'");
        ProjectEntity project = new ProjectEntity();
        project.setName(createPDto.getName());
        project.setClient(createPDto.getClient());
        project.setDescription(createPDto.getDescription());
        project.setCreatedBy(creator.getUsername());
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDate localDeadline = LocalDate.parse(createPDto.getDeadline());
        project.setDeadline(Date.from(localDeadline.atStartOfDay(zoneId).toInstant()));
        List<UserEntity> responsible = userRepository.findUsersByUsernames(createPDto.getResponsible());
        project.setId(idGen.generateProjectId());
        project.setResponsible(new HashSet<>(responsible));
        project = projectRepository.save(project);
        return new ResponseEntity<>(project, HttpStatus.OK);
    }

    // TODO: remove after testing
    @GetMapping("getAll")
    public ResponseEntity<List<ProjectEntity>> getAll() {
        UserEntity client = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).orElse(null);
        if (client == null) {
            return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(client.getProjects(), HttpStatus.OK);
    }

    @GetMapping("delete")
    public ResponseEntity<ProjectEntity> delete(@RequestParam("id") Long id) {
        UserEntity client = userRepository.findByUsername(SecurityContextHolder.getContext().getAuthentication().getName()).orElse(null);
        if (client == null) {
            ProjectEntity $ = new ProjectEntity();
            $.setResponsible(new HashSet<>());
            return new ResponseEntity<>($, HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(projectRepository.deleteById(id), HttpStatus.OK);
    }
}
