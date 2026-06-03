package com.atamanahmet.vinylexchange.service.media;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.atamanahmet.vinylexchange.infrastructure.CompressedImage;
import com.atamanahmet.vinylexchange.domain.enums.CompressionType;
import com.atamanahmet.vinylexchange.infrastructure.ImageSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

@Service
public class ImageCompressionService {

    private Logger logger = LoggerFactory.getLogger(ImageCompressionService.class);

    public List<CompressedImage> compressImages(List<ImageSource> images) throws IOException {

        List<CompressedImage> compressedImages = new ArrayList<>();

        for (ImageSource image : images) {

            byte[] originalBytes = image.getInputStream().readAllBytes();

            System.out.println(image.getSize() + "byte");

            // TODO: lossless new impl, webp lossless file size bigger than jpg
            // compare dpi before converst lossless

            // smaller than 1000kb no need to compress, webp conversion reduces too much dpi
            if (image.getSize() > 100_000) {

                byte[] lossyImage = compressLossy(originalBytes, 0.80f);

                String newFileName = changeExtension(image.getOriginalFilename(), ".webp");

                compressedImages.add(new CompressedImage(newFileName, lossyImage));

                logger.info("Image compressed: " + newFileName);

            } else {

                compressedImages.add(new CompressedImage(image.getOriginalFilename(), originalBytes));
            }
        }
        return compressedImages;
    }

    public byte[] compressLossy(byte[] imageBytes, Float quality) throws IOException {
        return compressImage(imageBytes, CompressionType.LOSSY, quality);
    }

    public byte[] compressLossless(byte[] imageBytes) throws IOException {
        return compressImage(imageBytes, CompressionType.LOSSLESS, null);
    }

    public byte[] compressImage(byte[] imageBytes, CompressionType compressionType, Float quality) throws IOException {

        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));

        if (bufferedImage == null) {
            throw new IllegalArgumentException("Invalid image file: bufferedImage compress");
        }

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("webp");

        if (!writers.hasNext()) {
            throw new IllegalStateException("No WebP writer found");
        }

        ImageWriter writer = writers.next();

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(outputStream);) {

            ImageWriteParam writeParam = writer.getDefaultWriteParam();

            // System.out.println("can write comppresed " +
            // writeParam.canWriteCompressed());

            writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            writeParam.setCompressionType(compressionType.toString());

            if (compressionType.equals(CompressionType.LOSSY)) {
                writeParam.setCompressionQuality(0.85f);
            }

            writer.setOutput(imageOutputStream);

            writer.write(null, new IIOImage(bufferedImage, null, null), writeParam);

            imageOutputStream.flush();

            // System.out.println("output size after write: " + outputStream.size());

            return outputStream.toByteArray();

        } finally {
            writer.dispose();
        }
    }

    public static String changeExtension(String filename, String ext) {

        int i = filename.lastIndexOf(".");

        String newFileName = filename.substring(0, i) + ext;

        return newFileName;
    }
}
