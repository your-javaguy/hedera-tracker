package com.hedera.tracker.controller;

import com.hedera.tracker.model.Alert;
import com.hedera.tracker.repository.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    @Autowired
    private AlertRepository alertRepository;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Get all alerts
     */
    @GetMapping
    public List<Alert> getAllAlerts() {
        return alertRepository.findTop50ByOrderByCreatedAtDesc();
    }
    
    /**
     * Get unacknowledged alerts
     */
    @GetMapping("/unacknowledged")
    public List<Alert> getUnacknowledgedAlerts() {
        return alertRepository.findByAcknowledged(false);
    }
    
    /**
     * Get alerts for a specific token
     */
    @GetMapping("/token/{tokenId}")
    public List<Alert> getAlertsByToken(@PathVariable String tokenId) {
        return alertRepository.findByTokenId(tokenId);
    }
    
    /**
     * Get a specific alert by ID
     */
    @GetMapping("/{alertId}")
    public ResponseEntity<Alert> getAlert(@PathVariable Long alertId) {
        Optional<Alert> alert = alertRepository.findById(alertId);
        return alert.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    /**
     * Acknowledge an alert
     */
    @PutMapping("/{alertId}/acknowledge")
    public ResponseEntity<Alert> acknowledgeAlert(@PathVariable Long alertId) {
        Optional<Alert> alertOpt = alertRepository.findById(alertId);
        
        if (alertOpt.isPresent()) {
            Alert alert = alertOpt.get();
            alert.setAcknowledged(true);
            alertRepository.save(alert);
            
            // Notify clients about the change
            notifyAlertChange();
            
            return ResponseEntity.ok(alert);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Acknowledge an alert via form submission
     */
    @PostMapping("/acknowledge")
    public String acknowledgeAlertForm(@RequestParam("alertId") Long alertId, RedirectAttributes redirectAttributes) {
        Optional<Alert> alertOpt = alertRepository.findById(alertId);
        if (alertOpt.isPresent()) {
            Alert alert = alertOpt.get();
            alert.setAcknowledged(true);
            alertRepository.save(alert);
            
            // Notify clients about the change
            notifyAlertChange();
            
            redirectAttributes.addFlashAttribute("message", "Alert acknowledged successfully.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Alert not found.");
        }
        return "redirect:/alerts";
    }
    
    /**
     * Helper method to notify clients about alert changes via WebSocket
     */
    private void notifyAlertChange() {
        // Send updated unacknowledged alerts count
        int unacknowledgedCount = alertRepository.findByAcknowledged(false).size();
        messagingTemplate.convertAndSend("/topic/alerts/count", unacknowledgedCount);
        
        // Send updated alerts list
        messagingTemplate.convertAndSend("/topic/alerts/unacknowledged", 
                alertRepository.findByAcknowledged(false));
        
        // Send full updated alerts list
        messagingTemplate.convertAndSend("/topic/alerts/all", 
                alertRepository.findTop50ByOrderByCreatedAtDesc());
    }
} 