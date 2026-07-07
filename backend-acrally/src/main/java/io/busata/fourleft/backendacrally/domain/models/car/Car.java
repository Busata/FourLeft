package io.busata.fourleft.backendacrally.domain.models.car;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/** A rally car in the reference catalogue: name, model year, group and class. Admin-managed. */
@Entity
@Table(name = "car")
@Getter
@NoArgsConstructor
public class Car {

    @Id
    private UUID id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "model_year")
    private Integer year;

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "class_name")
    private String className;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Car(String name, Integer year, String groupName, String className) {
        this.id = UUID.randomUUID();
        this.name = name.strip();
        this.year = year;
        this.groupName = normalise(groupName);
        this.className = normalise(className);
        this.createdAt = LocalDateTime.now();
    }

    public void update(String name, Integer year, String groupName, String className) {
        this.name = name.strip();
        this.year = year;
        this.groupName = normalise(groupName);
        this.className = normalise(className);
        this.updatedAt = LocalDateTime.now();
    }

    private static String normalise(String value) {
        return (value == null || value.isBlank()) ? null : value.strip();
    }
}
