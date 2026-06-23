package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class InMemoryItemStorage implements ItemStorage {

    private final Map<Long, Item> items = new HashMap<>();
    private long idCounter = 0;

    @Override
    public Item save(Item item) {
        item.setId(++idCounter);
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item item) {
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public List<Item> findAllByOwnerId(Long ownerId) {
        return items.values().stream()
                .filter(i -> i.getOwner().getId().equals(ownerId))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> search(String text) {
        String lower = text.toLowerCase();
        return items.values().stream()
                .filter(i -> Boolean.TRUE.equals(i.getAvailable()))
                .filter(i -> i.getName().toLowerCase().contains(lower)
                        || i.getDescription().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }
}
