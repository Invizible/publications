package com.github.invizible.repository;

import com.github.invizible.domain.Rubric;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the Rubric entity.
 */
@SuppressWarnings("unused")
@Repository
public interface RubricRepository extends JpaRepository<Rubric, Long> {

}
