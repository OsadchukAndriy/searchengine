package searchengine.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Lemma")
public class Lemma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "site_id")
    private Site site;

    @Column(length = 255)
    private String lemma;

    @Column(name = "frequency")
    private int frequency;

    @OneToMany(mappedBy = "lemma", targetEntity = Index.class)
    private List<Page> indexes;


    public void IncreaseFrequency(){
        frequency++;
    }

}
