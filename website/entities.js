const jediTempleIcon = L.icon({
    iconUrl: "/icons/jedi-temple-192px.png",
    iconSize: [84, 42],
    iconAnchor: [42, 21]
});

const jediUniStuttgart = L.marker(
    [48.7470843, 9.1056518],
    {
        icon: jediTempleIcon,
        opacity: 0.75
    });

const uniStuttgart = L.marker([48.7470843, 9.1056518]);

const mustafaIcon = L.icon({
    iconUrl: "/icons/mustafa.png",
    iconSize: [42, 42],
    iconAnchor: [21, 21]
});

const mustafaHome = L.marker(
    [48.875502006251175, 9.3526053428],
    {
        icon: mustafaIcon,
        opacity: 0.75
    });

const janniksHome = L.marker([48.875502006251175, 9.3526053428]);

const timsHome = L.marker([48.7325457341223, 9.141590595245363]);

const silasHome = L.marker([48.76716202993186, 9.176596105098726]);


const defaultStartIcon = L.icon({
    iconUrl: "https://cdn-icons-png.flaticon.com/512/2776/2776000.png",
    iconSize: [42, 42],
    iconAnchor: [21, 42]
});
const defaultTargetIcon = L.icon({
    iconUrl: "https://cdn-icons-png.flaticon.com/512/1363/1363376.png",
    iconSize: [42, 42],
    iconAnchor: [40, 0]
});
const emperorIcon = L.icon({
    // https://cdn-icons-png.flaticon.com/512/2776/2776000.png
    iconUrl: "/icons/emperor-96px.png",
    iconSize: [84, 84],
    iconAnchor: [42, 42]
});
const yodaIcon = L.icon({
    //iconUrl: "https://cdn-icons-png.flaticon.com/512/1363/1363376.png",
    iconUrl: "/icons/yoda-96px.png",
    iconSize: [168, 84],
    iconAnchor: [25, 40]
});

