package com.github.invizible.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * A Publication.
 */
@Entity
@Table(name = "publication")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Publication implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @NotNull
    @Size(min = 2)
    @Column(name = "title", nullable = false)
    private String title;

    @NotNull
    @Size(min = 2)
    @Column(name = "text", nullable = false)
    private String text;

    @Column(name = "publication_date")
    private ZonedDateTime publicationDate = ZonedDateTime.now();

    @ManyToOne(optional = false)
    private User author;

    @ManyToMany(fetch = FetchType.EAGER)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "publication_rubrics",
               joinColumns = @JoinColumn(name="publications_id", referencedColumnName="id"),
               inverseJoinColumns = @JoinColumn(name="rubrics_id", referencedColumnName="id"))
    private Set<Rubric> rubrics = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here, do not remove
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public Publication title(String title) {
        this.title = title;
        return this;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public Publication text(String text) {
        this.text = text;
        return this;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ZonedDateTime getPublicationDate() {
        return publicationDate;
    }

    public Publication publicationDate(ZonedDateTime publicationDate) {
        this.publicationDate = publicationDate;
        return this;
    }

    public void setPublicationDate(ZonedDateTime publicationDate) {
        this.publicationDate = publicationDate;
    }

    public User getAuthor() {
        return author;
    }

    public Publication author(User user) {
        this.author = user;
        return this;
    }

    public void setAuthor(User user) {
        this.author = user;
    }

    public Set<Rubric> getRubrics() {
        return rubrics;
    }

    public Publication rubrics(Set<Rubric> rubrics) {
        this.rubrics = rubrics;
        return this;
    }

    public Publication addRubrics(Rubric rubric) {
        this.rubrics.add(rubric);
        return this;
    }

    public Publication removeRubrics(Rubric rubric) {
        this.rubrics.remove(rubric);
        return this;
    }

    public void setRubrics(Set<Rubric> rubrics) {
        this.rubrics = rubrics;
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here, do not remove

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Publication publication = (Publication) o;
        if (publication.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), publication.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "Publication{" +
            "id=" + getId() +
            ", title='" + getTitle() + "'" +
            ", text='" + getText() + "'" +
            ", publicationDate='" + getPublicationDate() + "'" +
            "}";
    }
}
