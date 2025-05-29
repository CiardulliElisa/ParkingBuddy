// Returns the parameter of the fetch request
function getQueryParam(name) {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get(name);
}

// Fetch prediction data and render the prediction chart
function fetchAndRenderPredictionChart(station, date, capacity) {
    fetch(`/api/prediction?station=${encodeURIComponent(station)}&date=${encodeURIComponent(date)}&capacity=${encodeURIComponent(capacity)}`)
        .then(response => response.json())
        .then(dataPoints => {
            Highcharts.chart('prediction', {
                title: { text: 'Prediction' },
                xAxis: {
                    type: 'datetime',
                    labels: { format: '{value:%H:%M}' },
                    title: { text: 'Date' }
                },
                tooltip: {
                    xDateFormat: '%Y-%m-%d %H:%M',
                    shared: true
                },
                yAxis: {
                    min: 0,
                    max: capacity,
                    title: { text: 'Free Slots' },
                    plotLines: [{
                        value: capacity,
                        color: 'red',
                        width: 2,
                        dashStyle: 'ShortDash',
                        label: {
                            text: 'Maximal Capacity ' + capacity,
                            align: 'right',
                            style: { color: 'red' }
                        }
                    }]
                },
                series: [{
                    type: 'line',
                    name: 'Free Slots',
                    data: dataPoints.map(point => ({
                        x: new Date(point.timestamp).getTime(),
                        y: point.value
                    }))
                }]
            });
            document.getElementById("prediction-loading").style.display = "none";
            document.getElementById("prediction").style.display = "block";
        })
        .catch(error => {
            console.error('Error displaying prediction chart:', error);
            document.getElementById("prediction-loading").innerHTML = "<p style='color:red;'>Failed to load chart data.</p>";
        });
}

// Fetch historical data and render the trend chart
function fetchAndRenderTrendChart(station, capacity) {
    fetch(`/api/dataPoints?station=${encodeURIComponent(station)}&capacity=${encodeURIComponent(capacity)}`)
        .then(response => response.json())
        .then(dataPoints => {
            Highcharts.chart('chart', {
                title: { text: 'Last 7 days trend' },
                xAxis: {
                    type: 'datetime',
                    labels: { format: '{value:%Y-%m-%d}' },
                    title: { text: 'Date' }
                },
                tooltip: {
                    xDateFormat: '%Y-%m-%d %H:%M',
                    shared: true
                },
                yAxis: {
                    min: 0,
                    max: capacity,
                    title: { text: 'Free Slots' },
                    plotLines: [{
                        value: capacity,
                        color: 'red',
                        width: 2,
                        dashStyle: 'ShortDash',
                        label: {
                            text: 'Maximal Capacity ' + capacity,
                            align: 'right',
                            style: { color: 'red' }
                        }
                    }]
                },
                series: [{
                    name: 'Free Slots',
                    data: dataPoints.map(point => ({
                        x: new Date(point.timestamp).getTime(),
                        y: point.value
                    }))
                }]
            });
            document.getElementById("chart-loading").style.display = "none";
            document.getElementById("chart").style.display = "block";
        })
        .catch(error => {
            console.error('Error displaying history chart:', error);
            document.getElementById("chart-loading").innerHTML = "<p style='color:red;'>Failed to load chart data.</p>";
        });
}

// Function to load the trend and prediction charts
function loadCharts() {
    const station = getQueryParam("station");
    const date = getQueryParam("date");
    const capacity = parseInt(getQueryParam("capacity"));

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

    fetchAndRenderPredictionChart(station, date, capacity);
    fetchAndRenderTrendChart(station, capacity);
}

// Event listener to trigger the loading of chart content
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