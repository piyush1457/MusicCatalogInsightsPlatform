package com.musiccatalog.repository;

import com.musiccatalog.entity.LibraryItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LibraryItemRepository extends JpaRepository<LibraryItem, Long> {

    Page<LibraryItem> findByUserId(Long userId, Pageable pageable);

    List<LibraryItem> findAllByUserId(Long userId);

    @Query("""
        SELECT l FROM LibraryItem l
        WHERE l.userId = :userId
        AND (:genre IS NULL OR l.genre = :genre)
        AND (:year IS NULL OR l.releaseDate LIKE :year || '%')
    """)
    Page<LibraryItem> findByUserIdWithFilters(
            @Param("userId") Long userId,
            @Param("genre") String genre,
            @Param("year") String year,
            Pageable pageable
    );

    Optional<LibraryItem> findByIdAndUserId(Long id, Long userId);
}
