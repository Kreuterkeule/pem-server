package com.kreuterkeule.PEM.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "projects")
public class ProjectEntity {
    @Id
    private Long id;

    private String name;

    private String description;

    private String client;

    private String createdBy;

    @CreationTimestamp
    @Temporal(TemporalType.DATE)
    private Date creationDate;

    @Temporal(TemporalType.DATE)
    private Date deadline;


    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinTable(
            name = "user_project",
            joinColumns = {@JoinColumn(name = "project_id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id")}
    )
    private Set<UserEntity> responsible;

    public List<String> getResponsibleNames() {
        List<String> names = new ArrayList<>();
        this.responsible.forEach(e -> names.add(e.getUsername()));
        return names;
    }

    @Override
    public String toString() {
        return "ProjectEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", client='" + client + '\'' +
                ", creationDate=" + creationDate +
                ", deadline=" + deadline +
                '}';
    }
}
