package com.bspapeleria.backend.service;

import com.bspapeleria.backend.dto.CarritoItemRequest;
import com.bspapeleria.backend.dto.CarritoResponse;
import com.bspapeleria.backend.entity.*;
import com.bspapeleria.backend.exception.BadRequestException;
import com.bspapeleria.backend.exception.ResourceNotFoundException;
import com.bspapeleria.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final CarritoItemRepository carritoItemRepository;
    private final ProductoRepository productoRepository;
    private final CursoRepository cursoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public CarritoResponse getCarritoByUsuario(Long usuarioId) {
        Carrito carrito = getOrCreateCarrito(usuarioId);
        return toResponse(carrito);
    }

    @Transactional
    public CarritoResponse agregarItem(Long usuarioId, CarritoItemRequest request) {
        Carrito carrito = getOrCreateCarrito(usuarioId);

        CarritoItem.TipoItem tipoItem = CarritoItem.TipoItem.valueOf(request.getTipo().toUpperCase());

        double precioUnitario = obtenerPrecioItem(tipoItem, request.getItemId());

        List<CarritoItem> itemsExistentes = carritoItemRepository.findByCarritoId(carrito.getId());
        CarritoItem existente = itemsExistentes.stream()
                .filter(i -> i.getTipoItem() == tipoItem && i.getItemId().equals(request.getItemId()))
                .findFirst()
                .orElse(null);

        if (existente != null) {
            existente.setCantidad(existente.getCantidad() + request.getCantidad());
            existente.setPrecioUnitario(precioUnitario);
            carritoItemRepository.save(existente);
        } else {
            CarritoItem nuevoItem = CarritoItem.builder()
                    .carrito(carrito)
                    .tipoItem(tipoItem)
                    .itemId(request.getItemId())
                    .cantidad(request.getCantidad())
                    .precioUnitario(precioUnitario)
                    .build();
            carritoItemRepository.save(nuevoItem);
        }

        carritoRepository.save(carrito);
        return toResponse(carrito);
    }

    @Transactional
    public CarritoResponse actualizarItem(Long usuarioId, CarritoItemRequest request) {
        Carrito carrito = getOrCreateCarrito(usuarioId);

        CarritoItem.TipoItem tipoItem = CarritoItem.TipoItem.valueOf(request.getTipo().toUpperCase());

        CarritoItem item = carritoItemRepository.findByCarritoId(carrito.getId()).stream()
                .filter(i -> i.getTipoItem() == tipoItem && i.getItemId().equals(request.getItemId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item no encontrado en el carrito"));

        if (request.getCantidad() <= 0) {
            carritoItemRepository.delete(item);
        } else {
            item.setCantidad(request.getCantidad());
            item.setPrecioUnitario(obtenerPrecioItem(tipoItem, request.getItemId()));
            carritoItemRepository.save(item);
        }

        carritoRepository.save(carrito);
        return toResponse(carrito);
    }

    @Transactional
    public CarritoResponse eliminarItem(Long usuarioId, String tipo, String itemId) {
        Carrito carrito = getOrCreateCarrito(usuarioId);
        CarritoItem.TipoItem tipoItem = CarritoItem.TipoItem.valueOf(tipo.toUpperCase());
        carritoItemRepository.deleteByCarritoIdAndTipoItemAndItemId(carrito.getId(), tipoItem.name(), itemId);
        return toResponse(carrito);
    }

    @Transactional
    public void vaciarCarrito(Long usuarioId) {
        Carrito carrito = getOrCreateCarrito(usuarioId);
        carritoItemRepository.deleteByCarritoId(carrito.getId());
    }

    private Carrito getOrCreateCarrito(Long usuarioId) {
        return carritoRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> {
                    Usuario usuario = usuarioRepository.findById(usuarioId)
                            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + usuarioId));
                    Carrito nuevo = Carrito.builder().usuario(usuario).build();
                    return carritoRepository.save(nuevo);
                });
    }

    private double obtenerPrecioItem(CarritoItem.TipoItem tipoItem, String itemId) {
        if (tipoItem == CarritoItem.TipoItem.PRODUCTO) {
            Producto producto = productoRepository.findById(Long.parseLong(itemId))
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + itemId));
            return producto.getPrecio();
        } else {
            Curso curso = cursoRepository.findById(Long.parseLong(itemId))
                    .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado: " + itemId));
            return curso.getPrecio();
        }
    }

    private CarritoResponse toResponse(Carrito carrito) {
        List<CarritoItem> items = carritoItemRepository.findByCarritoId(carrito.getId());

        List<CarritoResponse.CarritoItemResponse> itemResponses = items.stream()
                .map(item -> {
                    String nombre;
                    String imagenUrl;

                    if (item.getTipoItem() == CarritoItem.TipoItem.PRODUCTO) {
                        nombre = productoRepository.findById(Long.parseLong(item.getItemId()))
                                .map(p -> {
                                    String nom = p.getNombre();
                                    String img = (p.getImagenes() != null && !p.getImagenes().isEmpty()) ? p.getImagenes().iterator().next() : "";
                                    return nom + "|" + img;
                                })
                                .orElse("")
                                .split("\\|")[0];
                        imagenUrl = productoRepository.findById(Long.parseLong(item.getItemId()))
                                .map(p -> (p.getImagenes() != null && !p.getImagenes().isEmpty()) ? p.getImagenes().iterator().next() : "")
                                .orElse("");
                    } else {
                        nombre = cursoRepository.findById(Long.parseLong(item.getItemId()))
                                .map(Curso::getTitulo)
                                .orElse("");
                        imagenUrl = cursoRepository.findById(Long.parseLong(item.getItemId()))
                                .map(Curso::getImagenUrl)
                                .orElse("");
                    }

                    return CarritoResponse.CarritoItemResponse.builder()
                            .id(item.getId())
                            .tipoItem(item.getTipoItem().name())
                            .itemId(item.getItemId())
                            .nombre(nombre)
                            .imagenUrl(imagenUrl)
                            .cantidad(item.getCantidad())
                            .precioUnitario(item.getPrecioUnitario())
                            .subtotal(item.getCantidad() * item.getPrecioUnitario())
                            .build();
                })
                .collect(Collectors.toList());

        double subtotal = itemResponses.stream().mapToDouble(CarritoResponse.CarritoItemResponse::getSubtotal).sum();

        return CarritoResponse.builder()
                .id(carrito.getId())
                .items(itemResponses)
                .subtotal(subtotal)
                .descuento(0.0)
                .total(subtotal)
                .fechaActualizacion(carrito.getFechaActualizacion())
                .build();
    }
}