package com.bspapeleria.backend.service;

import com.bspapeleria.backend.dto.ValoracionRequest;
import com.bspapeleria.backend.dto.ValoracionResponse;
import com.bspapeleria.backend.entity.Producto;
import com.bspapeleria.backend.entity.Usuario;
import com.bspapeleria.backend.entity.Valoracion;
import com.bspapeleria.backend.exception.BadRequestException;
import com.bspapeleria.backend.exception.ResourceNotFoundException;
import com.bspapeleria.backend.repository.ProductoRepository;
import com.bspapeleria.backend.repository.UsuarioRepository;
import com.bspapeleria.backend.repository.ValoracionRepository;
import com.bspapeleria.backend.repository.CursoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ValoracionService {

    private final ValoracionRepository valoracionRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final CursoRepository cursoRepository;

    @Transactional(readOnly = true)
    public Page<ValoracionResponse> getValoracionesByEntidad(String entidadTipo, Long entidadId, Pageable pageable) {
        return valoracionRepository.findByEntidadTipoAndEntidadId(entidadTipo, entidadId, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ValoracionResponse> getAllValoracionesByEntidad(String entidadTipo, Long entidadId) {
        return valoracionRepository.findByEntidadTipoAndEntidadIdOrderByFechaCreacionDesc(entidadTipo, entidadId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public ValoracionResponse createValoracion(Long usuarioId, ValoracionRequest request) {
        if (valoracionRepository.existsByUsuarioIdAndEntidadTipoAndEntidadId(usuarioId, request.getEntidadTipo(), request.getEntidadId())) {
            throw new BadRequestException("Ya has dejado una valoración para esta entidad");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + usuarioId));

        if (request.getCalificacion() < 1 || request.getCalificacion() > 5) {
            throw new BadRequestException("La calificación debe estar entre 1 y 5");
        }

        Valoracion valoracion = Valoracion.builder()
                .usuario(usuario)
                .entidadTipo(request.getEntidadTipo())
                .entidadId(request.getEntidadId())
                .calificacion(request.getCalificacion())
                .comentario(request.getComentario())
                .build();

        valoracionRepository.save(valoracion);

        actualizarRatingEntidad(request.getEntidadTipo(), request.getEntidadId());

        return toResponse(valoracion);
    }

    @Transactional
    public void deleteValoracion(Long id, Long usuarioId) {
        Valoracion valoracion = valoracionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Valoración no encontrada: " + id));

        if (!valoracion.getUsuario().getId().equals(usuarioId)) {
            throw new BadRequestException("No puedes eliminar esta valoración");
        }

        String entidadTipo = valoracion.getEntidadTipo();
        Long entidadId = valoracion.getEntidadId();

        valoracionRepository.delete(valoracion);

        actualizarRatingEntidad(entidadTipo, entidadId);
    }

    private void actualizarRatingEntidad(String entidadTipo, Long entidadId) {
        Double promedio = valoracionRepository.getPromedioCalificacion(entidadTipo, entidadId);
        int count = valoracionRepository.countByEntidad(entidadTipo, entidadId);

        if ("PRODUCTO".equals(entidadTipo)) {
            productoRepository.findById(entidadId).ifPresent(producto -> {
                producto.setRating(promedio != null ? promedio : 0.0);
                producto.setReviewsCount(count);
                productoRepository.save(producto);
            });
        } else if ("CURSO".equals(entidadTipo)) {
            cursoRepository.findById(entidadId).ifPresent(curso -> {
                curso.setRating(promedio != null ? promedio : 0.0);
                curso.setEstudiantesCount(count);
                cursoRepository.save(curso);
            });
        }
    }

    private ValoracionResponse toResponse(Valoracion valoracion) {
        return ValoracionResponse.builder()
                .id(valoracion.getId())
                .usuarioId(valoracion.getUsuario().getId())
                .nombreUsuario(valoracion.getUsuario().getNombre())
                .entidadTipo(valoracion.getEntidadTipo())
                .entidadId(valoracion.getEntidadId())
                .calificacion(valoracion.getCalificacion())
                .comentario(valoracion.getComentario())
                .createdAt(valoracion.getFechaCreacion())
                .build();
    }
}