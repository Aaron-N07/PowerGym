// Paginacion con JavaScript, pero los datos los pone el SERVIDOR:
// cada vez que cambias de pagina se hace una peticion (fetch) y solo se trae esa pagina.
document.addEventListener('DOMContentLoaded', function () {
    const contenedor = document.getElementById('contenedor-clientes');
    if (!contenedor) {
        return;
    }

    // Pide al servidor la pagina y sustituye el contenido del contenedor
    function cargarPagina(url, guardarEnHistorial) {
        fetch(url, {
            headers: { 'X-Requested-With': 'fetch' },   // asi el servidor sabe que solo queremos el fragmento
            cache: 'no-store'
        })
            .then(function (respuesta) {
                return respuesta.text();
            })
            .then(function (html) {
                contenedor.innerHTML = html;
                if (guardarEnHistorial) {
                    history.pushState(null, '', url);   // la URL cambia sin recargar (para F5 y para Editar/Eliminar)
                }
            });
    }

    // Click en cualquier boton de paginacion
    contenedor.addEventListener('click', function (evento) {
        const enlace = evento.target.closest('a.page-link');
        if (!enlace) {
            return;
        }
        evento.preventDefault();
        if (!enlace.closest('.disabled')) {
            cargarPagina(enlace.href, true);
        }
    });

    // Cambio del selector "Clientes por pagina": se vuelve a la pagina 1
    contenedor.addEventListener('change', function (evento) {
        if (evento.target.id === 'tamano') {
            cargarPagina(evento.target.form.action + '?pagina=1&tamano=' + evento.target.value, true);
        }
    });

    // Botones Atras/Adelante del navegador
    window.addEventListener('popstate', function () {
        cargarPagina(window.location.href, false);
    });
});
