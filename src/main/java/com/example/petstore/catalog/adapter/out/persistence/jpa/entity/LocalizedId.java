package com.example.petstore.catalog.adapter.out.persistence.jpa.entity;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key shared by all localized {@code *_details} tables: the owning entity id
 * plus the locale. Field names ({@code id}, {@code locale}) match the {@code @Id} fields on each
 * detail entity, as required by {@code @IdClass}.
 */
public class LocalizedId implements Serializable {

    private String id;
    private String locale;

    public LocalizedId() {
    }

    public LocalizedId(String id, String locale) {
        this.id = id;
        this.locale = locale;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LocalizedId that)) {
            return false;
        }
        return Objects.equals(id, that.id) && Objects.equals(locale, that.locale);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, locale);
    }
}
