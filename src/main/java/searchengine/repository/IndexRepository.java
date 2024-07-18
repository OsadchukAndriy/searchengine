package searchengine.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.Index;
import searchengine.model.Lemma;

import java.util.List;

@Repository
@Transactional
public interface IndexRepository extends JpaRepository<Index, Integer> {
//    @Modifying
//    @Query(value = "delete i from `index` i inner join lemma l on i.lemma_id = l.id where l.site_id=:siteId", nativeQuery = true)
//    void deleteIndexesBySiteId(@Param("siteId") Integer siteId);

    List<Index> findIndexByLemma(Lemma lemma);
}
