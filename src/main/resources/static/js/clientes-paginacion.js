document.addEventListener('DOMContentLoaded', function () {
    const cuerpoTabla = document.getElementById('cuerpo-tabla-clientes');
    if (!cuerpoTabla) {
        return;
    }

    const filas = Array.from(cuerpoTabla.querySelectorAll('tr[data-cliente]'));
    const paginacion = document.getElementById('paginacion-clientes');
    const infoPaginacion = document.getElementById('info-paginacion');
    const selectorTamano = document.getElementById('tamano-pagina');

    if (filas.length === 0 || !paginacion || !infoPaginacion || !selectorTamano) {
        return;
    }

    let tamanoPagina = parseInt(selectorTamano.value, 10);
    let paginaActual = 1;

    function totalPaginas() {
        return Math.max(1, Math.ceil(filas.length / tamanoPagina));
    }

    function crearItem(etiqueta, pagina, opciones) {
        opciones = opciones || {};
        const li = document.createElement('li');
        li.className = 'page-item'
            + (opciones.activo ? ' active' : '')
            + (opciones.deshabilitado ? ' disabled' : '');

        const enlace = document.createElement('a');
        enlace.className = 'page-link';
        enlace.href = '#';
        enlace.textContent = etiqueta;
        if (opciones.titulo) {
            enlace.title = opciones.titulo;
            enlace.setAttribute('aria-label', opciones.titulo);
        }
        enlace.addEventListener('click', function (evento) {
            evento.preventDefault();
            if (!opciones.deshabilitado) {
                mostrarPagina(pagina);
            }
        });

        li.appendChild(enlace);
        return li;
    }

    function renderizarControles(total) {
        paginacion.innerHTML = '';
        const desde = Math.max(1, paginaActual - 1);
        const hasta = Math.min(total, paginaActual + 1);

        paginacion.appendChild(crearItem('«', 1, { deshabilitado: paginaActual === 1, titulo: 'Primera pagina' }));
        paginacion.appendChild(crearItem('‹', paginaActual - 1, { deshabilitado: paginaActual === 1, titulo: 'Pagina anterior' }));

        if (desde > 1) {
            paginacion.appendChild(crearItem('1', 1));
        }
        if (desde > 2) {
            paginacion.appendChild(crearPuntosSuspensivos());
        }
        for (let pagina = desde; pagina <= hasta; pagina++) {
            paginacion.appendChild(crearItem(String(pagina), pagina, { activo: pagina === paginaActual }));
        }
        if (hasta < total - 1) {
            paginacion.appendChild(crearPuntosSuspensivos());
        }
        if (hasta < total) {
            paginacion.appendChild(crearItem(String(total), total));
        }

        paginacion.appendChild(crearItem('›', paginaActual + 1, { deshabilitado: paginaActual === total, titulo: 'Pagina siguiente' }));
        paginacion.appendChild(crearItem('»', total, { deshabilitado: paginaActual === total, titulo: 'Ultima pagina' }));
    }

    function crearPuntosSuspensivos() {
        const li = document.createElement('li');
        li.className = 'page-item disabled';
        const span = document.createElement('span');
        span.className = 'page-link';
        span.textContent = '…';
        li.appendChild(span);
        return li;
    }

    function renderizarInfo(inicio, fin) {
        const desde = inicio + 1;
        const hasta = Math.min(fin, filas.length);
        infoPaginacion.textContent = 'Mostrando ' + desde + '-' + hasta + ' de ' + filas.length + ' clientes';
    }

    function mostrarPagina(pagina) {
        const total = totalPaginas();
        paginaActual = Math.min(Math.max(pagina, 1), total);

        const inicio = (paginaActual - 1) * tamanoPagina;
        const fin = inicio + tamanoPagina;

        filas.forEach(function (fila, indice) {
            fila.classList.toggle('d-none', indice < inicio || indice >= fin);
        });

        renderizarControles(total);
        renderizarInfo(inicio, fin);
    }

    selectorTamano.addEventListener('change', function () {
        tamanoPagina = parseInt(selectorTamano.value, 10);
        mostrarPagina(1);
    });

    mostrarPagina(1);
});
