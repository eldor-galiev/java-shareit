package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto create(Long userId, ItemDto dto) {
        User owner = getUser(userId);
        Item item = ItemMapper.toItem(dto, owner);
        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    @Transactional
    public ItemDto update(Long userId, Long itemId, ItemDto dto) {
        Item existing = getItem(itemId);
        if (!existing.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Item not found for this user");
        }
        if (dto.getName() != null && !dto.getName().isBlank()) {
            existing.setName(dto.getName());
        }
        if (dto.getDescription() != null && !dto.getDescription().isBlank()) {
            existing.setDescription(dto.getDescription());
        }
        if (dto.getAvailable() != null) {
            existing.setAvailable(dto.getAvailable());
        }
        return ItemMapper.toItemDto(itemRepository.save(existing));
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDto getById(Long userId, Long itemId) {
        Item item = getItem(itemId);

        List<CommentDto> comments = commentRepository.findAllByItemId(itemId).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());

        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();
            BookingShortDto lastBooking = bookingRepository
                    .findFirstByItemAndStatusAndEndLessThanOrderByEndDesc(item, BookingStatus.APPROVED, now)
                    .map(BookingMapper::toBookingShortDto)
                    .orElse(null);
            BookingShortDto nextBooking = bookingRepository
                    .findFirstByItemAndStatusAndStartGreaterThanOrderByStartAsc(item, BookingStatus.APPROVED, now)
                    .map(BookingMapper::toBookingShortDto)
                    .orElse(null);
            return ItemMapper.toItemDto(item, lastBooking, nextBooking, comments);
        }

        return ItemMapper.toItemDto(item, null, null, comments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> getAllByOwner(Long userId) {
        List<Item> items = itemRepository.findAllByOwnerIdOrderById(userId);
        LocalDateTime now = LocalDateTime.now();

        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());

        Map<Long, List<Comment>> rawCommentsByItem = commentRepository.findAllByItemIdIn(itemIds).stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId()));

        Map<Long, List<Booking>> bookingsByItem = bookingRepository
                .findAllByItemInAndStatusOrderByStartAsc(items, BookingStatus.APPROVED).stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));

        return items.stream().map(item -> {
            List<Booking> itemBookings = bookingsByItem.getOrDefault(item.getId(), List.of());
            BookingShortDto lastBooking = itemBookings.stream()
                    .filter(booking -> booking.getEnd().isBefore(now))
                    .max(Comparator.comparing(Booking::getEnd))
                    .map(BookingMapper::toBookingShortDto)
                    .orElse(null);
            BookingShortDto nextBooking = itemBookings.stream()
                    .filter(booking -> booking.getStart().isAfter(now))
                    .min(Comparator.comparing(Booking::getStart))
                    .map(BookingMapper::toBookingShortDto)
                    .orElse(null);
            List<CommentDto> comments = rawCommentsByItem.getOrDefault(item.getId(), List.of()).stream()
                    .map(CommentMapper::toCommentDto)
                    .collect(Collectors.toList());
            return ItemMapper.toItemDto(item, lastBooking, nextBooking, comments);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentDto dto) {
        User author = getUser(userId);
        Item item = getItem(itemId);

        boolean hasPastBooking = bookingRepository.existsByBookerIdAndItemIdAndEndLessThan(
                userId, itemId, LocalDateTime.now());
        if (!hasPastBooking) {
            throw new ValidationException("User has no completed booking for this item");
        }

        Comment comment = CommentMapper.toComment(dto, item, author);
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    private Item getItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found: " + itemId));
    }
}
