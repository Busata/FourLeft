package io.busata.fourleft.backendacrally.endpoints;

import io.busata.fourleft.api.acrally.models.CarRequestTo;
import io.busata.fourleft.api.acrally.models.CarTo;
import io.busata.fourleft.backendacrally.domain.models.car.Car;
import io.busata.fourleft.backendacrally.domain.services.car.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Admin CRUD for the car catalogue. Locked to ROLE_ADMIN by the {@code /acrally-api/admin/**} rule. */
@RestController
@RequestMapping("/acrally-api/admin/cars")
@RequiredArgsConstructor
public class AdminCarEndpoint {

    private final CarService carService;

    @GetMapping("")
    public List<CarTo> list() {
        return carService.list().stream().map(this::toTo).toList();
    }

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    public CarTo create(@RequestBody CarRequestTo request) {
        return toTo(carService.create(request.name(), request.year(), request.groupName(), request.className()));
    }

    @PutMapping("/{id}")
    public CarTo update(@PathVariable UUID id, @RequestBody CarRequestTo request) {
        return toTo(carService.update(id, request.name(), request.year(), request.groupName(), request.className()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        carService.delete(id);
    }

    private CarTo toTo(Car car) {
        return new CarTo(
                car.getId(),
                car.getName(),
                car.getYear(),
                car.getGroupName(),
                car.getClassName(),
                car.getCreatedAt(),
                car.getUpdatedAt());
    }
}
