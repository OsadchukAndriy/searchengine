package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.Page;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface PageRepository extends JpaRepository<Page, Integer> {
    @Modifying
    @Query(value = "delete from page where site_id=:siteId", nativeQuery = true)
    void deletePagesBySiteId(@Param("siteId") Integer siteId);
}
