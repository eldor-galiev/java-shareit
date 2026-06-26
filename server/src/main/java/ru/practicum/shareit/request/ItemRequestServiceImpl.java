package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto create(Long userId, ItemRequestDto dto) {
        User requestor = getUser(userId);
        ItemRequest request = ItemRequestMapper.toItemRequest(dto, requestor);
        return ItemRequestMapper.toItemRequestDto(requestRepository.save(request), List.of());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestDto> getOwn(Long userId) {
        getUser(userId);
        return toDtosWithItems(requestRepository.findAllByRequestorIdOrderByCreatedDesc(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestDto> getAll(Long userId) {
        getUser(userId);
        return toDtosWithItems(requestRepository.findAllByRequestorIdNotOrderByCreatedDesc(userId));
    }

    @Override
    @Transactional(readOnly = true)
    public ItemRequestDto getById(Long userId, Long requestId) {
        getUser(userId);
        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request not found: " + requestId));
        return ItemRequestMapper.toItemRequestDto(request, itemRepository.findAllByRequestId(requestId));
    }

    private List<ItemRequestDto> toDtosWithItems(List<ItemRequest> requests) {
        List<Long> requestIds = requests.stream().map(ItemRequest::getId).collect(Collectors.toList());
        Map<Long, List<Item>> itemsByRequest = itemRepository.findAllByRequestIdIn(requestIds).stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));
        return requests.stream()
                .map(request -> ItemRequestMapper.toItemRequestDto(
                        request, itemsByRequest.getOrDefault(request.getId(), List.of())))
                .collect(Collectors.toList());
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }
}
