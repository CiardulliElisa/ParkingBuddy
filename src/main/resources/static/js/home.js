let map;
let points = [];

// Initialize the map with a center and zoom level
function initializeMap() {
   const savedCenter = JSON.parse(localStorage.getItem('mapCenter'));
    const savedZoom = parseInt(localStorage.getItem('mapZoom'), 10);

    const defaultCenter = [46.638780, 11.350111];
    const defaultZoom = 9;

    const center = (savedCenter && savedCenter.length === 2) ? savedCenter : defaultCenter;
    const zoom = (!isNaN(savedZoom)) ? savedZoom : defaultZoom;

    map = L.map('map').setView(center, zoom);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19
    }).addTo(map);

    loadMarkers(map);

    //saves current zoom level and center
    map.on('moveend zoomend', () => {
        const center = map.getCenter();
        const zoom = map.getZoom();
        localStorage.setItem('mapCenter', JSON.stringify([center.lat, center.lng]));
        localStorage.setItem('mapZoom', zoom);
    });
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

// Zooms to a municipality when selected
function changeMapMunicipality() {
    let dropdown = document.getElementById('parkingDropdown');
    if (!dropdown) {
        console.warn('Dropdown not found');
        return;
    }
    let dropdownStationNames = Array.from(dropdown.options)
        .map(option => option.value)
        .filter(name => name);

    let filteredPoints = points.filter(p => dropdownStationNames.includes(p.name));
    if (filteredPoints.length == 0) {
        console.warn('No matching points found in dropdown.');
        return;
    }
    const avgLat = filteredPoints.reduce((sum, p) => sum + p.lat, 0) / filteredPoints.length;
    const avgLng = filteredPoints.reduce((sum, p) => sum + p.lng, 0) / filteredPoints.length;
    
    if (map && typeof map.flyTo === 'function') {
        map.flyTo([avgLat, avgLng], 13, {
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
            points = [];
            Object.entries(data).forEach(([name, point]) => {
                if (point.lat && point.lng) {
                    points.push({ name, lat: point.lat, lng: point.lng });
                    createMarker(map, name, point);
                }
            });
            let selectedRadio = document.querySelector('input[name="municipality"]:checked');
            if (selectedRadio) {
                changeMapMunicipality();
            }
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
    let found = false;
    for (let i = 0; i < dropdown.options.length; i++) {
        if (dropdown.options[i].value === stationName) {
            dropdown.selectedIndex = i;
            found = true;
            break;
        }
    }
    //makes sure, that you can click on stations outside of municipality, too
    if (found) {
        dropdown.dispatchEvent(new Event('change'));
    } else {
        localStorage.setItem('pendingStation', stationName);

        const baseUrl = window.location.origin + window.location.pathname;
        window.location.href = baseUrl;
    }
}

// Handle station dropdown changes,
function handleDropdownChange() {
    let dropdown = document.getElementById('parkingDropdown');
    let infoDiv = document.getElementById('stationInfo');
    let loading = document.getElementById('loading');
    let datePickerContainer = document.getElementById('datePickerContainer');
    let submitBtn = document.getElementById('submitBtn');

    dropdown.addEventListener('change', function () {
        let selectedName = this.value;

        if (selectedName) {
            datePickerContainer.style.display = 'block';
            submitBtn.style.display = 'inline-block';
        } else {
            datePickerContainer.style.display = 'none';
            submitBtn.style.display = 'none';
            infoDiv.innerHTML = '';
            return;
        }

        loading.style.display = 'block';
        infoDiv.innerHTML = '';

        fetch(`/api/stationData?name=${encodeURIComponent(selectedName)}`)
            .then(response => response.json())
            .then(data => {
                loading.style.display = 'none';

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
                loading.style.display = 'none';
                infoDiv.innerHTML = '<p>Error loading station data.</p>';
                console.error(error);
            });
    });
}

// Display the selected station details
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

// Calculate how long ago the given timestamp was
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

// Wait for the DOM to be fully loaded before running the scripts
window.addEventListener('DOMContentLoaded', () => {
    initializeMap();
    handleDropdownChange();

    const header = document.getElementById('header-clickable');
    if (header) {
        header.addEventListener('click', () => {
            localStorage.removeItem('mapCenter');
            localStorage.removeItem('mapZoom');
            window.location.href = '/';
        });
    }

    const pendingStation = localStorage.getItem('pendingStation');
    if (pendingStation) {
        localStorage.removeItem('pendingStation');
        setDropdownValue(pendingStation);
    }
});

window.addEventListener('load', () => {
    loadMarkers(map);
});
