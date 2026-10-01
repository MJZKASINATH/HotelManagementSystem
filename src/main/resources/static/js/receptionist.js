const API_BASE = '/api/receptionist';
const adminId = localStorage.getItem('adminId') || 'R100'; // Default receptionist adminId

document.addEventListener('DOMContentLoaded', () => {
    loadAvailableRooms();
    loadServiceRequests();
});

// Process Offline Booking
document.getElementById('offlineBookingForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const payload = {
        guestName: document.getElementById('guestName').value,
        idProof: document.getElementById('idProof').value,
        mobileNumber: document.getElementById('mobileNumber').value,
        roomNumber: document.getElementById('roomNumber').value,
        checkInDate: document.getElementById('checkInDate').value,
        checkOutDate: document.getElementById('checkOutDate').value,
        adminId: adminId
    };

    try {
        const response = await fetch(`${API_BASE}/offline-booking`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        if (response.ok) {
            const guest = await response.json();
            document.getElementById('bookingResult').innerHTML = 
                `Booking successful! Assigned Guest ID: <strong>${guest.guestId}</strong> (Provide this to guest for Portal Login)`;
            loadAvailableRooms();
            document.getElementById('offlineBookingForm').reset();
        } else {
            document.getElementById('bookingResult').innerText = 'Failed to process booking.';
        }
    } catch (err) {
        console.error(err);
    }
});

// Check-In Guest
async function performCheckIn() {
    const bookingNo = document.getElementById('bookingNoInput').value;
    if (!bookingNo) return;
    const response = await fetch(`${API_BASE}/check-in/${bookingNo}`, { method: 'PUT' });
    const result = await response.text();
    document.getElementById('actionResult').innerText = result;
    loadAvailableRooms();
}

// Check-Out Guest
async function performCheckOut() {
    const bookingNo = document.getElementById('bookingNoInput').value;
    if (!bookingNo) return;
    const response = await fetch(`${API_BASE}/check-out/${bookingNo}`, { method: 'PUT' });
    const result = await response.text();
    document.getElementById('actionResult').innerText = result;
    loadAvailableRooms();
}

// Fetch Available Rooms
async function loadAvailableRooms() {
    const response = await fetch(`${API_BASE}/rooms/available`);
    const rooms = await response.json();
    const list = document.getElementById('roomsList');
    list.innerHTML = rooms.map(r => `<li>Room No: <strong>${r.roomNumber}</strong> - ${r.roomType}</li>`).join('');
}

// Fetch Service Requests
async function loadServiceRequests() {
    const response = await fetch(`${API_BASE}/service-requests`);
    const requests = await response.json();
    const tableBody = document.getElementById('serviceRequestsTable');
    tableBody.innerHTML = requests.map(req => `
        <tr>
            <td>${req.requestId}</td>
            <td>${req.serviceId}</td>
            <td>${req.guestId}</td>
            <td>${req.adminId || 'Unassigned'}</td>
            <td>
                <button onclick="assignRequest('${req.requestId}')">Assign to Me</button>
            </td>
        </tr>
    `).join('');
}

// Assign Service Request
async function assignRequest(requestId) {
    const response = await fetch(`${API_BASE}/service-requests/${requestId}/assign?adminId=${adminId}`, { method: 'PUT' });
    if (response.ok) {
        loadServiceRequests();
    }
}
