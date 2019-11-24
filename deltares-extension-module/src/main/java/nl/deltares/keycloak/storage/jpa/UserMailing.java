package nl.deltares.keycloak.storage.jpa;

import javax.persistence.*;

@NamedQueries({
        @NamedQuery(name = "findByUser", query = "from UserMailing where userId = :userId and realmId = :realmId"),
        @NamedQuery(name = "findByMailing", query = "from UserMailing where mailingId = :mailingId and realmId = :realmId")
})
@Entity
@Table(name = "USER_MAILING")
public class UserMailing {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "REALM_ID", nullable = false)
    private String realmId;

    @Column(name = "USER_ID", nullable = false)
    private String userId;

    @Column(name = "MAILING_ID", nullable = false)
    private String mailingId;

    @Column(name = "LANGUAGE", nullable = false)
    private String language="en";

    @Column(name = "DELIVERY", nullable = false)
    private int delivery=0;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        if (language == null) throw new IllegalArgumentException("missing language");
        if (language.length() > 2) throw new IllegalArgumentException("invalid language. expected two character country code. found " + language);
       this.language = language;

    }

    public int getDelivery() {
        return delivery;
    }

    public void setDelivery(int delivery) {
        if (delivery < 0 || delivery > 2) throw new IllegalArgumentException("invalid delivery " + delivery);
        this.delivery = delivery;
    }
}