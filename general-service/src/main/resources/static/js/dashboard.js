document.addEventListener('DOMContentLoaded', () => {
    const modules = [
        {
            name: 'bankAgent',
            healthUrl: '/api/modules/status',
            dataUrl: 'http://localhost:8081/api/bank/latest',
            statusEl: 'bank-status',
            dataEl: 'bank-data',
            render: renderBankData
        },
        {
            name: 'userService',
            healthUrl: '/api/modules/status',
            dataUrl: 'http://localhost:8082/api/users/summary',
            statusEl: 'user-status',
            dataEl: 'user-data',
            render: renderUserData
        }
    ];

    modules.forEach(mod => {
        fetch(mod.healthUrl)
            .then(res => res.json())
            .then(status => {
                const isActive = status[mod.name];
                document.getElementById(mod.statusEl).textContent = isActive ? '✅ Активен' : '❌ Недоступен';
                if (isActive) {
                    fetch(mod.dataUrl)
                        .then(r => r.json())
                        .then(data => {
                            mod.render(data, mod.dataEl);
                            document.getElementById(mod.dataEl).style.display = 'block';
                        })
                        .catch(err => console.warn('Data load failed for', mod.name, err));
                }
            })
            .catch(() => {
                document.getElementById(mod.statusEl).textContent = '❌ Ошибка проверки';
            });
    });
});

function renderBankData(data, elId) {
    const el = document.getElementById(elId);
    if (Array.isArray(data.rates) && data.rates.length > 0) {
        const items = data.rates.map(r =>
            `<div><strong>${r.name || r.symbol}:</strong> ${r.value} ${r.unit || ''}</div>`
        ).join('');
        el.innerHTML = `<h3>Текущие курсы</h3>${items}`;
    } else {
        el.innerHTML = '<p>Нет данных о курсах</p>';
    }
}

function renderUserData(data, elId) {
    const el = document.getElementById(elId);
    el.innerHTML = `
        <h3>Информация о пользователях</h3>
        <p>Всего пользователей: <strong>${data.totalUsers || 0}</strong></p>
        ${data.lastLogin ? `<p>Последний вход: ${new Date(data.lastLogin).toLocaleString()}</p>` : ''}
    `;
}