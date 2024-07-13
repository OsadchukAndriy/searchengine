package searchengine.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Index")
public class Index {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int Id;

    @ManyToOne
    @JoinColumn(name = "page_id", referencedColumnName = "id")
    private Page page;

    @ManyToOne
    @JoinColumn(name = "lemma_id", referencedColumnName = "id")
    private Lemma lemma;

    @Column(name = "'rank'")
    private float rank;


}
