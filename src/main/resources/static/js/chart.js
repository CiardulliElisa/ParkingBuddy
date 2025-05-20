//load the graph content for prediction and 7 days review
function loadCharts() {
    Highcharts.setOptions({
            lang: {
                decimalPoint: '.',
                thousandsSep: ','
            }
        });
    // Fetch and render chart
    fetch('/api/dataPoints')
        .then(response => response.json())
        .then(dataPoints => {
            console.log('DataPoints:', dataPoints); // <-- Add this
            Highcharts.chart('chart', {
                title: {text: 'Last 7 days trend'},
                xAxis: {
                    categories: dataPoints.map(point => point.timestamp)
                },
                yAxis: {title: {text: 'Value'}},
                series: [{
                    type: 'line',
                    name: 'Line 1',
                    data: dataPoints.map(point => point.freeSlots)
                }]
            });
        }).catch(error => console.error('Error displaying history chart:', error));

    // Fetch and render table
    fetch('/api/prediction' )
        .then(response => response.json())
        .then(dataPoints => {
            Highcharts.chart('prediction', {
                title: {text: 'Prediction'},
                xAxis: {
                    categories: dataPoints.map(point => point.timestamp)
                },
                yAxis: {title: {text: 'Value'}},
                series: [{
                    type: 'line',
                    name: 'Line 1',
                    data: dataPoints.map(point => point.freeSlots)
                }]
            });
        }).catch(error => console.error('Error displaying prediction chart:', error));
}

document.addEventListener('DOMContentLoaded', loadCharts);