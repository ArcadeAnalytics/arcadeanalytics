package com.arcadeanalytics.web.rest;

/*-
 * #%L
 * Arcade Analytics
 * %%
 * Copyright (C) 2018 - 2019 ArcadeAnalytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.arcadeanalytics.domain.DataSet;
import com.arcadeanalytics.repository.DataSetRepository;
import com.arcadeanalytics.repository.search.DataSetSearchRepository;
import com.arcadeanalytics.security.AuthoritiesConstants;
import com.arcadeanalytics.security.SecurityUtils;
import com.arcadeanalytics.service.dto.DataSetDTO;
import com.arcadeanalytics.service.mapper.DataSetMapper;
import com.arcadeanalytics.web.rest.errors.BadRequestAlertException;
import com.arcadeanalytics.web.rest.util.HeaderUtil;
import com.arcadeanalytics.web.rest.util.PaginationUtil;
import com.codahale.metrics.annotation.Timed;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

/**
 * REST controller for managing DataSet.
 */
@RestController
@RequestMapping("/api")
public class DataSetResource {

    private static final String ENTITY_NAME = "dataSet";
    private final Logger log = LoggerFactory.getLogger(DataSetResource.class);
    private final DataSetRepository dataSetRepository;

    private final DataSetMapper dataSetMapper;

    private final DataSetSearchRepository dataSetSearchRepository;

    public DataSetResource(DataSetRepository dataSetRepository, DataSetMapper dataSetMapper, DataSetSearchRepository dataSetSearchRepository) {
        this.dataSetRepository = dataSetRepository;
        this.dataSetMapper = dataSetMapper;
        this.dataSetSearchRepository = dataSetSearchRepository;
    }

    /**
     * POST  /data-sets : Create a new dataSet.
     *
     * @param dataSetDTO the dataSetDTO to create
     * @return the ResponseEntity with status 201 (Created) and with body the new dataSetDTO, or with status 400 (Bad Request) if the dataSet has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/data-sets")
    @Timed
    public ResponseEntity<DataSetDTO> createDataSet(@Valid @RequestBody DataSetDTO dataSetDTO) throws URISyntaxException {
        log.debug("REST request to save DataSet : {}", dataSetDTO);
        if (dataSetDTO.getId() != null) {
            throw new BadRequestAlertException("A new dataSet cannot already have an ID", ENTITY_NAME, "idexists");
        }
        DataSet dataSet = dataSetMapper.toEntity(dataSetDTO);
        dataSet = dataSetRepository.save(dataSet);
        DataSetDTO result = dataSetMapper.toDto(dataSet);
        dataSetSearchRepository.save(dataSet);
        return ResponseEntity.created(new URI("/api/data-sets/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
                .body(result);
    }

    /**
     * PUT  /data-sets : Updates an existing dataSet.
     *
     * @param dataSetDTO the dataSetDTO to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated dataSetDTO,
     * or with status 400 (Bad Request) if the dataSetDTO is not valid,
     * or with status 500 (Internal Server Error) if the dataSetDTO couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/data-sets")
    @Timed
    public ResponseEntity<DataSetDTO> updateDataSet(@Valid @RequestBody DataSetDTO dataSetDTO) throws URISyntaxException {
        log.debug("REST request to update DataSet : {}", dataSetDTO);
        if (dataSetDTO.getId() == null) {
            return createDataSet(dataSetDTO);
        }
        DataSet dataSet = dataSetMapper.toEntity(dataSetDTO);
        dataSet = dataSetRepository.save(dataSet);
        DataSetDTO result = dataSetMapper.toDto(dataSet);
        dataSetSearchRepository.save(dataSet);
        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, dataSetDTO.getId().toString()))
                .body(result);
    }

    /**
     * GET  /data-sets : get all the dataSets.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of dataSets in body
     */
    @GetMapping("/data-sets")
    @Timed
    public ResponseEntity<List<DataSetDTO>> getAllDataSets(Pageable pageable) {
        log.debug("REST request to get a page of DataSets");

        Page<DataSet> page;
        if (SecurityUtils.isCurrentUserInRole(AuthoritiesConstants.ADMIN)) {
            page = dataSetRepository.findAll(pageable);
        } else {
            page = dataSetRepository.findByWidgetDashboardWorkspaceUserUserLogin(SecurityUtils.getCurrentUserLogin().get(), pageable);
        }

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/data-sets");
        return new ResponseEntity<>(dataSetMapper.toDto(page.getContent()), headers, HttpStatus.OK);
    }

    /**
     * GET  /data-sets/:id : get the "id" dataSet.
     *
     * @param id the id of the dataSetDTO to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the dataSetDTO, or with status 404 (Not Found)
     */
    @GetMapping("/data-sets/{id}")
    @Timed
    public ResponseEntity<DataSetDTO> getDataSet(@PathVariable Long id) {
        log.debug("REST request to get DataSet : {}", id);
        DataSet dataSet = dataSetRepository.findOne(id);
        DataSetDTO dataSetDTO = dataSetMapper.toDto(dataSet);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(dataSetDTO));
    }

    /**
     * DELETE  /data-sets/:id : delete the "id" dataSet.
     *
     * @param id the id of the dataSetDTO to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/data-sets/{id}")
    @Timed
    public ResponseEntity<Void> deleteDataSet(@PathVariable Long id) {
        log.debug("REST request to delete DataSet : {}", id);
        dataSetRepository.delete(id);
        dataSetSearchRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

    /**
     * SEARCH  /_search/data-sets?query=:query : search for the dataSet corresponding
     * to the query.
     *
     * @param query    the query of the dataSet search
     * @param pageable the pagination information
     * @return the result of the search
     */
    @GetMapping("/_search/data-sets")
    @Timed
    public ResponseEntity<List<DataSetDTO>> searchDataSets(@RequestParam String query, Pageable pageable) {
        log.debug("REST request to search for a page of DataSets for query {}", query);
        Page<DataSet> page = dataSetSearchRepository.search(queryStringQuery(query), pageable);
        HttpHeaders headers = PaginationUtil.generateSearchPaginationHttpHeaders(query, page, "/api/_search/data-sets");
        return new ResponseEntity<>(dataSetMapper.toDto(page.getContent()), headers, HttpStatus.OK);
    }

}
