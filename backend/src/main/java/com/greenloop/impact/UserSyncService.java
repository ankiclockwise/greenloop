package com.greenloop.impact;

import com.greenloop.model.User;
import com.greenloop.model.UserRole;
import com.greenloop.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserSyncService {

    private final UserRepository userRepository;
    private final UserImpactRepository userImpactRepository;

    public UserSyncService(UserRepository userRepository, UserImpactRepository userImpactRepository) {
        this.userRepository = userRepository;
        this.userImpactRepository = userImpactRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public User getOrCreate(String email, String name, UserRole role) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User u = new User();
            u.setEmail(email);
            u.setName(name != null && !name.isBlank() ? name : email.split("@")[0]);
            u.setRole(role);
            return userRepository.save(u);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UserImpact getOrCreateImpact(User user) {
        return userImpactRepository.findByUserId(user.getId())
                .orElseGet(() -> userImpactRepository.save(UserImpact.forUser(user)));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Optional<UserImpact> findImpactByUserId(Long userId) {
        return userImpactRepository.findByUserId(userId);
    }
}
