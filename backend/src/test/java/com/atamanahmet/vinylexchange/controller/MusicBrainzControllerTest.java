package com.atamanahmet.vinylexchange.controller;

import com.atamanahmet.vinylexchange.dto.musicbrainz.ReleaseDTO;
import com.atamanahmet.vinylexchange.service.MusicBrainzService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MusicBrainzControllerTest {

    @Mock
    private MusicBrainzService musicBrainzService;

    @InjectMocks
    private MusicBrainzController musicBrainzController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(musicBrainzController).build();
    }

    @Test
    void search_withQuery_returnsOkAndBody() throws Exception {
        UUID releaseId = UUID.fromString("a1000001-0001-4001-8001-000000000001");
        ReleaseDTO dto = ReleaseDTO.builder()
                .id(releaseId)
                .title("Abbey Road")
                .build();
        when(musicBrainzService.searchReleases("abbey road", "title", 75, 0))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/mb/search")
                        .param("query", "abbey road")
                        .param("scope", "title")
                        .param("limit", "75")
                        .param("offset", "0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Abbey Road"));

        verify(musicBrainzService).searchReleases("abbey road", "title", 75, 0);
    }

    @Test
    void search_withTitleParam_usesTitleWhenQueryMissing() throws Exception {
        when(musicBrainzService.searchReleases("abbey road", "title", 20, 0))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/mb/search").param("title", "abbey road"))
                .andExpect(status().isOk());

        verify(musicBrainzService).searchReleases("abbey road", "title", 20, 0);
    }

    @Test
    void search_withOffset_forwardsPaginationToService() throws Exception {
        when(musicBrainzService.searchReleases("abbey road", "title", 75, 75))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/mb/search")
                        .param("query", "abbey road")
                        .param("limit", "75")
                        .param("offset", "75"))
                .andExpect(status().isOk());

        verify(musicBrainzService).searchReleases(eq("abbey road"), eq("title"), eq(75), eq(75));
    }

    @Test
    void search_withoutQueryOrTitle_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/mb/search"))
                .andExpect(status().isBadRequest());
    }
}
