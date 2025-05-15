let map;

// Initialize the map with a center and zoom level
function initializeMap() {
    map = L.map('map').setView([46.638780, 11.350111], 9);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19
    }).addTo(map);

    loadMarkers(map);
}

// Zooms to a parking station when selected
function changeMap(lat, lng) {
    if (map && typeof map.flyTo === 'function') {
        map.flyTo([lat, lng], 17, {
            animate: true,
            duration: 2
        });
    }
}

// Load markers on the map from the /api/points endpoint
function loadMarkers(map) {
    fetch('/api/points')
        .then(response => response.json())
        .then(data => {
            Object.entries(data).forEach(([name, point]) => {
                if (point.lat && point.lng) {
                    createMarker(map, name, point);
                }
            });
        })
        .catch(error => console.error('Error loading markers:', error));
}

// Create a marker on the map with a popup
function createMarker(map, name, point) {
    L.marker([point.lat, point.lng])
        .addTo(map)
        .bindPopup(name || 'Unnamed Station')
        .on('click', function () {
            fetchAndDisplayStation(name);
        });
}

//Change dropdown
function handleDropdownChange() {
    const dropdown = document.getElementById('parkingDropdown');
    dropdown.addEventListener('change', function () {
        const selectedName = this.value;
        if (!selectedName) return;

        fetchAndDisplayStation(selectedName);
    });
}

// Fetch and display station data
function fetchAndDisplayStation(name) {
    const infoDiv = document.getElementById('stationInfo');
    const loading = document.getElementById('loading');

    loading.style.display = 'block';
    infoDiv.innerHTML = '';

    fetch(`/api/stationData?name=${encodeURIComponent(name)}`)
        .then(response => response.json())
        .then(data => {
            loading.style.display = 'none';
            displayStationData(data);
            if (data.coordinates) {
                changeMap(data.coordinates.lat, data.coordinates.lng);
            }
        })
        .catch(error => {
            loading.style.display = 'none';
            infoDiv.innerHTML = `<p>Error loading station data.</p>`;
            console.error(error);
        });
}

// Display the station data in the infoDiv
function displayStationData(data) {
    if (data) {
        const firstTimestamp = data.timestamps[0];
        const firstValue = data.free_spots[0];

        const timeAgo = calculateTimeAgo(firstTimestamp);

        let infoHTML = `
            <div class="card mt-3">
                <div class="card-body">
                    <h5 class="card-title">${data.name}</h5>
                    <p class="card-text"><strong>Municipality:</strong> ${data.municipality}</p>
                    <p class="card-text"><strong>Capacity:</strong> ${data.capacity}</p>
        `;

        if (timeAgo === 'No real-time data available for this station') {
            infoHTML += `<p class="card-text" style="color: red;">${timeAgo}</p>`;
        } else {
            infoHTML += `<p class="card-text"><strong>Most Recent Free Spots:</strong> ${firstValue}</p>
                         <p class="card-text"><strong>Last Updated:</strong> ${timeAgo}</p>`;
        }

        infoHTML += `</div></div>`;

        document.getElementById('stationInfo').innerHTML = infoHTML;
    }
}

// Calculate the time ago based on the timestamp
function calculateTimeAgo(timestamp) {
    const timestampDate = new Date(timestamp);
    const now = new Date();
    const diffMs = now - timestampDate; // Difference in milliseconds

    const diffSec = Math.floor(diffMs / 1000);
    const diffMin = Math.floor(diffSec / 60);
    const diffHours = Math.floor(diffMin / 60);
    const diffDays = Math.floor(diffHours / 24);

    if (diffHours > 10) {
        return 'No real-time data available for this station';
    }

    if (diffDays > 0) {
        return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`;
    } else if (diffHours > 0) {
        return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`;
    } else if (diffMin > 0) {
        return `${diffMin} minute${diffMin > 1 ? 's' : ''} ago`;
    } else {
        return `${diffSec} second${diffSec !== 1 ? 's' : ''} ago`;
    }
}

// Initialize everything
function init() {
    initializeMap();
    handleDropdownChange();
}

// Wait for the DOM to be fully loaded before running the scripts
document.addEventListener('DOMContentLoaded', init);
