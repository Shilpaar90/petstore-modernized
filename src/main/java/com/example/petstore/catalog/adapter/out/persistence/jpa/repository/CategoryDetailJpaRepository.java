package com.example.petstore.catalog.adapter.out.persistence.jpa.repository;

import com.example.petstore.catalog.adapter.out.persistence.jpa.entity.CategoryDetailEntity;
import com.example.petstore.catalog.adapter.out.persistence.jpa.entity.LocalizedId;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface CategoryDetailJpaRepository extends Repository<CategoryDetailEntity, LocalizedId> {

    List<CategoryDetailEntity> findByLocaleOrderByIdAsc(String locale);

    Optional<CategoryDetailEntity> findByIdAndLocale(String id, String locale);
}
