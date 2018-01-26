package com.github.invizible.repository;

import com.github.invizible.domain.Publication;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.List;

/**
 * Spring Data JPA repository for the Publication entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PublicationRepository extends JpaRepository<Publication, Long> {

    @Query("select publication from Publication publication where publication.author.login = ?#{principal.username}")
    List<Publication> findByAuthorIsCurrentUser();
    @Query("select distinct publication from Publication publication left join fetch publication.rubrics")
    List<Publication> findAllWithEagerRelationships();

    @Query("select publication from Publication publication left join fetch publication.rubrics where publication.id =:id")
    Publication findOneWithEagerRelationships(@Param("id") Long id);

}
