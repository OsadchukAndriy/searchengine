package searchengine.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Data
@Entity
public class Page{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "site_id")
    private Site site;

    @Column(columnDefinition = "TEXT")
    @EqualsAndHashCode.Include
    private String path;

    @Column
    private int code;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String content;

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
