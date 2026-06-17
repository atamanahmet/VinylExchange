package com.atamanahmet.vinylexchange.service;

import com.atamanahmet.vinylexchange.domain.entity.Page;
import com.atamanahmet.vinylexchange.domain.enums.PageType;
import com.atamanahmet.vinylexchange.repository.cms.PageRepository;
import com.atamanahmet.vinylexchange.service.media.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.atamanahmet.vinylexchange.dto.PageDTO;
import com.atamanahmet.vinylexchange.exception.PageNotFoundException;

@Service
@RequiredArgsConstructor
public class CmsService {

    private final PageRepository pageRepository;
    private final FileStorageService fileStorageService;

    public Page getPageByPageType(PageType pageType) {

        return pageRepository.findByPageType(pageType)
                        .orElseThrow(PageNotFoundException::new);
    }

    public Page savePage(Page page) {

        return pageRepository.save(page);
    }

    public Boolean existsByPageType(PageType pageType) {

        return pageRepository.existsByPageType(pageType);
    }

    public void deleteAll() {

        pageRepository.deleteAll();
    }

    public PageDTO getPageDTOByType(PageType pageType) {

        Page page = getPageByPageType(pageType);

        String textContent = fileStorageService.readTextContentFile(page.getTextContentPath());

        return new PageDTO(
                page.getHeader(),
                textContent,
                page.getBackgroundImagePath()
        );
    }
}
