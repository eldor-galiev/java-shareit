package ru.practicum.shareit.request;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemForRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ItemRequestMapper {

    private ItemRequestMapper() {
    }

    public static ItemRequest toItemRequest(ItemRequestDto dto, User requestor) {
        return new ItemRequest(null, dto.getDescription(), requestor, LocalDateTime.now());
    }

    public static ItemRequestDto toItemRequestDto(ItemRequest request, List<Item> items) {
        List<ItemForRequestDto> answers = items.stream()
                .map(ItemRequestMapper::toItemForRequestDto)
                .collect(Collectors.toList());
        return new ItemRequestDto(request.getId(), request.getDescription(), request.getCreated(), answers);
    }

    private static ItemForRequestDto toItemForRequestDto(Item item) {
        return new ItemForRequestDto(item.getId(), item.getName(), item.getOwner().getId());
    }
}
