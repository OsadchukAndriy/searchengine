package searchengine.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
public class Site {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    private SiteStatus status;

    @Column(name = "status_time")
    private LocalDateTime statusTime;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(length = 255)
    private String url;

    @Column(length = 255)
    private String name;

    @OneToMany(mappedBy = "site", targetEntity = Page.class)
    private List<Page> pages;

    @OneToMany(mappedBy = "site", targetEntity = Lemma.class)
    private List<Lemma> lemmas;

    @Override
    public String toString() {
        return "Page{" +
                "id=" + id +
                ", url=" + url +
                ", name=" + name +
                ", status=" + status +
                ", lastError='" + lastError + '\'' +
                '}';
    }

}
