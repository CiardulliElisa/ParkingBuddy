function getQueryParam(name) {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(name);
}

function loadCharts() {
    const station = getQueryParam("station");
    const date = getQueryParam("date");

    if (!station || !date) {
        console.error("Missing station or date in URL");
        return;
    }

    Highcharts.setOptions({
        lang: {
            decimalPoint: '.',
            thousandsSep: ','
        }
    });

    // Fetch and render chart
    fetch(`/api/dataPoints?station=${encodeURIComponent(station)}`)
        .then(response => response.json())
        .then(dataPoints => {
            Highcharts.chart('chart', {
                title: { text: 'Last 7 days trend' },
                xAxis: { categories: dataPoints.map(point => point.timestamp) },
                yAxis: { title: { text: 'Free Slots' }},
                series: [{
                    type: 'line',
                    name: station,
                    data: dataPoints.map(point => point.freeSlots)
                }]
            });
        }).catch(error => console.error('Error displaying history chart:', error));

    // Fetch and render prediction
    fetch(`/api/prediction?station=${encodeURIComponent(station)}&date=${encodeURIComponent(date)}`)
        .then(response => response.json())
        .then(dataPoints => {
            Highcharts.chart('prediction', {
                title: { text: 'Prediction' },
                xAxis: { categories: dataPoints.map(point => point.timestamp) },
                yAxis: { title: { text: 'Free Slots' }},
                series: [{
                    type: 'line',
                    name: station,
                    data: dataPoints.map(point => point.freeSlots)
                }]
            });
        }).catch(error => console.error('Error displaying prediction chart:', error));
}

window.addEventListener('DOMContentLoaded', () => {
    loadCharts();

    const header = document.getElementById('header-clickable');
    if (header) {
        header.addEventListener('click', () => {
            localStorage.removeItem('mapCenter');
            localStorage.removeItem('mapZoom');
            window.location.href = '/';
        });
    }
});