package com.ilerna.PowerGym.controller;

import com.ilerna.PowerGym.exception.DniDuplicadoException;
import com.ilerna.PowerGym.model.Cliente;
import com.ilerna.PowerGym.model.Entrenador;
import com.ilerna.PowerGym.model.TipoMembresia;
import com.ilerna.PowerGym.service.ClienteService;
import jakarta.validation.Valid;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @InitBinder("cliente")
    public void quitarEspaciosDeLosTextos(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    // Ejemplo de URL: /clientes?pagina=2&tamano=10
    @GetMapping
    public String listar(@RequestParam(defaultValue = "1") int pagina,
                         @RequestParam(defaultValue = "10") int tamano,
                         @RequestHeader(value = "X-Requested-With", required = false) String peticionJs,
                         Model model) {
        // Solo se admiten los tamanos del selector
        if (tamano != 10 && tamano != 25 && tamano != 50) {
            tamano = 10;
        }

        long totalClientes = clienteService.contarTodos();
        int totalPaginas = (int) Math.max(1, Math.ceil((double) totalClientes / tamano));

        // La pagina tiene que estar entre 1 y totalPaginas
        pagina = Math.min(Math.max(pagina, 1), totalPaginas);

        model.addAttribute("clientes", clienteService.listarPagina(pagina, tamano));
        model.addAttribute("pagina", pagina);
        model.addAttribute("tamano", tamano);
        model.addAttribute("totalPaginas", totalPaginas);
        model.addAttribute("totalClientes", totalClientes);
        model.addAttribute("desde", totalClientes == 0 ? 0 : (pagina - 1) * tamano + 1);
        model.addAttribute("hasta", Math.min((long) pagina * tamano, totalClientes));

        // Si la peticion viene del JS (fetch), devolvemos solo el trozo de la tabla, no la pagina entera
        if (peticionJs != null) {
            return "clientes/listar :: tabla-clientes";
        }
        return "clientes/listar";
    }

    // pagina y tamano se reciben solo para "recordar" donde estaba el usuario en el listado
    @GetMapping("/nuevo")
    public String formularioNuevo(@RequestParam(defaultValue = "1") int pagina,
                                  @RequestParam(defaultValue = "10") int tamano,
                                  Model model) {
        model.addAttribute("cliente", new Cliente());
        model.addAttribute("pagina", pagina);
        model.addAttribute("tamano", tamano);
        agregarListasDeApoyo(model);
        return "clientes/formulario";
    }

    @GetMapping("/editar/{id}")
    public String formularioEditar(@PathVariable Long id,
                                   @RequestParam(defaultValue = "1") int pagina,
                                   @RequestParam(defaultValue = "10") int tamano,
                                   Model model) {
        model.addAttribute("cliente", clienteService.buscarPorId(id));
        model.addAttribute("pagina", pagina);
        model.addAttribute("tamano", tamano);
        agregarListasDeApoyo(model);
        return "clientes/formulario";
    }

    @PostMapping("/guardar")
    public String guardar(@Valid @ModelAttribute("cliente") Cliente cliente,
                           BindingResult resultado,
                           @RequestParam(defaultValue = "1") int pagina,
                           @RequestParam(defaultValue = "10") int tamano,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        // Si el formulario vuelve a mostrarse con errores, tiene que seguir recordando la pagina
        model.addAttribute("pagina", pagina);
        model.addAttribute("tamano", tamano);
        if (resultado.hasErrors()) {
            agregarListasDeApoyo(model);
            return "clientes/formulario";
        }
        try {
            clienteService.guardar(cliente);
        } catch (DniDuplicadoException ex) {
            resultado.rejectValue("dni", "dni.duplicado", ex.getMessage());
            agregarListasDeApoyo(model);
            return "clientes/formulario";
        }
        redirectAttributes.addFlashAttribute("mensaje", "Cliente guardado correctamente.");
        // addAttribute (no Flash) se anade a la URL: redirect:/clientes?pagina=2&tamano=10
        redirectAttributes.addAttribute("pagina", pagina);
        redirectAttributes.addAttribute("tamano", tamano);
        return "redirect:/clientes";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Long id,
                           @RequestParam(defaultValue = "1") int pagina,
                           @RequestParam(defaultValue = "10") int tamano,
                           RedirectAttributes redirectAttributes) {
        clienteService.eliminar(id);
        redirectAttributes.addFlashAttribute("mensaje", "Cliente eliminado correctamente.");
        redirectAttributes.addAttribute("pagina", pagina);
        redirectAttributes.addAttribute("tamano", tamano);
        return "redirect:/clientes";
    }

    private void agregarListasDeApoyo(Model model) {
        model.addAttribute("membresias", TipoMembresia.values());
        model.addAttribute("entrenadores", Entrenador.values());
    }
}
