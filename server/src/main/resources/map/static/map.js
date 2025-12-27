/**
 * Google Maps WebView - Main JavaScript
 * Two-way communication with Compose Multiplatform
 */

// Global variables
let map;
let marker;
let circle;
let autocomplete;
let isMapInitialized = false;

// Input fields
let latInput;
let lngInput;
let radInput;

// Prevent infinite loops when updating
let isUpdatingFromMap = false;
let isUpdatingFromInput = false;

// Default values (will be overridden by Kotlin)
let currentLat = -6.2;
let currentLng = 106.816666;
let currentRadius = 1000;
let currentZoom = 18;

/**
 * Initialize the Google Map
 * Called automatically by Google Maps API when loaded
 */
function initMap() {
  currentLat = GOOGLE_MAPS_CONFIG.options.initialLat;
  currentLng = GOOGLE_MAPS_CONFIG.options.initialLong;
  currentRadius = GOOGLE_MAPS_CONFIG.options.initialRad;

  console.log("Initializing map...");
  showLoading(true);

  const center = { lat: currentLat, lng: currentLng };

  // Create the map
  map = new google.maps.Map(document.getElementById("map"), {
    zoom: currentZoom,
    center: center,
    mapTypeControl: true,
    mapTypeControlOptions: {
      position: google.maps.ControlPosition.TOP_RIGHT,
    },
    streetViewControl: true,
    streetViewControlOptions: {
      position: google.maps.ControlPosition.RIGHT_BOTTOM,
    },
    fullscreenControl: true,
    fullscreenControlOptions: {
      position: google.maps.ControlPosition.RIGHT_BOTTOM,
    },
    zoomControl: true,
    zoomControlOptions: {
      position: google.maps.ControlPosition.RIGHT_BOTTOM,
    },
  });

  // Initialize Places Autocomplete
  const input = document.getElementById("pac-input");
  autocomplete = new google.maps.places.Autocomplete(input, {
    fields: ["geometry", "name", "formatted_address", "place_id"],
  });

  // Bias autocomplete results to map's viewport
  autocomplete.bindTo("bounds", map);

  // Create marker
  marker = new google.maps.Marker({
    position: center,
    map: map,
    title: "Location: " + currentLat.toFixed(6) + ", " + currentLng.toFixed(6),
    draggable: true,
    animation: google.maps.Animation.DROP,
  });

  // Create circle if radius > 0
  if (currentRadius > 0) {
    createCircle(center, currentRadius);
  }

  // Set up event listeners
  setupEventListeners();

  // Initialize text field controls
  initializeTextFieldControls();

  isMapInitialized = true;
  showLoading(false);

  // Send initial state to Kotlin
  updateMapKmpBridge(currentLat, currentLng, currentRadius);

  console.log("Map initialized successfully");
}

/**
 * Initialize text field controls and their event listeners
 */
function initializeTextFieldControls() {
  latInput = document.getElementById("lat-input");
  lngInput = document.getElementById("lng-input");
  radInput = document.getElementById("rad-input");

  // Set initial values
  updateTextFields(currentLat, currentLng, currentRadius);

  // Add input event listeners with debouncing
  let inputTimeout;

  const handleInputChange = () => {
    if (isUpdatingFromMap) return; // Prevent loop

    clearTimeout(inputTimeout);
    inputTimeout = setTimeout(() => {
      const lat = parseFloat(latInput.value);
      const lng = parseFloat(lngInput.value);
      const rad = parseFloat(radInput.value) || 0;

      // Validate inputs
      if (isNaN(lat) || isNaN(lng)) {
        console.warn("Invalid latitude or longitude");
        return;
      }

      if (lat < -90 || lat > 90) {
        console.warn("Latitude must be between -90 and 90");
        return;
      }

      if (lng < -180 || lng > 180) {
        console.warn("Longitude must be between -180 and 180");
        return;
      }

      if (rad < 0) {
        console.warn("Radius must be positive");
        return;
      }

      console.log(`Text field changed: ${lat}, ${lng}, ${rad}`);

      // Update map from text field input
      isUpdatingFromInput = true;
      updateMapLocation(lat, lng, rad);

      // Send to Kotlin
      updateMapKmpBridge(lat, lng, rad);

      isUpdatingFromInput = false;
    }, 500); // 500ms debounce
  };

  latInput.addEventListener("input", handleInputChange);
  lngInput.addEventListener("input", handleInputChange);
  radInput.addEventListener("input", handleInputChange);

  // Also listen for Enter key
  const handleEnterKey = (event) => {
    if (event.key === "Enter") {
      clearTimeout(inputTimeout);
      handleInputChange();
    }
  };

  latInput.addEventListener("keypress", handleEnterKey);
  lngInput.addEventListener("keypress", handleEnterKey);
  radInput.addEventListener("keypress", handleEnterKey);
}

/**
 * Update text fields with current values
 */
function updateTextFields(lat, lng, rad) {
  if (isUpdatingFromInput) return; // Prevent loop

  isUpdatingFromMap = true;

  if (latInput) latInput.value = lat.toFixed(6);
  if (lngInput) lngInput.value = lng.toFixed(6);
  if (radInput) radInput.value = Math.round(rad);

  isUpdatingFromMap = false;
}

/**
 * Create or update the radius circle
 */
function createCircle(center, radius) {
  if (circle) {
    circle.setMap(null);
  }

  if (radius > 0) {
    circle = new google.maps.Circle({
      strokeColor: "#FF0000",
      strokeOpacity: 0.8,
      strokeWeight: 2,
      fillColor: "#FF0000",
      fillOpacity: 0.15,
      map: map,
      center: center,
      radius: radius,
    });
  }
}

/**
 * Set up all event listeners
 */
function setupEventListeners() {
  // Map click listener
  map.addListener("click", function (event) {
    handleLocationChange(event.latLng, "click");
  });

  // Marker drag listener
  marker.addListener("dragend", function (event) {
    handleLocationChange(event.latLng, "drag");
  });

  // Autocomplete place selection
  autocomplete.addListener("place_changed", function () {
    const place = autocomplete.getPlace();

    if (!place.geometry || !place.geometry.location) {
      console.log("No details available for: '" + place.name + "'");
      return;
    }

    handleLocationChange(place.geometry.location, "search", place);
  });
}

/**
 * Handle location changes from any source (click, drag, search)
 */
function handleLocationChange(location, source, place) {
  const lat = location.lat();
  const lng = location.lng();

  console.log(`Location changed via ${source}: ${lat}, ${lng}`);

  // Update marker
  marker.setPosition(location);

  if (place && place.name) {
    marker.setTitle(
      place.name + " (" + lat.toFixed(6) + ", " + lng.toFixed(6) + ")"
    );
  } else {
    marker.setTitle("Location: " + lat.toFixed(6) + ", " + lng.toFixed(6));
  }

  // Update circle
  if (circle) {
    circle.setCenter(location);
  }

  // Center map on new location
  if (source === "search") {
    map.setCenter(location);

    // Smart zoom based on place type
    if (place.geometry.viewport) {
      map.fitBounds(place.geometry.viewport);
    } else {
      map.setZoom(17);
    }

    // Clear search input
    document.getElementById("pac-input").value = "";
  } else if (source === "click") {
    // Smooth pan to new location
    map.panTo(location);
  }

  // Update current values
  currentLat = lat;
  currentLng = lng;
  currentRadius = circle ? circle.getRadius() : currentRadius;

  // Update text fields
  updateTextFields(lat, lng, currentRadius);

  // Send coordinates back to Kotlin
  updateMapKmpBridge(lat, lng, currentRadius);
}

/**
 * Send updates to Kotlin via bridge
 * This is the JS -> Kotlin communication
 */
function updateMapKmpBridge(lat, lng, rad) {
  if (typeof window.kmpJsBridge !== "undefined") {
    try {
      window.kmpJsBridge.callNative(
        "updateMapKmpBridge",
        JSON.stringify({ lat: lat, long: lng, rad: rad }),
        null // No callback needed
      );
      console.log(`Sent to Kotlin: lat=${lat}, lng=${lng}, rad=${rad}`);
    } catch (error) {
      console.error("Error calling Kotlin bridge:", error);
    }
  } else {
    console.warn("kmpJsBridge not available (might be on desktop)");
  }
}

/**
 * Update map location from Kotlin
 * This is called from Kotlin via evaluateJavaScript or the updateMap function
 * This is the Kotlin -> JS communication
 */
function updateMapLocation(lat, lng, radius) {
  if (!isMapInitialized) {
    console.log("Map not ready, queueing update...");
    setTimeout(() => updateMapLocation(lat, lng, radius), 100);
    return;
  }

  console.log(`Updating map from external call: ${lat}, ${lng}, ${radius}`);

  currentLat = lat;
  currentLng = lng;
  currentRadius = radius;

  const newCenter = { lat: lat, lng: lng };

  // Update map center with smooth animation
  map.panTo(newCenter);

  // Update marker
  marker.setPosition(newCenter);
  marker.setTitle("Location: " + lat.toFixed(6) + ", " + lng.toFixed(6));

  // Update or create circle
  createCircle(newCenter, radius);

  // Update text fields
  updateTextFields(lat, lng, radius);

  // Fit bounds to circle if radius > 0
  if (circle && radius > 0) {
    setTimeout(() => {
      map.fitBounds(circle.getBounds());
    }, 300);
  }
}

/**
 * Show/hide loading indicator
 */
function showLoading(show) {
  const loadingElement = document.getElementById("loading");
  if (loadingElement) {
    loadingElement.style.display = show ? "block" : "none";
  }
}

/**
 * Error handler
 */
window.addEventListener("error", function (event) {
  console.error("JavaScript error:", event.error);
});

document.addEventListener("DOMContentLoaded", () => {
    const applyBtn = document.getElementById("apply-btn");

    applyBtn.addEventListener("click", () => {
        const lat = parseFloat(document.getElementById("lat-input").value);
        const lng = parseFloat(document.getElementById("lng-input").value);
        const rad = parseFloat(document.getElementById("rad-input").value);

        if (isNaN(lat) || isNaN(lng) || isNaN(rad)) {
            alert("Please fill out all fields.");
            return;
        }

        // Update map UI
        updateMapLocation(lat, lng, rad);

        // Send data to Kotlin
        if (window.kmpJsBridge) {
            window.kmpJsBridge.callNative(
                "submitMapDataKmpBridge",
                JSON.stringify({ lat, long: lng, rad }),
                null
            );
        }

        console.log("Submitted:", lat, lng, rad);
    });
});


// Log when script is loaded
console.log("Map script loaded, waiting for Google Maps API...");