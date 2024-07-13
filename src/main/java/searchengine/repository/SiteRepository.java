package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.model.Site;

import javax.transaction.Transactional;


@Repository
@Transactional
public interface SiteRepository extends JpaRepository<Site, Integer> {

    Site findFirstSiteByUrl(String url);

}
