package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.Lemma;
import searchengine.model.Site;

import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {

    @Modifying
    @Query(value = "delete from lemma where site_id=:siteId", nativeQuery = true)
    void deleteLemmasBySiteId(@Param("siteId") Integer siteId);

    List<Lemma> findLemmaByLemmaAndSite(String lemma, Site site);

}

