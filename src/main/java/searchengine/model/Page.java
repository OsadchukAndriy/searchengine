package searchengine.model;

import lombok.*;


import javax.persistence.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Page")
public class Page{

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(name = "path", columnDefinition = "TEXT")
    private String path;

    @Column(name = "code")
    private int code;

    @Column(name = "content", columnDefinition = "MEDIUMTEXT")
    private String content;


}
