package com.coworking.reservation.repository;

import com.coworking.reservation.entity.Reservation;
import com.coworking.reservation.entity.enums.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @EntityGraph(attributePaths = {"user", "space"})
    @Query("""
            select r
            from Reservation r
            where r.user.id = :userId
            order by r.startTime desc
            """)
    Page<Reservation> findByUserIdWithDetails(
            @Param("userId") Long userId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"user", "space"})
    @Query("""
            select r
            from Reservation r
            order by r.startTime desc
            """)
    Page<Reservation> findAllWithDetails(Pageable pageable);

    @Query("""
            select count(r) > 0
            from Reservation r
            where r.space.id = :spaceId
              and r.status in :statuses
              and r.startTime < :endTime
              and r.endTime > :startTime
            """)
    boolean existsOverlappingReservation(
            @Param("spaceId") Long spaceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("statuses") Collection<ReservationStatus> statuses
    );

    @EntityGraph(attributePaths = {"user", "space"})
    @Query("""
            select r
            from Reservation r
            where r.id = :id
            """)
    Optional<Reservation> findByIdWithDetails(@Param("id") Long id);

    @EntityGraph(attributePaths = {"space"})
    @Query("""
            select r
            from Reservation r
            where r.status = :status
            and r.startTime < :endTime
            and r.endTime > :startTime
            """)
    List<Reservation> findByStatusOverlappingPeriod(
            @Param("status") ReservationStatus status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
