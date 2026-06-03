package com.atamanahmet.vinylexchange.domain.entity;

import java.util.UUID;

import com.atamanahmet.vinylexchange.domain.enums.PageType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pages")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "page_type")
    @Enumerated(EnumType.STRING)
    private PageType pageType;

    private String header;

    private String textContentPath;

    private String backgroundColor;

    private String backgroundImagePath;
}
