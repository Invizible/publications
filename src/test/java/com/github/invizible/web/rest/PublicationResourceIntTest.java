package com.github.invizible.web.rest;

import com.github.invizible.PublicationsApp;

import com.github.invizible.domain.Publication;
import com.github.invizible.domain.User;
import com.github.invizible.repository.PublicationRepository;
import com.github.invizible.web.rest.errors.ExceptionTranslator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.time.ZoneId;
import java.util.List;

import static com.github.invizible.web.rest.TestUtil.sameInstant;
import static com.github.invizible.web.rest.TestUtil.createFormattingConversionService;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the PublicationResource REST controller.
 *
 * @see PublicationResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PublicationsApp.class)
public class PublicationResourceIntTest {

    private static final String DEFAULT_TITLE = "AAAAAAAAAA";
    private static final String UPDATED_TITLE = "BBBBBBBBBB";

    private static final String DEFAULT_TEXT = "AAAAAAAAAA";
    private static final String UPDATED_TEXT = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_PUBLICATION_DATE = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_PUBLICATION_DATE = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restPublicationMockMvc;

    private Publication publication;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final PublicationResource publicationResource = new PublicationResource(publicationRepository, userRepository);
        this.restPublicationMockMvc = MockMvcBuilders.standaloneSetup(publicationResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setConversionService(createFormattingConversionService())
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Publication createEntity(EntityManager em) {
        Publication publication = new Publication()
            .title(DEFAULT_TITLE)
            .text(DEFAULT_TEXT)
            .publicationDate(DEFAULT_PUBLICATION_DATE);
        // Add required entity
        User author = UserResourceIntTest.createEntity(em);
        em.persist(author);
        em.flush();
        publication.setAuthor(author);
        return publication;
    }

    @Before
    public void initTest() {
        publication = createEntity(em);
    }

    @Test
    @Transactional
    public void createPublication() throws Exception {
        int databaseSizeBeforeCreate = publicationRepository.findAll().size();

        // Create the Publication
        restPublicationMockMvc.perform(post("/api/publications")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(publication)))
            .andExpect(status().isCreated());

        // Validate the Publication in the database
        List<Publication> publicationList = publicationRepository.findAll();
        assertThat(publicationList).hasSize(databaseSizeBeforeCreate + 1);
        Publication testPublication = publicationList.get(publicationList.size() - 1);
        assertThat(testPublication.getTitle()).isEqualTo(DEFAULT_TITLE);
        assertThat(testPublication.getText()).isEqualTo(DEFAULT_TEXT);
        assertThat(testPublication.getPublicationDate()).isEqualTo(DEFAULT_PUBLICATION_DATE);
    }

    @Test
    @Transactional
    public void createPublicationWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = publicationRepository.findAll().size();

        // Create the Publication with an existing ID
        publication.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restPublicationMockMvc.perform(post("/api/publications")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(publication)))
            .andExpect(status().isBadRequest());

        // Validate the Publication in the database
        List<Publication> publicationList = publicationRepository.findAll();
        assertThat(publicationList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkTitleIsRequired() throws Exception {
        int databaseSizeBeforeTest = publicationRepository.findAll().size();
        // set the field null
        publication.setTitle(null);

        // Create the Publication, which fails.

        restPublicationMockMvc.perform(post("/api/publications")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(publication)))
            .andExpect(status().isBadRequest());

        List<Publication> publicationList = publicationRepository.findAll();
        assertThat(publicationList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkTextIsRequired() throws Exception {
        int databaseSizeBeforeTest = publicationRepository.findAll().size();
        // set the field null
        publication.setText(null);

        // Create the Publication, which fails.

        restPublicationMockMvc.perform(post("/api/publications")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(publication)))
            .andExpect(status().isBadRequest());

        List<Publication> publicationList = publicationRepository.findAll();
        assertThat(publicationList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllPublications() throws Exception {
        // Initialize the database
        publicationRepository.saveAndFlush(publication);

        // Get all the publicationList
        restPublicationMockMvc.perform(get("/api/publications?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(publication.getId().intValue())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE.toString())))
            .andExpect(jsonPath("$.[*].text").value(hasItem(DEFAULT_TEXT.toString())))
            .andExpect(jsonPath("$.[*].publicationDate").value(hasItem(sameInstant(DEFAULT_PUBLICATION_DATE))));
    }

    @Test
    @Transactional
    public void getPublication() throws Exception {
        // Initialize the database
        publicationRepository.saveAndFlush(publication);

        // Get the publication
        restPublicationMockMvc.perform(get("/api/publications/{id}", publication.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(publication.getId().intValue()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE.toString()))
            .andExpect(jsonPath("$.text").value(DEFAULT_TEXT.toString()))
            .andExpect(jsonPath("$.publicationDate").value(sameInstant(DEFAULT_PUBLICATION_DATE)));
    }

    @Test
    @Transactional
    public void getNonExistingPublication() throws Exception {
        // Get the publication
        restPublicationMockMvc.perform(get("/api/publications/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updatePublication() throws Exception {
        // Initialize the database
        publicationRepository.saveAndFlush(publication);
        int databaseSizeBeforeUpdate = publicationRepository.findAll().size();

        // Update the publication
        Publication updatedPublication = publicationRepository.findOne(publication.getId());
        // Disconnect from session so that the updates on updatedPublication are not directly saved in db
        em.detach(updatedPublication);
        updatedPublication
            .title(UPDATED_TITLE)
            .text(UPDATED_TEXT)
            .publicationDate(UPDATED_PUBLICATION_DATE);

        restPublicationMockMvc.perform(put("/api/publications")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedPublication)))
            .andExpect(status().isOk());

        // Validate the Publication in the database
        List<Publication> publicationList = publicationRepository.findAll();
        assertThat(publicationList).hasSize(databaseSizeBeforeUpdate);
        Publication testPublication = publicationList.get(publicationList.size() - 1);
        assertThat(testPublication.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(testPublication.getText()).isEqualTo(UPDATED_TEXT);
        assertThat(testPublication.getPublicationDate()).isEqualTo(UPDATED_PUBLICATION_DATE);
    }

    @Test
    @Transactional
    public void updateNonExistingPublication() throws Exception {
        int databaseSizeBeforeUpdate = publicationRepository.findAll().size();

        // Create the Publication

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restPublicationMockMvc.perform(put("/api/publications")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(publication)))
            .andExpect(status().isCreated());

        // Validate the Publication in the database
        List<Publication> publicationList = publicationRepository.findAll();
        assertThat(publicationList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deletePublication() throws Exception {
        // Initialize the database
        publicationRepository.saveAndFlush(publication);
        int databaseSizeBeforeDelete = publicationRepository.findAll().size();

        // Get the publication
        restPublicationMockMvc.perform(delete("/api/publications/{id}", publication.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Publication> publicationList = publicationRepository.findAll();
        assertThat(publicationList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Publication.class);
        Publication publication1 = new Publication();
        publication1.setId(1L);
        Publication publication2 = new Publication();
        publication2.setId(publication1.getId());
        assertThat(publication1).isEqualTo(publication2);
        publication2.setId(2L);
        assertThat(publication1).isNotEqualTo(publication2);
        publication1.setId(null);
        assertThat(publication1).isNotEqualTo(publication2);
    }
}
