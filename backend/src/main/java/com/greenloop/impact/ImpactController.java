package com.greenloop.impact;

import com.greenloop.impact.dto.BadgeListResponse;
import com.greenloop.impact.dto.ImpactMeResponse;
import com.greenloop.impact.dto.LeaderboardResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/impact")
public class ImpactController {

    private final ImpactService impactService;

    public ImpactController(ImpactService impactService) {
        this.impactService = impactService;
    }

    @GetMapping("/me")
    public ResponseEntity<ImpactMeResponse> getMyImpact(
            @RequestParam String email,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String userType) {
        return ResponseEntity.ok(impactService.getMyImpact(email, name, userType));
    }

    @GetMapping("/badges")
    public ResponseEntity<BadgeListResponse> getBadges(
            @RequestParam String email,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String userType) {
        return ResponseEntity.ok(impactService.getAllBadges(email, name, userType));
    }

    @GetMapping("/leaderboard/donors")
    public ResponseEntity<LeaderboardResponse> getDonorLeaderboard(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(impactService.getDonorLeaderboard(page, limit));
    }

    @GetMapping("/leaderboard/students")
    public ResponseEntity<LeaderboardResponse> getStudentLeaderboard(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(impactService.getStudentLeaderboard(page, limit));
    }
}
