package com.example.demo.controller;
import com.example.demo.model.User;
import com.example.demo.repository.PersonnelRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/personnel")
public class PersonnelController {

    private final PersonnelRepository personnelRepository;

    public PersonnelController(PersonnelRepository personnelRepository) {
        this.personnelRepository = personnelRepository;
    }

    @GetMapping
    public List<User> getAllPersonnel() {
        return personnelRepository.findAll();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return personnelRepository.findById(id).orElse(null);
    }

    @PostMapping("/save")
    public User savePersonnel(@RequestBody User user) {
        return personnelRepository.save(user);
    }

    @PutMapping("/{id}")
    public void updatePersonnel(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        personnelRepository.save(user);
    }

    @DeleteMapping("/{id}")
    public void deletePersonnel(@PathVariable Long id) {
        personnelRepository.deleteById(id);
    }
}