package searchengine.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name="site_id")
    private Site site;

    @Column(columnDefinition = "TEXT")
    @EqualsAndHashCode.Include
    String path;

    @Column
    int code;

    @Column(columnDefinition = "MEDIUMTEXT")
    String content;

    @OneToMany(mappedBy = "page",  targetEntity = Index.class)
    private List<Page> indexes;

    @Override
    public String toString() {
        return "Page{" +
                "id=" + id +
                ", site_id=" + site.getId() +
                ", path='" + path + '\'' +
                ", code=" + code +
                '}';
    }
}