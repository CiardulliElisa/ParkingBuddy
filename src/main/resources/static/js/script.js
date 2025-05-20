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
            setDropdownValue(name);
        });
}

// Set the dropdown to the value of the clicked marker
function setDropdownValue(stationName) {
    let dropdown = document.getElementById('parkingDropdown');
    for (let i = 0; i < dropdown.options.length; i++) {
        if (dropdown.options[i].value === stationName) {
            dropdown.selectedIndex = i;
            break;
        }
    }
    dropdown.dispatchEvent(new Event('change'));
}

// Handle station dropdown changes by adding event listener
function handleDropdownChange() {
    let dropdown = document.getElementById('parkingDropdown');
    let infoDiv = document.getElementById('stationInfo');
    let loading = document.getElementById('loading');
    let datePickerContainer = document.getElementById('datePickerContainer');
    let submitBtn = document.getElementById('submitBtn');

    dropdown.addEventListener('change', function () {
        let selectedName = this.value;

        // Toggle date picker and submit button visibility
        if (selectedName) {
            if (datePickerContainer) datePickerContainer.style.display = 'block';
            if (submitBtn) submitBtn.style.display = 'inline-block';
        } else {
            if (datePickerContainer) datePickerContainer.style.display = 'none';
            if (submitBtn) submitBtn.style.display = 'none';
            infoDiv.innerHTML = '';
            return;
        }

        if (loading) loading.style.display = 'block';
        infoDiv.innerHTML = '';

        fetch(`/api/stationData?name=${encodeURIComponent(selectedName)}`)
            .then(response => response.json())
            .then(data => {
                if (loading) loading.style.display = 'none';

                if (data.coordinates) {
                    changeMap(data.coordinates.lat, data.coordinates.lng);
                }
                if (data && data.timestamps && data.timestamps.length > 0 && data.free_spots && data.free_spots.length > 0) {
                    displayStationData(data);
                } else {
                    infoDiv.innerHTML = '<p>No data available.</p>';
                }
            })
            .catch(error => {
                if (loading) loading.style.display = 'none';
                infoDiv.innerHTML = '<p>Error loading station data.</p>';
                console.error(error);
            });
    });
}

// Display the station data in the infoDiv
function displayStationData(data) {
    if (!data) {
        document.getElementById('stationInfo').innerHTML = '<p>No data to display.</p>';
        return;
    }

    const firstTimestamp = data.timestamps[0];
    const firstValue = data.free_spots[0];

    const timeAgo = calculateTimeAgo(firstTimestamp);

    let infoHTML = `
        <div class="card mt-3">
            <div class="card-body">
                <h5 class="card-title">${data.name || 'Unknown Station'}</h5>
                <p class="card-text"><strong>Municipality:</strong> ${data.municipality || 'N/A'}</p>
                <p class="card-text"><strong>Capacity:</strong> ${data.capacity || 'N/A'}</p>
    `;

    if (timeAgo === 'No real-time data available for this station') {
        infoHTML += `<p class="card-text text-danger">${timeAgo}</p>`;
    } else {
        infoHTML += `
            <p class="card-text"><strong>Most Recent Free Spots:</strong> ${firstValue}</p>
            <p class="card-text"><strong>Last Updated:</strong> ${timeAgo}</p>
        `;
    }

    infoHTML += '</div></div>';

    document.getElementById('stationInfo').innerHTML = infoHTML;
}

// Calculate how long ago the data was updated
function calculateTimeAgo(timestamp) {
    const timestampDate = new Date(timestamp);
    const now = new Date();
    const diffMs = now - timestampDate;

    if (isNaN(diffMs)) {
        return 'Invalid timestamp';
    }

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

// Run when DOM ready
function init() {
    initializeMap();
    handleDropdownChange();
}

// Wait for the DOM to be fully loaded before running the scripts
document.addEventListener('DOMContentLoaded', init);

