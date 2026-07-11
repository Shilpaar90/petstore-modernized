package com.example.petstore.identity.adapter.out.persistence.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB projection of an application user — the document-store counterpart of the relational
 * {@code users} table. The username is the natural {@code _id}.
 */
@Document(collection = "users")
public class UserDocument {

    @Id
    private String username;
    private String password;
    private boolean enabled;

    protected UserDocument() {
    }

    public UserDocument(String username, String password, boolean enabled) {
        this.username = username;
        this.password = password;
        this.enabled = enabled;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
