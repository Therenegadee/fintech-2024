package ru.tbank.hw10.repository;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import ru.tbank.hw10.entity.Event;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer>, JpaSpecificationExecutor<Event> {

    List<Event> findAll(Specification<Event> specification);

    static Specification<Event> buildSpecification(String eventName, Integer placeId, String placeName,
                                                   OffsetDateTime fromDate, OffsetDateTime toDate) {
        List<Specification<Event>> specifications = new ArrayList<>();

        if(Objects.nonNull(eventName) && !StringUtils.isBlank(eventName)) {
            specifications.add(
                    (Specification<Event>) (event, query, criteriaBuilder) ->
                            criteriaBuilder.equal(event.get("name"), eventName)
            );
        }

        if(Objects.nonNull(placeId)) {
            specifications.add(
                    (Specification<Event>) (event, query, criteriaBuilder) ->
                            criteriaBuilder.equal(event.get("place").get("id"), placeId)
            );
        }

        if(Objects.nonNull(placeName) && !StringUtils.isBlank(placeName)) {
            specifications.add(
                    (Specification<Event>) (event, query, criteriaBuilder) ->
                            criteriaBuilder.equal(event.get("place").get("name"), placeName)
            );
        }

        if(Objects.nonNull(fromDate)) {
            specifications.add(
                    (Specification<Event>) (event, query, criteriaBuilder) ->
                            criteriaBuilder.greaterThanOrEqualTo(event.get("fromDate"), fromDate)
            );
        }

        if(Objects.nonNull(toDate)) {
            specifications.add(
                    (Specification<Event>) (event, query, criteriaBuilder) ->
                            criteriaBuilder.lessThanOrEqualTo(event.get("toDate"), toDate)
            );
        }

        return specifications.stream().reduce(Specification::and).orElse(null);
    }
}
