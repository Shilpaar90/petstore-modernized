package com.example.petstore.catalog.adapter.out.persistence.jpa.repository;

import com.example.petstore.catalog.adapter.out.persistence.jpa.entity.ItemDetailEntity;
import com.example.petstore.catalog.adapter.out.persistence.jpa.entity.LocalizedId;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemDetailJpaRepository extends Repository<ItemDetailEntity, LocalizedId> {

    @Query("select d from ItemDetailEntity d "
            + "where d.item.productid = :productId and d.locale = :locale order by d.id asc")
    List<ItemDetailEntity> findByProductAndLocale(@Param("productId") String productId,
                                                  @Param("locale") String locale);

    Optional<ItemDetailEntity> findByIdAndLocale(String id, String locale);
}
