package searchengine.model.entity;

import javax.persistence.*;

@Entity
@Table(name = "indexs")
public class Indexs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "page_id", nullable = false, updatable = false, insertable = false)
    private int pages;
    @Column(name = "lemma_id", nullable = false, updatable = false, insertable = false)
    private int lemmaId;
    @Column(nullable = false)
    private float ranked;
}
