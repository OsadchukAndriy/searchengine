package searchengine.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Data
@Entity(name = "`index`")
public class Index {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "page_id", referencedColumnName = "id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Page page;

    @ManyToOne
    @JoinColumn(name = "lemma_id", referencedColumnName = "id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Lemma lemma;

    @Column(name = "`rank`")
    private float rank;

    @Override
    public String toString() {
        return "Index{" +
                "id=" + id +
                ", page_id=" + page.getId() +
                ", lemma_id=" + lemma.getId() +
                ", lemma=" + lemma.getLemma() +
                ", rank=" + rank +
                '}';
    }
}
