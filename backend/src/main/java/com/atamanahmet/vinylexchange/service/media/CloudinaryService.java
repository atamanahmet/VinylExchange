package com.atamanahmet.vinylexchange.service.media;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import io.github.cdimascio.dotenv.Dotenv;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService() {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        this.cloudinary = new Cloudinary(dotenv.get("CLOUDINARY_URL"));
    }

    public String uploadImage(InputStream inputStream, String filename) throws IOException {
        Map params = ObjectUtils.asMap(
                "use_filename", true,
                "unique_filename", false,
                "overwrite", true);

        Map<String, Object> uploadResult = cloudinary.uploader().upload(inputStream, params);

        return uploadResult.get("secure_url").toString();
    }

    public String uploadImage(String url, String filename) throws IOException {
        Map<String, Object> params = ObjectUtils.asMap(
                "use_filename", true,
                "unique_filename", false,
                "overwrite", true);

        Map<String, Object> uploadResult = cloudinary.uploader().upload(url, params);

        return uploadResult.get("secure_url").toString();
    }
}
