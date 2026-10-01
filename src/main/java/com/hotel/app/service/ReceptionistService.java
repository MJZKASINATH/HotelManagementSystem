package com.hotel.app.service;

import com.hotel.app.dto.OfflineBookingDto;
import com.hotel.app.entity.*;
import com.hotel.app.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ReceptionistService {

    @Autowired private RoomRepository roomRepository;
    @Autowired private GuestRepository guestRepository;
    @Autowired private GuestPhoneRepository guestPhoneRepository;
    @Autowired private BookingRepository bookingRepository;
    @Autowired private BookingRoomRepository bookingRoomRepository;
    @Autowired private ServiceRequestRepository serviceRequestRepository;

    public List<Room> getAvailableRooms() {
        return roomRepository.findByAvailability(true);
    }

    @Transactional
    public Guest processOfflineBooking(OfflineBookingDto dto) {
        // 1. Generate unique Guest ID and save Guest record
        String generatedGuestId = "G" + System.currentTimeMillis() % 10000;
        Guest guest = new Guest();
        guest.setGuestId(generatedGuestId);
        guest.setName(dto.getGuestName());
        guest.setIdProof(dto.getIdProof());
        guestRepository.save(guest);

        // 2. Save Guest Mobile Phone
        GuestPhone guestPhone = new GuestPhone();
        guestPhone.setGuestId(generatedGuestId);
        guestPhone.setMobileNumber(dto.getMobileNumber());
        guestPhoneRepository.save(guestPhone);

        // 3. Create Booking Record
        String bookingNo = "B" + System.currentTimeMillis() % 10000;
        Booking booking = new Booking();
        booking.setBookingNo(bookingNo);
        booking.setCheckInDate(dto.getCheckInDate());
        booking.setCheckOutDate(dto.getCheckOutDate());
        booking.setGuestId(generatedGuestId);
        booking.setAdminId(dto.getAdminId());
        bookingRepository.save(booking);

        // 4. Map Room to Booking & update room availability
        BookingRoom bookingRoom = new BookingRoom();
        bookingRoom.setBookingNo(bookingNo);
        bookingRoom.setRoomNo(dto.getRoomNumber());
        bookingRoomRepository.save(bookingRoom);

        Room room = roomRepository.findById(dto.getRoomNumber())
                .orElseThrow(() -> new RuntimeException("Room not found"));
        room.setAvailability(false);
        roomRepository.save(room);

        return guest; // Returns guest object containing generated guest_id for login credentials
    }

    @Transactional
    public void checkInGuest(String bookingNo) {
        List<BookingRoom> bookingRooms = bookingRoomRepository.findByBookingNo(bookingNo);
        for (BookingRoom br : bookingRooms) {
            Room room = roomRepository.findById(br.getRoomNo()).orElse(null);
            if (room != null) {
                room.setAvailability(false);
                roomRepository.save(room);
            }
        }
    }

    @Transactional
    public void checkOutGuest(String bookingNo) {
        List<BookingRoom> bookingRooms = bookingRoomRepository.findByBookingNo(bookingNo);
        for (BookingRoom br : bookingRooms) {
            Room room = roomRepository.findById(br.getRoomNo()).orElse(null);
            if (room != null) {
                room.setAvailability(true);
                roomRepository.save(room);
            }
        }
    }

    public List<ServiceRequest> getPendingServiceRequests() {
        return serviceRequestRepository.findAll();
    }

    public void assignServiceToAdmin(String requestId, String adminId) {
        ServiceRequest req = serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        req.setAdminId(adminId);
        serviceRequestRepository.save(req);
    }
}
