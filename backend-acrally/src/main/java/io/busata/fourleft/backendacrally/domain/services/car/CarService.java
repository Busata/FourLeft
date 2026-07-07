package io.busata.fourleft.backendacrally.domain.services.car;

import io.busata.fourleft.backendacrally.domain.models.car.Car;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository carRepository;

    public List<Car> list() {
        return carRepository.findAllByOrderByNameAsc();
    }

    @Transactional
    public Car create(String name, Integer year, String groupName, String className) {
        String cleaned = require(name);
        if (carRepository.existsByNameIgnoreCase(cleaned)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A car with that name already exists.");
        }
        return carRepository.save(new Car(cleaned, year, groupName, className));
    }

    @Transactional
    public Car update(UUID id, String name, Integer year, String groupName, String className) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        String cleaned = require(name);
        if (!car.getName().equalsIgnoreCase(cleaned) && carRepository.existsByNameIgnoreCase(cleaned)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A car with that name already exists.");
        }
        car.update(cleaned, year, groupName, className);
        return car;
    }

    @Transactional
    public void delete(UUID id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        carRepository.delete(car);
    }

    private String require(String name) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A name is required.");
        }
        return name.strip();
    }
}
