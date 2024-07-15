package searchengine.model;


import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
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

    public void IncreaseFrequency(){
        frequency++;
    }

}
