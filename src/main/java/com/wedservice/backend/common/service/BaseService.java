package com.wedservice.backend.common.service;

import com.wedservice.backend.common.exception.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Base service providing common CRUD operations.
 */
public abstract class BaseService<E, ID> {

    protected abstract JpaRepository<E, ID> getRepository();
    protected abstract String getEntityName();

    public E findById(ID id) {
        return getRepository().findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(getEntityName() + " not found with id: " + id));
    }

    public void delete(ID id) {
        if (!getRepository().existsById(id)) {
            throw new ResourceNotFoundException(getEntityName() + " not found with id: " + id);
        }
        getRepository().deleteById(id);
    }
}
