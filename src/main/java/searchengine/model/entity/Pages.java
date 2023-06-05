package searchengine.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.persistence.Index;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(indexes = @Index(name = "path", columnList = "path"), name = "pages")
public class Pages {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;
    @Column(name = "path", columnDefinition = "VARCHAR(255)", nullable = false)
    private String path;
    @Column(nullable = false)
    private int code;
    @Column(columnDefinition = "MEDIUMTEXT", nullable = false)
    private String content;
}
