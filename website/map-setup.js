function setupMapContents() {
  // This is used to add an OpenStreetMap background on top of the map
  const tiles = L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>'
  }).addTo(map);

  const uniStuttgart = L.marker([48.7470843,9.1056518]).addTo(map)
          .bindPopup('<b>Universität Stuttgart/Vaihingen!</b>').openPopup();

  const janniksHome = L.marker([48.875502006251175,9.3526053428]).addTo(map)
          .bindPopup('<b>Jannik</b>').openPopup();

  const timsHome = L.marker([48.7325457341223,9.141590595245363]).addTo(map)
          .bindPopup('<b>Tim</b>').openPopup();

  const silasHome = L.marker([48.76716202993186, 9.176596105098726]).addTo(map)
          .bindPopup('<b>Silas</b>').openPopup();

          var currentLatitude = 0;
          var currentLongitude = 0;
}