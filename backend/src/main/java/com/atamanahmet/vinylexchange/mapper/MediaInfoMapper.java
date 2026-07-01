package com.atamanahmet.vinylexchange.mapper;

import org.springframework.stereotype.Component;

import com.atamanahmet.vinylexchange.domain.embeddable.MediaInfo;
import com.atamanahmet.vinylexchange.domain.enums.MediaFormat;
import com.atamanahmet.vinylexchange.dto.listing.MediaInfoDTO;

@Component
public class MediaInfoMapper {

    public MediaInfo toEntity(MediaInfoDTO dto) {
        if (dto == null) {
            return null;
        }

        return MediaInfo.builder()
                .format(dto.getFormat())
                .vinylSubtype(dto.getFormat() == MediaFormat.VINYL ? dto.getVinylSubtype() : null)
                .speedRpm(dto.getFormat() == MediaFormat.VINYL ? dto.getSpeedRpm() : null)
                .vinylSize(dto.getFormat() == MediaFormat.VINYL ? dto.getVinylSize() : null)
                .discCount(discCountFor(dto))
                .colored(dto.getFormat() == MediaFormat.VINYL ? dto.getColored() : null)
                .pictureDisc(dto.getFormat() == MediaFormat.VINYL ? dto.getPictureDisc() : null)
                .sourceFormatRaw(dto.getSourceFormatRaw())
                .build();
    }

    public MediaInfoDTO toDto(MediaInfo entity) {
        return toDtoStatic(entity);
    }

    public static MediaInfoDTO toDtoStatic(MediaInfo entity) {
        if (entity == null || entity.getFormat() == null) {
            return null;
        }

        return MediaInfoDTO.builder()
                .format(entity.getFormat())
                .vinylSubtype(entity.getVinylSubtype())
                .speedRpm(entity.getSpeedRpm())
                .vinylSize(entity.getVinylSize())
                .discCount(entity.getDiscCount())
                .colored(entity.getColored())
                .pictureDisc(entity.getPictureDisc())
                .sourceFormatRaw(entity.getSourceFormatRaw())
                .build();
    }

    public void applyUpdate(MediaInfo target, MediaInfoDTO dto) {
        if (dto == null || dto.getFormat() == null) {
            return;
        }

        target.setFormat(dto.getFormat());
        target.setSourceFormatRaw(dto.getSourceFormatRaw());

        if (dto.getFormat() == MediaFormat.VINYL) {
            target.setVinylSubtype(dto.getVinylSubtype());
            target.setSpeedRpm(dto.getSpeedRpm());
            target.setVinylSize(dto.getVinylSize());
            target.setColored(dto.getColored());
            target.setPictureDisc(dto.getPictureDisc());
            target.setDiscCount(dto.getDiscCount() != null ? dto.getDiscCount() : 1);
            return;
        }

        target.setVinylSubtype(null);
        target.setSpeedRpm(null);
        target.setVinylSize(null);
        target.setColored(null);
        target.setPictureDisc(null);
        target.setDiscCount(dto.getFormat() == MediaFormat.CD
                ? (dto.getDiscCount() != null ? dto.getDiscCount() : 1)
                : null);
    }

    private Integer discCountFor(MediaInfoDTO dto) {
        if (dto.getFormat() == MediaFormat.VINYL || dto.getFormat() == MediaFormat.CD) {
            return dto.getDiscCount() != null ? dto.getDiscCount() : 1;
        }
        return null;
    }
}
