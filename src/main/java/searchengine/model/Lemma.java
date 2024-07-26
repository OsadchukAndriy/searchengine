package searchengine.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@Entity
public class Lemma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "site_id")
    private Site site;

    @Column(length = 255)
    @EqualsAndHashCode.Include
    private String lemma;

    @Column
    private int frequency;

    @OneToMany(mappedBy = "lemma", targetEntity = Index.class)
    private List<Page> indexes;

    @Override
    public String toString() {
        return "Lemma{" +
                "id=" + id +
                ", site_id=" + site.getId() +
                ", lemma='" + lemma + '\'' +
                ", frequency=" + frequency +
                '}';
    }

    public void increaseFrequency(){
        frequency++;
    }

}
