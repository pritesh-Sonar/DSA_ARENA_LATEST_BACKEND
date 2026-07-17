package com.dsaarena.dsa_arena_backend.game.matchmaking;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import java.security.Principal;

@Controller
public class MatchmakingController {

    private final MatchmakingService matchmakingService;

    public MatchmakingController(MatchmakingService matchmakingService) {
        this.matchmakingService = matchmakingService;
    }

    @MessageMapping("/matchmake.join")
    public void joinQueue(Principal principal) {
        if (principal == null) {
            System.out.println("🚨 Drop request aborted: Inbound matchmake.join principal parameter is NULL.");
            return;
        }

        System.out.println("🎮 Matchmaking join queue requested by: " + principal.getName());
        matchmakingService.addToQueue(principal.getName());
    }

    @MessageMapping("/matchmake.leave")
    public void leaveQueue(Principal principal) {
        if (principal == null) {
            System.out.println("🚨 Drop request aborted: Inbound matchmake.leave principal parameter is NULL.");
            return;
        }

        System.out.println("🎮 Matchmaking leave queue requested by: " + principal.getName());
        matchmakingService.removeFromQueue(principal.getName());
    }
}