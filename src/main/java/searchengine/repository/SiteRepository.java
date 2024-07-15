package searchengine.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Site;


@Repository
@Transactional
public interface SiteRepository extends JpaRepository<Site, Integer> {

    Site findFirstSiteByUrl(String url);

}
