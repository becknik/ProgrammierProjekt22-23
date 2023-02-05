function setupMapContents() {
    // This is used to add an OpenStreetMap background on top of the map
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
    }).addTo(map);

    uniStuttgart.addTo(map)
        .bindPopup('<b>Universität Vaihingen</b>');

    janniksHome.addTo(map)
        .bindPopup('<b>Jannik\'s Home</b>')

    timsHome.addTo(map)
        .bindPopup('<b>Tim\'s Home</b>');

    silasHome.addTo(map)
        .bindPopup('<b>Silas\' Home</b>');
}