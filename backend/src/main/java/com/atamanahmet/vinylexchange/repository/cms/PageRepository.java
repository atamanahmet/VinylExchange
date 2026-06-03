package com.atamanahmet.vinylexchange.repository.cms;

import java.util.Optional;
import java.util.UUID;

import com.atamanahmet.vinylexchange.domain.entity.Page;
import com.atamanahmet.vinylexchange.domain.enums.PageType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PageRepository extends JpaRepository<Page, UUID> {
    Optional<Page> findByPageType(PageType pageType);

    boolean existsByPageType(PageType pageType);
}
