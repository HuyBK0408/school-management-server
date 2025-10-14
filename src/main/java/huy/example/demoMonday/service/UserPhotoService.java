package huy.example.demoMonday.service;

import huy.example.demoMonday.repository.UserPhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserPhotoService {
    private final UserPhotoRepository userPhotoRepository;

    @Transactional
    public String saveAndAttachToUser(UUID userId, MultipartFile file){
        return userPhotoRepository.saveAndAttachToUser(userId, file);
    }
}
