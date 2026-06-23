package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private long idCounter = 0;

    @Override
    public User save(User user) {
        user.setId(++idCounter);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void delete(Long id) {
        users.remove(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return users.values().stream().anyMatch(u -> u.getEmail().equalsIgnoreCase(email));
    }

    @Override
    public boolean existsByEmailAndNotId(String email, Long id) {
        return users.values().stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(email) && !u.getId().equals(id));
    }
}
