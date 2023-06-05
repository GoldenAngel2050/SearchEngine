package searchengine.model.entity;

import javax.persistence.*;

@Entity
@Table(name = "lemma", uniqueConstraints={
        @UniqueConstraint( name = "lemma_siteId",  columnNames ={"lemma","site_id"})
})
public class Lemma {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;
    @Column(columnDefinition = "VARCHAR(255)", nullable = false)
    private String lemma;
    @Column(nullable = false)
    private int frequency;
}
