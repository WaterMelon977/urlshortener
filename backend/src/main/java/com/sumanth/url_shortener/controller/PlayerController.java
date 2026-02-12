package com.sumanth.url_shortener.controller;

import com.sumanth.url_shortener.model.Player;
import com.sumanth.url_shortener.repository.PlayerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/players")
public class PlayerController {

    @Autowired
    private PlayerRepository playerRepository;

    @PostMapping
    public Player createPlayer(@RequestBody Player player) {
        return playerRepository.save(player);
    }

    @GetMapping("/all")
    public List<Player> getAllPlayers() {
        return playerRepository.findAll();
    }
}
