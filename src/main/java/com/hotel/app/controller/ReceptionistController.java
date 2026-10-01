package com.hotel.app.controller;

import com.hotel.app.dto.OfflineBookingDto;
import com.hotel.app.entity.Guest;
import com.hotel.app.entity.Room;
import com.hotel.app.entity.ServiceRequest;
import com.hotel.app.service.ReceptionistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/receptionist")
@CrossOrigin(origins = "*")
public class ReceptionistController {

    @Autowired
    private ReceptionistService receptionistService;

    @GetMapping("/rooms/available")
    public ResponseEntity<List<Room>> getAvailableRooms() {
        return ResponseEntity.ok(receptionistService.getAvailableRooms());
    }

    @PostMapping("/offline-booking")
    public ResponseEntity<Guest> offlineBooking(@RequestBody OfflineBookingDto dto) {
        Guest newGuest = receptionistService.processOfflineBooking(dto);
        return ResponseEntity.ok(newGuest);
    }

    @PutMapping("/check-in/{bookingNo}")
    public ResponseEntity<String> checkIn(@PathVariable String bookingNo) {
        receptionistService.checkInGuest(bookingNo);
        return ResponseEntity.ok("Check-in successful for booking: " + bookingNo);
    }

    @PutMapping("/check-out/{bookingNo}")
    public ResponseEntity<String> checkOut(@PathVariable String bookingNo) {
        receptionistService.checkOutGuest(bookingNo);
        return ResponseEntity.ok("Check-out successful for booking: " + bookingNo);
    }

    @GetMapping("/service-requests")
    public ResponseEntity<List<ServiceRequest>> getServiceRequests() {
        return ResponseEntity.ok(receptionistService.getPendingServiceRequests());
    }

    @PutMapping("/service-requests/{requestId}/assign")
    public ResponseEntity<String> assignService(@PathVariable String requestId, @RequestParam String adminId) {
        receptionistService.assignServiceToAdmin(requestId, adminId);
        return ResponseEntity.ok("Service request assigned successfully.");
    }
}
