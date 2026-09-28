const apiBase = '/api';

async function fetchData(url) {
    const response = await fetch(url);
    if (!response.ok) {
        throw new Error('Request failed: ' + response.status);
    }
    return response.json();
}

function getStatusClass(status) {
    const value = String(status || '').toLowerCase();
    if (value === 'safe') return 'safe';
    if (value === 'warning') return 'warning';
    if (value === 'critical') return 'critical';
    if (value === 'normal') return 'normal';
    return 'unknown';
}

function getBridgeById(bridges, bridgeId) {
    return bridges.find(b => String(b.id) === String(bridgeId));
}

function getSensorById(sensors, sensorId) {
    return sensors.find(s => String(s.id) === String(sensorId));
}

function getLatestReadingForSensor(sensorId, readings) {
    return readings
        .filter(reading => String(reading.sensor?.id ?? reading.sensorId) === String(sensorId))
        .sort((a, b) => new Date(b.readingTime) - new Date(a.readingTime))[0] || null;
}

function formatDateTime(value) {
    if (!value) return 'N/A';
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? value : date.toLocaleString();
}

function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) modal.classList.add('open');
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) modal.classList.remove('open');
}

async function loadDashboard() {
    try {
        const [bridges, sensors, alerts, maintenance, readings] = await Promise.all([
            fetchData(`${apiBase}/bridges`),
            fetchData(`${apiBase}/sensors`),
            fetchData(`${apiBase}/alerts`),
            fetchData(`${apiBase}/maintenance`),
            fetchData(`${apiBase}/readings`)
        ]);

        document.getElementById('totalBridges').textContent = bridges.length;
        document.getElementById('totalSensors').textContent = sensors.length;
        document.getElementById('safeBridges').textContent = bridges.filter(b => String(b.status).toUpperCase() === 'SAFE').length;
        document.getElementById('warningBridges').textContent = bridges.filter(b => String(b.status).toUpperCase() === 'WARNING').length;
        document.getElementById('criticalBridges').textContent = bridges.filter(b => String(b.status).toUpperCase() === 'CRITICAL').length;
        document.getElementById('openAlerts').textContent = alerts.filter(a => String(a.status).toUpperCase() === 'OPEN').length;
        document.getElementById('pendingMaintenance').textContent = maintenance.filter(m => String(m.status).toUpperCase() === 'PENDING').length;

        renderBridgeTable(bridges);
        renderRecentAlerts(alerts, sensors, bridges);
        renderSensorMonitoring(sensors, readings, bridges);
        renderSensorReadings(readings, sensors, bridges);
        renderMaintenanceTable(maintenance, bridges);
        populateSelects(bridges, sensors);
    } catch (error) {
        console.error('Dashboard load error:', error);
    }
}

function renderBridgeTable(bridges) {
    const tableBody = document.getElementById('bridgeHealthTableBody');
    tableBody.innerHTML = '';

    bridges.forEach(bridge => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${bridge.bridgeCode || 'N/A'}</td>
            <td>${bridge.bridgeName || 'N/A'}</td>
            <td>${bridge.location || 'N/A'}</td>
            <td><span class="status-badge status-${getStatusClass(bridge.status)}">${bridge.status || 'UNKNOWN'}</span></td>
            <td>
                <div class="action-row">
                    <button class="action-btn edit-btn" data-id="${bridge.id}" data-type="bridge">Edit</button>
                    <button class="action-btn delete-btn" data-id="${bridge.id}" data-type="bridge">Delete</button>
                </div>
            </td>
        `;
        tableBody.appendChild(row);
    });
}

function renderRecentAlerts(alerts, sensors, bridges) {
    const alertList = document.getElementById('recentAlertsList');
    alertList.innerHTML = '';

    alerts.forEach(alert => {
        const sensor = getSensorById(sensors, alert.sensor?.id ?? alert.sensorId);
        const bridge = getBridgeById(bridges, alert.bridge?.id ?? alert.bridgeId);
        const severity = String(alert.severity || 'INFO').toUpperCase();
        const alertClass = severity === 'CRITICAL' ? 'critical' : severity === 'WARNING' ? 'warning' : 'info';

        const item = document.createElement('div');
        item.className = `alert-item ${alertClass}`;
        item.innerHTML = `
            <div class="alert-item-header">
                <strong>${bridge ? bridge.bridgeCode : 'Bridge'} / ${sensor ? sensor.sensorCode : 'Sensor'}</strong>
                <div class="action-row">
                    <span class="status-badge status-${getStatusClass(severity === 'CRITICAL' ? 'critical' : severity === 'WARNING' ? 'warning' : 'safe')}">${severity}</span>
                    ${String(alert.status).toUpperCase() === 'OPEN' ? `<button class="action-btn resolve-btn" data-id="${alert.id}" data-type="alert">Resolve</button>` : ''}
                </div>
            </div>
            <p>${alert.message || 'System alert detected'}</p>
            <small class="alert-time">${formatDateTime(alert.createdAt)}</small>
        `;
        alertList.appendChild(item);
    });
}

function renderSensorReadings(readings, sensors, bridges) {
    const tableBody = document.getElementById('sensorReadingsTableBody');
    tableBody.innerHTML = '';

    readings.forEach(reading => {
        const sensor = getSensorById(sensors, reading.sensor?.id ?? reading.sensorId);
        const bridge = sensor ? getBridgeById(bridges, sensor.bridge?.id ?? sensor.bridgeId) : null;
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${sensor ? sensor.sensorCode : 'Unknown sensor'}</td>
            <td>${bridge ? bridge.bridgeCode : 'N/A'}</td>
            <td>${reading.value ?? 'N/A'} ${sensor?.unit || ''}</td>
            <td><span class="status-badge status-${getStatusClass(reading.status)}">${reading.status || 'UNKNOWN'}</span></td>
            <td>${formatDateTime(reading.readingTime)}</td>
            <td><button class="action-btn delete-btn" data-id="${reading.id}" data-type="reading">Delete</button></td>
        `;
        tableBody.appendChild(row);
    });
}

function renderSensorMonitoring(sensors, readings, bridges) {
    const container = document.getElementById('sensorMonitoringList');
    container.innerHTML = '';

    sensors.forEach(sensor => {
        const bridge = getBridgeById(bridges, sensor.bridge?.id ?? sensor.bridgeId);
        const latestReading = getLatestReadingForSensor(sensor.id, readings);
        const readingValue = latestReading ? latestReading.value : 'N/A';
        const status = latestReading ? latestReading.status : sensor.status;

        const card = document.createElement('article');
        card.className = 'sensor-card';
        card.innerHTML = `
            <div class="sensor-card-header">
                <h3>${sensor.sensorCode}</h3>
                <span class="status-badge status-${getStatusClass(status)}">${status || sensor.status || 'UNKNOWN'}</span>
            </div>
            <div class="sensor-meta">
                <span>${sensor.sensorType}</span>
                <span>${bridge ? bridge.bridgeCode : 'N/A'}</span>
            </div>
            <div class="sensor-reading">${readingValue}<small> ${sensor.unit || ''}</small></div>
            <div class="sensor-threshold">
                <span>Threshold</span>
                <strong>${sensor.threshold || 0} ${sensor.unit || ''}</strong>
            </div>
            <div class="sensor-meta">
                <span>Location</span>
                <span>${sensor.location || 'N/A'}</span>
            </div>
        `;
        container.appendChild(card);
    });
}

function renderMaintenanceTable(maintenance, bridges) {
    const tableBody = document.getElementById('maintenanceTableBody');
    tableBody.innerHTML = '';

    maintenance.forEach(item => {
        const bridge = getBridgeById(bridges, item.bridge?.id ?? item.bridgeId);
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${bridge ? bridge.bridgeCode : 'N/A'}</td>
            <td>${item.issue || 'N/A'}</td>
            <td><span class="status-badge status-${getStatusClass(item.priority)}">${item.priority || 'LOW'}</span></td>
            <td><span class="status-badge status-${getStatusClass(item.status)}">${item.status || 'PENDING'}</span></td>
            <td>${item.assignedTeam || 'Unassigned'}</td>
            <td>
                <div class="action-row">
                    <button class="action-btn edit-btn" data-id="${item.id}" data-type="maintenance">Edit</button>
                    <button class="action-btn delete-btn" data-id="${item.id}" data-type="maintenance">Delete</button>
                </div>
            </td>
        `;
        tableBody.appendChild(row);
    });
}

function populateSelects(bridges, sensors) {
    const bridgeSelectSensor = document.getElementById('bridgeSelectSensor');
    const readingSensorSelect = document.getElementById('readingSensorSelect');
    const maintenanceBridgeSelect = document.getElementById('maintenanceBridgeSelect');

    bridgeSelectSensor.innerHTML = '<option value="">Select Bridge</option>' +
        bridges.map(bridge => `<option value="${bridge.id}">${bridge.bridgeCode}</option>`).join('');

    maintenanceBridgeSelect.innerHTML = '<option value="">Select Bridge</option>' +
        bridges.map(bridge => `<option value="${bridge.id}">${bridge.bridgeCode}</option>`).join('');

    readingSensorSelect.innerHTML = '<option value="">Select Sensor</option>' +
        sensors.map(sensor => `<option value="${sensor.id}">${sensor.sensorCode}</option>`).join('');
}

async function saveBridge(event) {
    event.preventDefault();
    const bridge = {
        bridgeCode: document.getElementById('bridgeCode').value,
        bridgeName: document.getElementById('bridgeName').value,
        location: document.getElementById('location').value,
        bridgeType: document.getElementById('bridgeType').value,
        length: Number(document.getElementById('length').value),
        constructionYear: Number(document.getElementById('constructionYear').value),
        numberOfCables: Number(document.getElementById('numberOfCables').value),
        status: document.getElementById('bridgeStatus').value
    };

    const id = document.getElementById('bridgeId').value;
    const url = id ? `${apiBase}/bridges/${id}` : `${apiBase}/bridges`;
    const method = id ? 'PUT' : 'POST';

    await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(bridge)
    });

    document.getElementById('bridgeForm').reset();
    closeModal('bridgeModal');
    loadDashboard();
}

async function saveSensor(event) {
    event.preventDefault();
    const sensor = {
        sensorCode: document.getElementById('sensorCode').value,
        sensorType: document.getElementById('sensorType').value,
        location: document.getElementById('sensorLocation').value,
        threshold: Number(document.getElementById('threshold').value),
        unit: document.getElementById('sensorUnit').value,
        status: document.getElementById('sensorStatus').value
    };

    const bridgeId = document.getElementById('bridgeSelectSensor').value;
    const id = document.getElementById('sensorId').value;

    if (id) {
        await fetch(`${apiBase}/sensors/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(sensor)
        });
    } else {
        await fetch(`${apiBase}/bridges/${bridgeId}/sensors`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(sensor)
        });
    }

    document.getElementById('sensorForm').reset();
    closeModal('sensorModal');
    loadDashboard();
}

async function saveReading(event) {
    event.preventDefault();
    const sensorId = document.getElementById('readingSensorSelect').value;
    const value = document.getElementById('readingValue').value;

    await fetch(`${apiBase}/sensors/${sensorId}/readings?value=${value}`, {
        method: 'POST'
    });

    document.getElementById('readingForm').reset();
    closeModal('readingModal');
    loadDashboard();
}

async function saveMaintenance(event) {
    event.preventDefault();
    const maintenance = {
        issue: document.getElementById('maintenanceIssue').value,
        priority: document.getElementById('maintenancePriority').value,
        assignedTeam: document.getElementById('assignedTeam').value,
        status: document.getElementById('maintenanceStatusSelect').value,
        createdAt: new Date().toISOString()
    };

    const bridgeId = document.getElementById('maintenanceBridgeSelect').value;
    const id = document.getElementById('maintenanceId').value;

    if (id) {
        await fetch(`${apiBase}/maintenance/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(maintenance)
        });
    } else {
        await fetch(`${apiBase}/bridges/${bridgeId}/maintenance`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(maintenance)
        });
    }

    document.getElementById('maintenanceForm').reset();
    closeModal('maintenanceModal');
    loadDashboard();
}

document.querySelectorAll('.open-modal-btn').forEach(button => {
    button.addEventListener('click', () => openModal(button.dataset.modal));
});

document.querySelectorAll('.modal-close, [data-close]').forEach(button => {
    button.addEventListener('click', () => closeModal(button.dataset.close));
});

document.querySelectorAll('.modal-backdrop').forEach(modal => {
    modal.addEventListener('click', (event) => {
        if (event.target === modal) {
            closeModal(modal.id);
        }
    });
});

document.getElementById('bridgeForm').addEventListener('submit', saveBridge);
document.getElementById('sensorForm').addEventListener('submit', saveSensor);
document.getElementById('readingForm').addEventListener('submit', saveReading);
document.getElementById('maintenanceForm').addEventListener('submit', saveMaintenance);

document.addEventListener('click', async (event) => {
    const action = event.target.closest('.edit-btn, .delete-btn, .resolve-btn');
    if (!action) return;

    const { id, type } = action.dataset;

    if (type === 'bridge' && action.classList.contains('delete-btn')) {
        await fetch(`${apiBase}/bridges/${id}`, { method: 'DELETE' });
        loadDashboard();
        return;
    }

    if (type === 'sensor' && action.classList.contains('delete-btn')) {
        await fetch(`${apiBase}/sensors/${id}`, { method: 'DELETE' });
        loadDashboard();
        return;
    }

    if (type === 'maintenance' && action.classList.contains('delete-btn')) {
        await fetch(`${apiBase}/maintenance/${id}`, { method: 'DELETE' });
        loadDashboard();
        return;
    }

    if (type === 'reading' && action.classList.contains('delete-btn')) {
        await fetch(`${apiBase}/readings/${id}`, { method: 'DELETE' });
        loadDashboard();
        return;
    }

    if (type === 'alert' && action.classList.contains('resolve-btn')) {
        await fetch(`${apiBase}/alerts/${id}/resolve`, { method: 'PUT' });
        loadDashboard();
        return;
    }

    if (type === 'bridge' && action.classList.contains('edit-btn')) {
        const bridge = await fetchData(`${apiBase}/bridges/${id}`);
        document.getElementById('bridgeId').value = bridge.id;
        document.getElementById('bridgeCode').value = bridge.bridgeCode;
        document.getElementById('bridgeName').value = bridge.bridgeName;
        document.getElementById('location').value = bridge.location;
        document.getElementById('bridgeType').value = bridge.bridgeType;
        document.getElementById('length').value = bridge.length;
        document.getElementById('constructionYear').value = bridge.constructionYear;
        document.getElementById('numberOfCables').value = bridge.numberOfCables;
        document.getElementById('bridgeStatus').value = bridge.status;
        openModal('bridgeModal');
        return;
    }

    if (type === 'sensor' && action.classList.contains('edit-btn')) {
        const sensor = await fetchData(`${apiBase}/sensors/${id}`);
        document.getElementById('sensorId').value = sensor.id;
        document.getElementById('sensorCode').value = sensor.sensorCode;
        document.getElementById('bridgeSelectSensor').value = sensor.bridge?.id || '';
        document.getElementById('sensorType').value = sensor.sensorType;
        document.getElementById('sensorLocation').value = sensor.location;
        document.getElementById('threshold').value = sensor.threshold;
        document.getElementById('sensorUnit').value = sensor.unit;
        document.getElementById('sensorStatus').value = sensor.status;
        openModal('sensorModal');
        return;
    }

    if (type === 'maintenance' && action.classList.contains('edit-btn')) {
        const item = await fetchData(`${apiBase}/maintenance/${id}`);
        document.getElementById('maintenanceId').value = item.id;
        document.getElementById('maintenanceBridgeSelect').value = item.bridge?.id || '';
        document.getElementById('maintenanceIssue').value = item.issue || '';
        document.getElementById('maintenancePriority').value = item.priority || 'LOW';
        document.getElementById('assignedTeam').value = item.assignedTeam || '';
        document.getElementById('maintenanceStatusSelect').value = item.status || 'PENDING';
        openModal('maintenanceModal');
    }
});

const signInScreen = document.getElementById('signInScreen');
const dashboardShell = document.getElementById('dashboardShell');
const signInForm = document.getElementById('signInForm');
const signInPassword = document.getElementById('signInPassword');

function showView(viewName, updateHash = true) {
    const navigationItems = document.querySelectorAll('.nav-item[data-view]');
    const selectedItem = Array.from(navigationItems).find(item => item.dataset.view === viewName) || navigationItems[0];
    const selectedView = selectedItem.dataset.view;

    document.querySelectorAll('[data-view-panel]').forEach(panel => {
        panel.hidden = panel.dataset.viewPanel !== selectedView;
    });

    navigationItems.forEach(item => {
        const isActive = item === selectedItem;
        item.classList.toggle('active', isActive);
        if (isActive) item.setAttribute('aria-current', 'page');
        else item.removeAttribute('aria-current');
    });

    document.getElementById('currentPageTitle').textContent = selectedItem.dataset.title;
    if (updateHash && window.location.hash !== `#${selectedView}`) {
        window.location.hash = selectedView;
    }
}

document.querySelectorAll('.nav-item[data-view]').forEach(item => {
    item.addEventListener('click', event => {
        event.preventDefault();
        showView(item.dataset.view);
    });
});

window.addEventListener('hashchange', () => {
    showView(window.location.hash.slice(1) || 'overview', false);
});

document.getElementById('togglePassword').addEventListener('click', (event) => {
    const isVisible = signInPassword.type === 'text';
    signInPassword.type = isVisible ? 'password' : 'text';
    event.currentTarget.textContent = isVisible ? 'Show' : 'Hide';
    event.currentTarget.setAttribute('aria-label', isVisible ? 'Show password' : 'Hide password');
    event.currentTarget.setAttribute('aria-pressed', String(!isVisible));
});

signInForm.addEventListener('submit', (event) => {
    event.preventDefault();
    const emailName = document.getElementById('signInEmail').value.split('@')[0];
    const displayName = emailName.replace(/[._-]+/g, ' ').replace(/\b\w/g, character => character.toUpperCase());
    document.getElementById('activeUserName').textContent = displayName || 'Operator';
    document.getElementById('activeUserInitial').textContent = (displayName || 'Operator').charAt(0);
    signInScreen.hidden = true;
    dashboardShell.hidden = false;
    showView(window.location.hash.slice(1) || 'overview', false);
    loadDashboard();
});

document.getElementById('signOutButton').addEventListener('click', () => {
    signInForm.reset();
    signInPassword.type = 'password';
    const togglePassword = document.getElementById('togglePassword');
    togglePassword.textContent = 'Show';
    togglePassword.setAttribute('aria-label', 'Show password');
    togglePassword.setAttribute('aria-pressed', 'false');
    dashboardShell.hidden = true;
    signInScreen.hidden = false;
});
