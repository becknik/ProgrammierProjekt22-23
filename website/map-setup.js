function setupMapContents() {
    // This is used to add an OpenStreetMap background on top of the map
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
        maxZoom: 19,
        attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
    }).addTo(map);

    const uniIcon = L.icon({
        iconUrl: "/icons/jedi-temple-192px.png",
        iconSize: [84,42],
        iconAnchor: [42, 21]
    });

    const uniStuttgart = L.marker(
        [48.7470843, 9.1056518],
        {
            icon: uniIcon,
            opacity: 0.75
        }
    ).addTo(map)
        .bindPopup('<b>Universität Vaihingen</b>');
    uniStuttgart.openPopup();

    const janniksHomeIcon = L.icon({
        iconUrl: "/icons/mustafa.png",
        iconSize: [42,42],
        iconAnchor: [21, 21]
    });

    const janniksHome = L.marker(
        [48.875502006251175, 9.3526053428],
        {
            icon: janniksHomeIcon,
            opacity: 0.75
        }
    ).addTo(map)
        .bindPopup('<b>Jannik\'s Home</b>')

    const timsHome = L.marker([48.7325457341223, 9.141590595245363])
        .addTo(map)
        .bindPopup('<b>Tim\'s Home</b>');

    const silasHome = L.marker([48.76716202993186, 9.176596105098726])
        .addTo(map)
        .bindPopup('<b>Silas\' Home</b>');
}