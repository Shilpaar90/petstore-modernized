package com.example.petstore.catalog.adapter.out.persistence.jpa.repository;

import com.example.petstore.catalog.adapter.out.persistence.jpa.entity.LocalizedId;
import com.example.petstore.catalog.adapter.out.persistence.jpa.entity.ProductDetailEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductDetailJpaRepository extends Repository<ProductDetailEntity, LocalizedId> {

    @Query("select d from ProductDetailEntity d "
            + "where d.product.catid = :catid and d.locale = :locale order by d.id asc")
    List<ProductDetailEntity> findByCategoryAndLocale(@Param("catid") String catid,
                                                      @Param("locale") String locale);

    Optional<ProductDetailEntity> findByIdAndLocale(String id, String locale);
}
